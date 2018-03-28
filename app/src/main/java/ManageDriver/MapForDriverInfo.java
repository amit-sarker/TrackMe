package ManageDriver;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.amit.trackmeNew.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

//this class is called from the HeatmapsActivity, and this will return the map
public abstract class MapForDriverInfo extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    protected int getLayoutId() {
        return R.layout.activity_map_for_driver_info;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setUpMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (mMap != null) {
            return;
        }
        mMap = map;
        startDemo();
    }

    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapdriverinfo)).getMapAsync(this);
    }

    protected abstract void startDemo();

    protected GoogleMap getMap() {
        return mMap;
    }
}