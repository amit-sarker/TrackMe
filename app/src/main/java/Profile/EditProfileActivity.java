package Profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import Maps.DriverMapActivity;
import Maps.OwnerMapActivity;
import com.example.amit.trackmeNew.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity  extends AppCompatActivity {

    private Button btnUpdate;
    private TextView user_name;
    private EditText mContact, mPassword1, mPassword2, mPassword0, mName;
    private DatabaseReference ref;
    private ProgressDialog progressDialog;

    private CircleImageView mCircleimageView;
    private TextView mSelectImagebtn;
    private Uri mImageUri = null;
    private static final int GALLERY_REQUEST = 1;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mName = (EditText) findViewById(R.id.ep_new_name);
        btnUpdate = (Button) findViewById(R.id.btn_update);
        mContact = (EditText) findViewById(R.id.new_contact);
        mPassword0 = (EditText) findViewById(R.id.current_password);
        mPassword1 = (EditText) findViewById(R.id.new_password);
        mPassword2 = (EditText) findViewById(R.id.confirm_password);
        user_name = (TextView) findViewById(R.id.ep_user_name);
        mCircleimageView = (CircleImageView) findViewById(R.id.moumitaimg);
        mSelectImagebtn = (TextView) findViewById(R.id.ep_upload_btn);
        mStorage = FirebaseStorage.getInstance().getReference();

        final String isUser;
        Bundle bundle = getIntent().getExtras();
        isUser = bundle.getString("user");

        final String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (isUser.equalsIgnoreCase("Owner")) {
            ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userUID);
        } else {
            ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userUID);
        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if(!image.equals("true"))
                {
                    Picasso.with(getApplicationContext()).load(image).into(mCircleimageView);
                }
                user_name.setText(username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mSelectImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });



        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String newName = mName.getText().toString();
                final String oldpass = mPassword0.getText().toString();
                final String newPass = mPassword1.getText().toString();
                final String confirmPass = mPassword2.getText().toString();
                final String contact = mContact.getText().toString();

                if(newName.isEmpty()) {
                    mName.setError("enter new name");
                    return;
                } else if(!isValid(newName)) {
                    mName.setError("only alphanumeric characters");
                    return;
                }
                else {
                    mName.setError(null);
                }

                if(contact.isEmpty() || contact.length() < 11) {
                    mContact.setError("at least 11 numbers(0-9)");
                    return;
                } else {
                    mContact.setError(null);
                }

                if(newPass.isEmpty() || newPass.length() < 6) {
                    mPassword1.setError("at least 6 alphanumeric characters");
                    return;
                } else {
                    mPassword1.setError(null);
                }

                if(confirmPass.isEmpty() || confirmPass.length() < 6) {
                    mPassword2.setError("at least 6 alphanumeric characters");
                    return;
                } else {
                    mPassword2.setError(null);
                }

                if(oldpass.isEmpty() || oldpass.length() < 6) {
                    mPassword0.setError("at least 6 alphanumeric characters");
                    return;
                } else {
                    mPassword0.setError(null);
                }

                //Upadate password, contact info
                if (newPass.compareTo(confirmPass) != 0) {
                    mPassword2.setError("Passwords are not same");
                    return;
                } else {
                    final FirebaseUser user;
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    final String email = user.getEmail();

                    AuthCredential credential = EmailAuthProvider.getCredential(email, oldpass);

                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(EditProfileActivity.this, "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if(mImageUri!=null)
                                            {
                                                StorageReference filepath = mStorage.child("Profile_Images").child(userUID).child(mImageUri.getLastPathSegment());
                                                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();




                                                        if (downloadUrl.toString() != null) {
                                                            DatabaseReference ref1, ref2;
                                                            String username = user_name.getText().toString();
                                                            if (isUser.equalsIgnoreCase("Owner")) {
                                                                ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userUID);
                                                                ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("username").child(username);

                                                            } else {
                                                                ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userUID);
                                                                ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(username);
                                                            }
                                                            ref1.child("image").setValue(downloadUrl.toString());

                                                            ref2.child("image").setValue(downloadUrl.toString());

                                                        }


                                                    }
                                                });

                                            }

                                            Toast.makeText(EditProfileActivity.this, "Profile is Successfully Modified", Toast.LENGTH_SHORT).show();

                                            DatabaseReference ref1, ref2;
                                            String username = user_name.getText().toString();
                                            if (isUser.equalsIgnoreCase("Owner")) {
                                                ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userUID);
                                                ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("username").child(username);

                                            } else {
                                                ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userUID);
                                                ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(username);
                                            }
                                            ref1.child("contact no").setValue(contact);
                                            ref1.child("name").setValue(newName);
                                            ref2.child("contact no").setValue(contact);
                                            ref2.child("name").setValue(newName);

                                            if (isUser.equalsIgnoreCase("Owner")) {
                                                Intent intent = new Intent(EditProfileActivity.this, OwnerMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                                return;
                                            } else {
                                                Intent intent = new Intent(EditProfileActivity.this, DriverMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                                return;
                                            }

                                        }
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                mPassword0.setError("Current password is incorrect");
                                return;
                            }
                        }
                    });
                    progressDialog = ProgressDialog.show(EditProfileActivity.this, "Please wait.",
                            "Updating..!", true);
                }
            }
        });
    }

    private boolean isValid(String string) {
        boolean valid = true;
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData();
            mCircleimageView.setImageURI(mImageUri);
        }


    }
}
