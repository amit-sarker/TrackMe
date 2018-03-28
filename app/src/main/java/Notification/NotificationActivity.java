package Notification;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.amit.trackmeNew.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView mNotificationList;
    private DatabaseReference mNotificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        mNotificationList = (RecyclerView) findViewById(R.id.feed_id);
        mNotificationList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mNotificationList.setLayoutManager(mLayoutManager);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        final FirebaseUser curuser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = curuser.getUid();
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications").child(currentUser);

    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<NotificationHelper, NotificationActivity.NotificationViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NotificationHelper, NotificationActivity.NotificationViewHolder>(
                NotificationHelper.class,
                R.layout.notification_row,
                NotificationActivity.NotificationViewHolder.class,
                mNotificationDatabase
        ) {
            @Override
            protected void populateViewHolder(NotificationActivity.NotificationViewHolder viewHolder, NotificationHelper model, int position) {

                viewHolder.setNotificationTitle(model.getNotificationTitle());
                viewHolder.setNotificationBody(model.getNotificationBody());

                if (model.getNotificationType().equals("Emergency")) {
                    viewHolder.setNotificationImage(1);
                } else if (model.getNotificationType().equals("Status")) {
                    viewHolder.setNotificationImage(2);
                }
                else if (model.getNotificationType().equals("Roaming")) {
                    viewHolder.setNotificationImage(3);
                }

            }
        };

        mNotificationList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        //TextView post_title
        View mView;
        //Constructor

        public NotificationViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        //Setters for setting up notification
        public void setNotificationTitle(String title) {
            TextView post_title = (TextView) mView.findViewById(R.id.notifiaction_title);
            post_title.setText(title);
        }

        public void setNotificationBody(String Body) {
            TextView post_body = (TextView) mView.findViewById(R.id.notification_desc);
            post_body.setText(Body);
        }

        public void setNotificationImage(int isemergency) {
            ImageView post_image = (ImageView) mView.findViewById(R.id.notification_image);
            if (isemergency==1) {
                post_image.setImageResource(R.drawable.ic_emergency);

            } else if (isemergency==2){
                post_image.setImageResource(R.drawable.ic_status);
            }
            else if (isemergency==3)
            {
                post_image.setImageResource(R.drawable.ic_roaming);
            }

        }

    }
}
