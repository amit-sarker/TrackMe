package ManageDriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.amit.trackmeNew.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DriverListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ListView mListView;
    private ArrayList<String> driverList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);

        mAuth = FirebaseAuth.getInstance();

        String owner_id = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(owner_id).child("Drivers");
        mListView = (ListView) findViewById(R.id.listview);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.my_list, driverList);
        mListView.setAdapter(arrayAdapter);

        //this will add all the drivers of a particular owner from the database when a new child is added
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String value = dataSnapshot.getValue(String.class);
                driverList.add(value);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mListView.setClickable(true);

        //this will show the driver list on UI

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemNumber, long l) {

                Object obj = mListView.getAdapter().getItem(itemNumber);
                final String driverUserName = obj.toString();
                Log.d("MyLog", "Value is: " + driverUserName);

                Intent intent = new Intent(DriverListActivity.this, DriverLocationToOwner.class);
                intent.putExtra("username", driverUserName);
                startActivity(intent);
                finish();
            }
        });
    }
}