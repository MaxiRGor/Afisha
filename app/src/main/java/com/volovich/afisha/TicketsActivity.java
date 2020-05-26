package com.volovich.afisha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import java.util.List;

public class TicketsActivity extends AppCompatActivity {

    private String uid;
    private WishlistAdapter ticketsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        setTitle(getResources().getString(R.string.sold_tickts));
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else uid = "";

        this.ticketsAdapter = new WishlistAdapter(this, new ArrayList<Event>());
        setRecyclerView();
        loadTicketsFromFirestore();
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.wishlist_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(ticketsAdapter);
    }


    private void loadTicketsFromFirestore() {
        //load wishlist documents with uid == (current user uid) collection
        FirebaseFirestore.getInstance()
                .collection(getString(R.string.firestore_collection_wishlists))
                .whereEqualTo(getString(R.string.firestore_field_uid), uid)
                .whereEqualTo(getString(R.string.firestore_field_is_purchased), true)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().size() == 0) {
                            ticketsAdapter.clearList();
                            Snackbar.make(findViewById(R.id.wishlist_recycler_view), getString(R.string.you_havent_bought_any_ticket_yet), Snackbar.LENGTH_LONG).show();
                        } else {
                            ticketsAdapter.clearList();

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
                                            //for easy access between collections
                                            event.setWishListDocumentId(wishlist.getDocumentId());
                                            //for counting a total price
                                            event.setCount(wishlist.getCount());
                                            Log.d("logs", event.getWishListDocumentId());
                                            ticketsAdapter.addEvent(event);
                                            Log.d("logs", event.getTitle());
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_tickets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_open_wishlist) {
            startWishlistActivity();
            return true;
        }

        if (id == R.id.action_open_afisha) {
            startAfishaActivity();
            return true;
        }

        if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startWishlistActivity() {
        Intent intent = new Intent(TicketsActivity.this, WishlistActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.diagonaltranslate,R.anim.alpha);
        finish();
    }

    private void startAfishaActivity() {
        Intent intent = new Intent(TicketsActivity.this, AfishaActivity.class);
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
                        Intent intent = new Intent(TicketsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }


}
