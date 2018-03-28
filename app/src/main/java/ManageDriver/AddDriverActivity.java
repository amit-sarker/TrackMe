package ManageDriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import Maps.OwnerMapActivity;
import com.example.amit.trackmeNew.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddDriverActivity extends AppCompatActivity {

    private EditText mDriverUserName;
    private Button mAddDriver;
    private FirebaseAuth mAuth;
    private Query usernameQuery;
    private EditText mDriverCode;
    private ProgressDialog progressDialog;

    private DatabaseReference mAddDriverList,mAddOwnerList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);

        mDriverUserName = (EditText) findViewById(R.id.driverusername);
        mAddDriver = (Button) findViewById(R.id.adddriver_btn);
        mDriverCode = (EditText) findViewById(R.id.drivercode);
        mAuth = FirebaseAuth.getInstance();

        mAddDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String driverUserName = mDriverUserName.getText().toString();
                final String ownerUID = mAuth.getCurrentUser().getUid();
                final String driverCode = mDriverCode.getText().toString();

                if(driverUserName.isEmpty() || driverUserName.length() < 3) {
                    mDriverUserName.setError("at least 3 characters");
                    return;
                } else {
                    mDriverUserName.setError(null);
                }

                if(driverCode.isEmpty()) {
                    mDriverCode.setError("at least 6 characters");
                    return;
                } else {
                    mDriverCode.setError(null);
                }

                progressDialog = ProgressDialog.show(AddDriverActivity.this, "Please wait.",
                        "Adding..!", true);

                usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").orderByChild("username").equalTo(driverUserName);

                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() == 0) {
                            progressDialog.dismiss();
                            mDriverUserName.setError("This driver is not available");
                        } else {
                            mDriverUserName.setError(null);
                            DatabaseReference driverCodeRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(driverUserName).child("code");
                            driverCodeRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String validCode = dataSnapshot.getValue().toString();
                                    if(validCode.compareTo(driverCode) == 0) {
                                        mDriverCode.setError(null);


                                        DatabaseReference driveUID = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(driverUserName).child("UID");
                                        driveUID.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                final String driverUID = (String) dataSnapshot.getValue();
                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(ownerUID).child("Drivers").child(driverUserName);
                                                ref.setValue(driverUserName);
                                                mAddDriverList = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(ownerUID).child("DriverList").push();
                                                mAddDriverList.child("username").setValue(driverUserName);
                                                mAddDriverList.child("UID").setValue(driverUID);


                                                final DatabaseReference ownerUserName = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(ownerUID).child("username");
                                                ownerUserName.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        String ownUserName = (String) dataSnapshot.getValue();
                                                        mAddOwnerList = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(driverUID).child("OwnerList").push();
                                                        mAddOwnerList.child("UID").setValue(ownerUID);
                                                        mAddOwnerList.child("username").setValue(ownUserName);

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });



                                        DatabaseReference ownerUserName = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(ownerUID).child("username");
                                        ownerUserName.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String ownUserName = (String) dataSnapshot.getValue();

                                                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(driverUserName).child("Owner").child(ownerUID).child("username");
                                                ref2.setValue(ownUserName);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        Intent intent = new Intent(AddDriverActivity.this, OwnerMapActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else{
                                        progressDialog.dismiss();
                                        mDriverCode.setError("Code did not match");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
