package com.volovich.afisha;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "log";
    private float delayInSeconds = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.login_activity);
        checkUserAuthorization();
    }

    private void checkUserAuthorization() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    createSignInIntent();
                } else {
                    startAfishaActivity();
                }
            }
        }, (long) delayInSeconds * 1000);

    }

    private void startAfishaActivity() {
        Intent intent = new Intent(LoginActivity.this, AfishaActivity.class);
        startActivity(intent);
        //finish current LoginActivity
        finish();
    }

    private void createSignInIntent() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(

                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }


    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (response != null) {
                if (resultCode == RESULT_OK) {
                    // Successfully signed in
                    Log.d(TAG, response.toString());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    startAfishaActivity();
                    // ...
                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    // ...
                    if (response.getError() != null && response.getError().getLocalizedMessage() != null)
                        Log.d(TAG, response.getError().getLocalizedMessage());
                    //I made a loop
                    createSignInIntent();
                }
            }
        }
    }
}
