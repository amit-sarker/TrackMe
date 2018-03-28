package Profile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.amit.trackmeNew.R;
import com.example.amit.trackmeNew.SelectionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView mPaImageView;
    private TextView mPaName, mPaUsername, mPaEmail, mPaCode, mPaContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mPaImageView = (CircleImageView) findViewById(R.id.pa_moumitaimg);
        mPaName = (TextView) findViewById(R.id.pa_profilename);
        mPaCode = (TextView) findViewById(R.id.pa_profilecode);
        mPaContact = (TextView) findViewById(R.id.pa_profilecontact);
        mPaEmail = (TextView) findViewById(R.id.pa_profileemail);
        mPaUsername = (TextView) findViewById(R.id.pa_profileusername);
        setProfileInfo();
    }

    private void setProfileInfo() {
        DatabaseReference infoRef;
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        final String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
        if(SelectionActivity.getisDriver()) {
            infoRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userID);
        } else {
            infoRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userID);
        }

        infoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String contact_no = dataSnapshot.child("contact no").getValue().toString();
                String username = dataSnapshot.child("username").getValue().toString();
                String code = " ";
                String image = "";
                image = dataSnapshot.child("image").getValue().toString();
                if(!image.equals("true"))
                Picasso.with(getApplicationContext()).load(image).into(mPaImageView);

                if(SelectionActivity.getisDriver()) {
                    code += dataSnapshot.child("code").getValue().toString();
                } else {
                    code += "N/A";
                }

                mPaCode.setText( code);
                mPaContact.setText( contact_no);
                mPaEmail.setText(userEmail);
                mPaName.setText(name);
                mPaUsername.setText( username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
