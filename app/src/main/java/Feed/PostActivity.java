package Feed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import Notification.NotificationtoFirebase;
import com.example.amit.trackmeNew.R;
import com.example.amit.trackmeNew.SelectionActivity;
import com.google.android.gms.tasks.OnSuccessListener;
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

//Owner er post thik kora baki
public class PostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDesc;

    private Button mSubmitButton;

    private Uri mImageUri = null;

    private ProgressDialog mProgress;

    private static final int GALLERY_REQUEST = 1;

    private DatabaseReference mDatabase;


    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Feed");
        mSelectImage = (ImageButton) findViewById(R.id.imageButton);
        mPostTitle = (EditText) findViewById(R.id.post_title);
        mPostDesc = (EditText) findViewById(R.id.post_des);

        mSubmitButton = (Button) findViewById(R.id.post_button);

        mStorage = FirebaseStorage.getInstance().getReference();

        mProgress = new ProgressDialog(this);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    //statuses are posted from here

    private void startPosting() {
        mProgress.setMessage("Posting to FeedActivity...");
        mProgress.show();

        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val)) {
            if (mImageUri != null) {
                postWithImage(title_val, desc_val);
            } else {
                postWithoutImage(title_val, desc_val);
            }

        }
    }


    //When a user update post with image
    private void postWithImage(final String title_val, final String desc_val) {
        StorageReference filepath = mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());
        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                final DatabaseReference newPost = mDatabase.push();
                newPost.child("title").setValue(title_val);
                newPost.child("desc").setValue(desc_val);
                newPost.child("likes").setValue("0");
                newPost.child("wholikes");

                final String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref;
                if (SelectionActivity.getisDriver()) {
                    ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userid).child("username");
                } else {
                    ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userid).child("username");
                }

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Post post = dataSnapshot.getValue(Post.class);
                        try {
                            final String user_name = (String) dataSnapshot.getValue();
                            newPost.child("username").setValue(user_name);

                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                if (downloadUrl.toString() != null) {
                    newPost.child("image").setValue(downloadUrl.toString());
                }

                mProgress.dismiss();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = currentUser.getUid().toString();
                NotificationtoFirebase mNotificationtoFirebase = new NotificationtoFirebase();
                if (SelectionActivity.getisDriver()) {
                    mNotificationtoFirebase.setNotificationToFirebase(userId, "Status", true,21.12345,90.124631210);
                } else {
                    mNotificationtoFirebase.setNotificationToFirebase(userId, "Status", false,21.12345,90.124631210);
                }

                Intent intent = new Intent(PostActivity.this, FeedActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    //When a user update post without an image
    private void postWithoutImage(final String title_val, final String desc_val) {
        final DatabaseReference newPost = mDatabase.push();
        newPost.child("title").setValue(title_val);
        newPost.child("desc").setValue(desc_val);
        newPost.child("likes").setValue("0");
        newPost.child("wholikes");

        final String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref;
        if (SelectionActivity.getisDriver()) {
            ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userid).child("username");
        } else {
            ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userid).child("username");
        }
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Post post = dataSnapshot.getValue(Post.class);
                try {
                    final String user_name = (String) dataSnapshot.getValue();
                    newPost.child("username").setValue(user_name);

                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Toast.makeText(PostActivity.this, "chobi chara..", Toast.LENGTH_SHORT).show();
        mProgress.dismiss();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid().toString();
        NotificationtoFirebase mNotificationtoFirebase = new NotificationtoFirebase();
        if (SelectionActivity.getisDriver()) {
            mNotificationtoFirebase.setNotificationToFirebase(userId, "Status", true,21.12345,90.124631210);
        } else {
            mNotificationtoFirebase.setNotificationToFirebase(userId, "Status", false,21.12345,90.124631210);
        }
        Intent intent = new Intent(PostActivity.this, FeedActivity.class);
        startActivity(intent);
        finish();
    }


    //Accessing the gallery and selecting the image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData();
            mSelectImage.setImageURI(mImageUri);
        }


    }

}
