package HeatMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;

public class HeatMapsActivity extends MapForHeatMap {

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private ArrayList<LatLng> locationList = new ArrayList<>();

    private DatabaseReference mRef;
    private boolean isFirstEntry = true;


    @Override
    protected void startDemo() {

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Location").child(userId).child("UserLocation").child("l");

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lat = dataSnapshot.child("0").getValue().toString();
                String lon = dataSnapshot.child("1").getValue().toString();
                double latitude, longitude;
                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lon);

                if(isFirstEntry) {
                    getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12));
                    isFirstEntry = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ReadFromFirebase();
    }

    private void ReadFromFirebase() {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("Location");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String lat = postSnapshot.child("UserLocation").child("l").child("0").getValue().toString();
                    String lon = postSnapshot.child("UserLocation").child("l").child("1").getValue().toString();

                    double latitude, longitude;
                    latitude = Double.parseDouble(lat);
                    longitude = Double.parseDouble(lon);
                    LatLng latLng = new LatLng(latitude, longitude);
                    if(locationList.size() < 55) {
                        locationList.add(latLng);
                    }
                }
                HeatMapGenerator(locationList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void HeatMapGenerator(ArrayList<LatLng> locationList) {
        if (mProvider == null) {
            mProvider = new HeatmapTileProvider.Builder()
                    .data(locationList)
                    .build();
            mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        } else {
            mProvider.setData(locationList);
            mOverlay.clearTileCache();
        }
    }

}