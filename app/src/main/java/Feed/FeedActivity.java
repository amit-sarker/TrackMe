package Feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amit.trackmeNew.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView mStatusList;
    private DatabaseReference mDatabase, mDatabaselike, mDatabasewholikes;
    private boolean mProcessLike = false;
    private FloatingActionButton mAddPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mStatusList = (RecyclerView) findViewById(R.id.feed_id);
        mAddPost = (FloatingActionButton) findViewById(R.id.fab_post_btn);
        mStatusList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mStatusList.setLayoutManager(mLayoutManager);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Feed");
        mDatabaselike = FirebaseDatabase.getInstance().getReference().child("Feed");
        mDatabasewholikes = FirebaseDatabase.getInstance().getReference().child("Feed");

        mAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeedActivity.this, PostActivity.class);
                startActivity(intent);
                return;
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<FeedHelper, FeedViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FeedHelper, FeedViewHolder>(
                FeedHelper.class,
                R.layout.feed_row,
                FeedViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(FeedViewHolder viewHolder, FeedHelper model, int position) {

                try
                {
                    final String post_key = getRef(position).getKey();
                    final int pos = position;

                    //Set values to viewholder
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setDesc(model.getDesc());
                    viewHolder.setUsername(model.getUsername() + " has posted a new post");
                    viewHolder.setImage(getApplicationContext(), model.getImage());
                    viewHolder.setLikes(getApplicationContext(), model.getLikes());
                    viewHolder.setLikebutton(post_key);
                    ////////////////////////////

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View view) {
                            Toast.makeText(FeedActivity.this, "You clicked a view", Toast.LENGTH_SHORT);
                        }
                    });



                    viewHolder.mLikebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String post_key = getRef(pos).getKey();
                            countLikes(post_key);

                        }
                    });
                }
                catch (Exception e)
                {

                }



            }
        };

        mStatusList.setAdapter(firebaseRecyclerAdapter);
    }


    //Count the number of likes in a post
    private void countLikes(final String post_key) {
        final FirebaseUser curuser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaselike = FirebaseDatabase.getInstance().getReference().child("Feed").child(post_key).child("likes");
        mDatabasewholikes = FirebaseDatabase.getInstance().getReference().child("Feed").child(post_key).child("wholikes");
        mProcessLike = true;


        mDatabasewholikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long cnt = dataSnapshot.getChildrenCount();
                mDatabaselike.setValue(Long.toString(cnt));
                if (mProcessLike) {

                    if (dataSnapshot.hasChild(curuser.getUid())) {
                        cnt = dataSnapshot.getChildrenCount();
                        mDatabaselike.setValue(Long.toString(cnt));


                    } else {
                        mDatabasewholikes.child(curuser.getUid()).setValue("true");
                        cnt = dataSnapshot.getChildrenCount();
                        mDatabaselike.setValue(Long.toString(cnt));

                    }
                    mProcessLike = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    //This is the holder which sets the contents of the feed
    public static class FeedViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton mLikebutton;
        TextView mLikecount;
        DatabaseReference mDatabasewholikes;
        final FirebaseUser curuser = FirebaseAuth.getInstance().getCurrentUser();

        public FeedViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mLikebutton = (ImageButton) mView.findViewById(R.id.likeButton);
            mLikecount = (TextView) mView.findViewById(R.id.likecount);

        }

        //Here are the setters for making each post
        public void setTitle(String title) {
            TextView post_title = (TextView) mView.findViewById(R.id.status_title);
            post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView post_desc = (TextView) mView.findViewById(R.id.status_desc);
            post_desc.setText(desc);
        }

        public void setUsername(String username) {
            TextView post_username = (TextView) mView.findViewById(R.id.status_userid);
            post_username.setText(username);
        }

        public void setImage(Context ctx, String image) {
            ImageView post_image = (ImageView) mView.findViewById(R.id.status_image);
            Picasso.with(ctx).load(image).into(post_image);
        }

        public void setLikes(Context ctx, String likes) {
            TextView post_likes = (TextView) mView.findViewById(R.id.likecount);
            post_likes.setText(likes);
        }

        public void setLikebutton(String post_key) {
            mDatabasewholikes = FirebaseDatabase.getInstance().getReference().child("Feed").child(post_key).child("wholikes");
            mDatabasewholikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(curuser.getUid())) {
                        mLikebutton.setImageResource(R.mipmap.ic_star_black_48dp);
                    } else {
                        mLikebutton.setImageResource(R.mipmap.ic_star_border_black_48dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(FeedActivity.this, PostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}