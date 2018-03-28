package com.example.amit.trackmeNew;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Maps.DriverMapActivity;
import Maps.OwnerMapActivity;

public class MainActivity extends AppCompatActivity {

    private static int WELCOME_TIMEOUT = 4000;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);


        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                    final DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("Location").child(userId);

                    mUserRef.addValueEventListener(new ValueEventListener() {
                        boolean flag = true;
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String usertype = dataSnapshot.child("Usertype").getValue().toString();
                            Intent intent;

                            if (usertype.equals("Driver") && flag) {
                                SelectionActivity.setIsDriver(true);
                                intent = new Intent(MainActivity.this, DriverMapActivity.class);
                                flag = false;
                                startActivity(intent);
                                finish();
                                return;

                            } else if(usertype.equals("Owner") && flag) {
                                SelectionActivity.setIsDriver(false);
                                intent = new Intent(MainActivity.this, OwnerMapActivity.class);

                                flag = false;
                                startActivity(intent);
                                finish();
                                return;
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    try {

                    } catch (NullPointerException e) {}

                }
                else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent welcome = new Intent(MainActivity.this, SelectionActivity.class);
                            startActivity(welcome);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }
                    }, WELCOME_TIMEOUT);
                }
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
