package com.bntu.afisha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.bntu.afisha.adapters.AfishaAdapter;
import com.bntu.afisha.model.Event;

import java.util.ArrayList;

public class AfishaActivity extends AppCompatActivity {

    private static String UID;
    private AfishaAdapter afishaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afisha);

        //UID is an unique identifier of Firebase user
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else UID = "";

        //initialize adapter with empty list of events
        this.afishaAdapter = new AfishaAdapter(this, new ArrayList<Event>());

        setRecyclerView();
        loadEventsFromFirestore();
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.afisha_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(afishaAdapter);
    }

    private void loadEventsFromFirestore() {
        Log.d("logs", "loading");
        //load full collection, convert the result of query to ArrayList of Events and send it to adapter
        FirebaseFirestore.getInstance()
                .collection(getString(R.string.firestore_collection_events))
                .orderBy(getString(R.string.firebase_field_date), Query.Direction.ASCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        Log.d("logs", "loaded");
                        ArrayList<Event> events = (ArrayList<Event>) task.getResult().toObjects(Event.class);
                        for (int i = 0; i < events.size(); i++) {
                            Log.d("logs", "adding " + task.getResult().getDocuments().get(i).getId());
                            events.get(i).setDocumentId(task.getResult().getDocuments().get(i).getId());
                            afishaAdapter.addEvent(events.get(i));
                        }
                    }
                }
            }
        });
    }

    public static String getUID() {
        Log.d("logs", UID);
        return UID;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_afisha, menu);
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
        Intent intent = new Intent(AfishaActivity.this, TicketsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.diagonaltranslate, R.anim.alpha);
        finish();
    }

    private void startWishlistActivity() {
        Intent intent = new Intent(AfishaActivity.this, WishlistActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.diagonaltranslate,R.anim.alpha);
        finish();
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        //start LoginActivity
                        Intent intent = new Intent(AfishaActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

}
