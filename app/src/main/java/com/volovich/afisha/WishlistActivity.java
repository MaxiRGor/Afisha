package com.volovich.afisha;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.cardform.view.CardForm;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.volovich.afisha.adapters.WishlistAdapter;
import com.volovich.afisha.model.Event;
import com.volovich.afisha.model.Wishlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishlistActivity extends AppCompatActivity {

    private String uid;
    private WishlistAdapter wishlistAdapter;

    private List<String> markedEventsDocumentIds;
    private Button purchaseButton;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        setTitle(getResources().getString(R.string.wishlist));
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else uid = "";

        //initialize adapter with empty list of events
        this.wishlistAdapter = new WishlistAdapter(this, new ArrayList<Event>());
        markedEventsDocumentIds = new ArrayList<>();
        setRecyclerView();
        setPurchaseButton();
        loadWishesFromFirestore();
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.wishlist_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(wishlistAdapter);

        //for swiping
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                wishlistAdapter.removeEventFromWishlist(viewHolder.getAdapterPosition());
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadWishesFromFirestore();
                    }
                }, 2000);

            }
        }).attachToRecyclerView(recyclerView);
    }


    private void setPurchaseButton() {
        purchaseButton = findViewById(R.id.purchase_button);
        purchaseButton.setVisibility(View.GONE);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPurchaseDialog();
            }
        });
    }

    private void createPurchaseDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(WishlistActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        ViewGroup subView = (ViewGroup) inflater.inflate(R.layout.dialog_purchase, null);

        alertBuilder.setView(subView);
        final AlertDialog alertDialog = alertBuilder.create();

        final CardForm cardForm = subView.findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .setup(WishlistActivity.this);
        Button applyButton = subView.findViewById(R.id.apply_button);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cardForm.isValid()) {
                    alertDialog.dismiss();
                    createConfirmDialog(cardForm);

                } else {
                    Snackbar.make(findViewById(R.id.wishlist_recycler_view), getString(R.string.please_complete_the_form), Snackbar.LENGTH_LONG).show();
                }

            }
        });

        alertDialog.show();
    }

    private void createConfirmDialog(CardForm cardForm) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(WishlistActivity.this);
        alertBuilder.setTitle(R.string.confirm_before_purchase);
        alertBuilder.setMessage("Номер карты: " + cardForm.getCardNumber() + "\n" +
                "Дейсвтительна до: " + cardForm.getExpirationDateEditText().getText().toString() + "\n" +
                "CVV: " + cardForm.getCvv() + "\n" +
                "Сумма оплаты: " + String.format(getString(R.string.event_price_value), totalPrice) + "\n");
        alertBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                purchaseEventsInWishlist();
                Snackbar.make(findViewById(R.id.wishlist_recycler_view), getString(R.string.thanks_for_purchase), Snackbar.LENGTH_LONG).show();
            }
        });
        alertBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }



    private void loadWishesFromFirestore() {
        //load wishlist documents with uid == (current user uid) collection
        FirebaseFirestore.getInstance()
                .collection(getString(R.string.firestore_collection_wishlists))
                .whereEqualTo(getString(R.string.firestore_field_uid), uid)
                .whereEqualTo(getString(R.string.firestore_field_is_purchased), false)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().size() == 0) {
                            wishlistAdapter.clearList();
                            Snackbar.make(findViewById(R.id.wishlist_recycler_view), getString(R.string.you_havent_mark_any_event_yet), Snackbar.LENGTH_LONG).show();
                            purchaseButton.setVisibility(View.GONE);
                        } else {
                            wishlistAdapter.clearList();
                            totalPrice = 0;
                            markedEventsDocumentIds = new ArrayList<>();
                            List<Wishlist> wishlists = new ArrayList<>();
                            for (QueryDocumentSnapshot result : task.getResult()) {
                                Wishlist wishlist = result.toObject(Wishlist.class);
                                wishlist.setDocumentId(result.getId());
                                wishlists.add(wishlist);
                            }
                            //then load events documents by event id which we get from wishlists
                            for (final Wishlist wishlist : wishlists) {
                                FirebaseFirestore.getInstance()
                                        .collection(getString(R.string.firestore_collection_events))
                                        .document(wishlist.getEventId())
                                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Event event = documentSnapshot.toObject(Event.class);
                                        if (event != null) {
                                            //because we do not keep documentId as document field, we have to insert it like this
                                            event.setDocumentId(wishlist.getEventId());
                                            markedEventsDocumentIds.add(wishlist.getDocumentId());
                                            //for easy access between collections
                                            event.setWishListDocumentId(wishlist.getDocumentId());
                                            //for counting a total price
                                            event.setCount(wishlist.getCount());
                                            Log.d("logs", event.getWishListDocumentId());
                                            wishlistAdapter.addEvent(event);
                                            Log.d("logs", event.getTitle());

                                            totalPrice += (int) (event.getPrice() * event.getCount());
                                            purchaseButton.setText(String.format(getString(R.string.purchse), totalPrice));
                                        }
                                    }
                                });
                            }
                            purchaseButton.setVisibility(View.VISIBLE);

                        }


                    }
                }
            }
        });
    }

    private void purchaseEventsInWishlist() {
        Map<String,Object> data = new HashMap<>();
        data.put(getString(R.string.firestore_field_is_purchased), true);

        for(String wishlistDocumentId : markedEventsDocumentIds){
            FirebaseFirestore.getInstance()
                    .collection(getString(R.string.firestore_collection_wishlists))
                    .document(wishlistDocumentId)
                    .update(data);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadWishesFromFirestore();
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_whishlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open_afisha) {
            startAfishaActivity();
            return true;
        }

        if (id == R.id.action_open_tickets) {
            startTicketsActivity();
            return true;
        }

        if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startTicketsActivity() {
        Intent intent = new Intent(WishlistActivity.this, TicketsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.diagonaltranslate, R.anim.alpha);
        finish();
    }

    private void startAfishaActivity() {
        Intent intent = new Intent(WishlistActivity.this, AfishaActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.diagonaltranslate, R.anim.alpha);
        finish();
    }


    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        //start LoginActivity
                        Intent intent = new Intent(WishlistActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

}
