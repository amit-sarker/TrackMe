package Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import Maps.DriverMapActivity;
import com.example.amit.trackmeNew.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DriverSignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private EditText mNameRegistration;
    private EditText mEmailRegistration;
    private EditText mPasswordRegistration;
    private EditText mContactRegistration;
    private EditText mUsernameRegistration;
    private Button mButtonRegistration;
    private TextView mLoginText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(DriverSignUpActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                }
                return;
            }
        };

        mNameRegistration = (EditText) findViewById(R.id.input_name);
        mEmailRegistration = (EditText) findViewById(R.id.input_email);
        mPasswordRegistration = (EditText) findViewById(R.id.input_password);
        mContactRegistration = (EditText) findViewById(R.id.input_contact);
        mUsernameRegistration = (EditText) findViewById(R.id.input_username);
        mLoginText = (TextView) findViewById(R.id.link_login);
        mButtonRegistration = (Button) findViewById(R.id.btn_signup);

        mLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverSignUpActivity.this, DriverSigninActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mButtonRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = mNameRegistration.getText().toString();
                final String email = mEmailRegistration.getText().toString();
                final String password = mPasswordRegistration.getText().toString();
                final String username = mUsernameRegistration.getText().toString();
                final String contact = mContactRegistration.getText().toString();

                if(name.isEmpty()) {
                    mNameRegistration.setError("enter your name");
                    return;
                } else if(!isValid(name, "name")) {
                    mNameRegistration.setError("only alphanumeric characters");
                    return;
                } else {
                    mNameRegistration.setError(null);
                }

                if(username.isEmpty()|| username.length() < 3) {
                    mUsernameRegistration.setError("at least 3 characters");
                    return;
                } else if(!isValid(username, "username")) {
                    mUsernameRegistration.setError("only alphanumeric characters");
                    return;
                }
                else {
                    mUsernameRegistration.setError(null);
                }

                if(contact.isEmpty() || contact.length() < 11) {
                    mContactRegistration.setError("at least 11 numbers(0-9)");
                    return;
                } else {
                    mContactRegistration.setError(null);
                }

                if(email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailRegistration.setError("enter a valid email address");
                    return;
                } else {
                    mEmailRegistration.setError(null);
                }

                if(password.isEmpty() || password.length() < 6) {
                    mPasswordRegistration.setError("at least 6 alphanumeric characters");
                    return;
                } else {
                    mPasswordRegistration.setError(null);
                }

                //check the username if it is unique or not
                Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").orderByChild("username").equalTo(username);

                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            mUsernameRegistration.setError("choose a different username");
                        } else {
                            mUsernameRegistration.setError(null);
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        mEmailRegistration.setError("this email is already registered");
                                        Toast.makeText(DriverSignUpActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                                    } else {
                                        mEmailRegistration.setError(null);
                                        String user_id = mAuth.getCurrentUser().getUid();
                                        final DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(username);

                                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                        String code = getSaltString();
                                        //save users information on firebase
                                        Map newPost = new HashMap();
                                        newPost.put("UID", user_id);
                                        newPost.put("name", name);
                                        newPost.put("contact no", contact);
                                        newPost.put("code",code);
                                        newPost.put("image","true");

                                        current_user_db.setValue(newPost);

                                        final DatabaseReference current_user_db2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(user_id);

                                        Map newPost2 = new HashMap();
                                        newPost2.put("username", username);
                                        newPost2.put("name", name);
                                        newPost2.put("contact no", contact);
                                        newPost2.put("device_token", deviceToken);
                                        newPost2.put("code",code);
                                        newPost2.put("image","true");

                                        current_user_db2.setValue(newPost2);
                                        ////////////////////////////////////
                                        final DatabaseReference mClustering = FirebaseDatabase.getInstance().getReference().child("Clustering").child(user_id);
                                        mClustering.setValue("true");
                                    }
                                }
                            });
                            progressDialog = ProgressDialog.show(DriverSignUpActivity.this, "Please wait.",
                                    "Logging in..!", true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }


    protected static String getSaltString() {
        int length = 6;
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    private boolean isValid(String string, String id) {
        boolean valid = true;
        if(id == "username") {
            for (int i = 0; i < string.length(); i++) {
                if (!(string.charAt(i) >= 'a' && string.charAt(i) <= 'z')) {
                    if (!(string.charAt(i) >= 'A' && string.charAt(i) <= 'Z')) {
                        if (!(string.charAt(i) >= '0' && string.charAt(i) <= '9')) {
                            valid = false;
                        }
                    }
                }
            }
            return valid;
        } else {
            for(int i = 0; i < string.length(); i++) {
                if(!(string.charAt(i) >= 'a' && string.charAt(i) <= 'z')) {
                    if(!(string.charAt(i) >= 'A' && string.charAt(i) <= 'Z')) {
                        if(!(string.charAt(i) >= '0' && string.charAt(i) <= '9')) {
                            valid = false;
                        }
                    }
                }
                if(string.charAt(i) == ' ') {
                    valid = true;
                }
            }
            return valid;
        }
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