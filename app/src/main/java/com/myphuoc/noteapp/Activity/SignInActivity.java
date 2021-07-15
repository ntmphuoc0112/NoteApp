package com.myphuoc.noteapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.myphuoc.noteapp.R;

public class SignInActivity extends AppCompatActivity {

    public static final String TAG = "GoogleSignIn";
    public static final int RC_SIGN_IN = 321;
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signInButton = findViewById(R.id.googleSignInBtn);
        mAuth = FirebaseAuth.getInstance();
        requestGoogleSignIn();

        signInButton.setOnClickListener(view ->{
            signIn();
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void requestGoogleSignIn() {
        // Config sign in to request the user's basic profile like name and email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "signInWithCredential:success");
                    // Sign in success, navigate user to Main Activity
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    // if sign in fail, display a message to the user
                    Toast.makeText(SignInActivity.this, "Sign in fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent((data));
            try {
               // Google Sign in was successful, authenticate with Firebase
               GoogleSignInAccount account = task.getResult(ApiException.class);
               // authenticating user with firebase using received token id
                firebaseAuthWithGoogle(account.getIdToken());

                // assigning user information to variable
                String userName = account.getDisplayName();
                String userEmail = account.getEmail();
                String userPhoto = account.getPhotoUrl().toString();
                userPhoto = userPhoto+"?type = large";

                // create sharePreference to store user data when user signs in successfully
                SharedPreferences.Editor editor = getApplicationContext()
                        .getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        .edit();
                editor.putString("username", userName);
                editor.putString("userEmail", userEmail);
                editor.putString("userPhoto", userPhoto);
                editor.apply();

                Log.i(TAG, "onActivityResult: Success");

            } catch (ApiException e) {
               Log.e(TAG, "onActivityResult: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}