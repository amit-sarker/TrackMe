package Maps;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.example.amit.trackmeNew.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import Direction.DirectionOnMap;
import NavigationDrawer.NavigationDriverActivity;
import Notification.NotificationActivity;
import Notification.NotificationtoFirebase;


public class DriverMapActivity extends NavigationDriverActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final Double EARTH_RADIUS = 6371.00;
    private static final int REQUEST_LOCATION = 0;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private static final String TAG = "";
    private GoogleMap mMap;
    private int markerCount;
    private FloatingActionButton mEmergency, mDirection, mNotification;

    private TextToSpeech locationAddress;
    private Timer timer = new Timer();

    private Location clusterLocation;

    boolean isGPSEnable = false;

    // flag for network status
    boolean isNetworkEnable = false;

    protected LocationManager locationManager;

    private ArrayList<LatLng> locationlistforcluster = new ArrayList<>();
    private FirebaseAuth mAuth;
    private double[] newItem;
    private DatabaseReference mAddLocation;
    private boolean isClustered = false;
    private static Set<LatLng> locationset = new HashSet<LatLng>();
    private String destination;
    private LatLng destinationLatLng;

    private void GetTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+6:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        date.setTimeZone(TimeZone.getTimeZone("GMT+6:00"));
        String localTime = date.format(currentLocalTime);
        int currentHour = cal.get(Calendar.HOUR);
        int currentMinutes = cal.get(Calendar.MINUTE);
        int currentSeconds = cal.get(Calendar.SECOND);
    }

    KMeansClustering kmeans = new KMeansClustering();
    double[][] means;
    boolean isRoamingNotification = false;
    long tStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.activity_driver_map);

        mEmergency = findViewById(R.id.emergency_btn);
        mNotification = findViewById(R.id.fab1);
        mDirection = findViewById(R.id.fab2);

        getLocation();

        locationAddress = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    locationAddress.setLanguage(Locale.UK);
                }
            }
        });
        markerCount = 0;

        //Check If Google Services Is Available
        if (getServicesAvailable()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        //Create The MapView Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DriverMapActivity.this, "Emergency Notification sent", Toast.LENGTH_SHORT).show();
                NotificationtoFirebase mNotificationtoFirebase = new NotificationtoFirebase();
                mNotificationtoFirebase.setNotificationToFirebase(userId, "Emergency", true, clusterLocation.getLatitude(), clusterLocation.getLongitude());

            }

        });


        mNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMapActivity.this, NotificationActivity.class);
                startActivity(intent);

            }
        });

        mDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMapActivity.this, DirectionOnMap.class);
                startActivity(intent);
                return;

            }
        });

        mAuth = FirebaseAuth.getInstance();

        //timer delay for speech
        int delay = 10000; // delay for 5 sec.
        int period = 10000 * 10; // repeat every  min.
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getAddress();
            }
        }, delay, period);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(destinationLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 17));
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

    }

    public boolean getFromMap = false;

    public void getLocation() {
        Location location;
        android.location.LocationListener listener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateFirebaseLocation(location);
                clusterLocation = location;

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {
            getFromMap = true;

        } else {

            if (isNetworkEnable) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, listener);
                if (locationManager != null) {

                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {

                        updateFirebaseLocation(location);
                        clusterLocation = location;
                    }
                }
            } else if (isGPSEnable) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
                if (locationManager != null) {

                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {

                        updateFirebaseLocation(location);
                        clusterLocation = location;
                    }
                }
            }

        }

        if (clusterLocation != null && isClustered) {
            double[] arr = new double[2];
            arr[0] = clusterLocation.getLatitude();
            arr[1] = clusterLocation.getLongitude();
            NewClassify(means, arr);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setPadding(0, 300, 0, 0);
        mMap.setMyLocationEnabled(true);
        displayLocation();
    }

    Marker mMarker = null;

    // Add A Map Pointer To The MAp
    public void addMarker(GoogleMap googleMap, double lat, double lon) {

        if (markerCount == 1) {
            animateMarker(mLastLocation, mMarker);
        } else if (markerCount == 0) {

            mMap = googleMap;

            LatLng latlong = new LatLng(lat, lon);
            mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16));

            markerCount = 1;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            mMap.setPadding(0, 300, 0, 0);
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    public boolean getServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {

            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot Connect To Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getServicesAvailable();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
            getLocation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getLocation();
    }

    //Method to display the location on UI
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {

            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                getLocation();
                //Add pointer to the map at location
                addMarker(mMap, latitude, longitude);

            } else {

            }
        }

        if (isClustered == false) {
            ReadFromFirebase();
        }
    }

    // Creating google api client object
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    //Creating location request object
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Starting the location updates
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public void updateFirebaseLocation(final Location location) {

        try {

            final String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            final DatabaseReference mLocationReference = FirebaseDatabase.getInstance().getReference().child("Location").child(driverId);

            mLocationReference.child("Usertype").setValue("Driver");
            GeoFire geoFire2 = new GeoFire(mLocationReference);
            geoFire2.setLocation("UserLocation", new GeoLocation(location.getLatitude(), location.getLongitude()));


            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(driverId).child("username");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try {
                        final String user_name = (String) dataSnapshot.getValue();
                        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(user_name);

                        GeoFire geoFire = new GeoFire(ref2);
                        geoFire.setLocation("Location", new GeoLocation(location.getLatitude(), location.getLongitude()));
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
        }
    }

    public void onLocationChanged(final Location location) {
        mLastLocation = location;
        getLocation();
        clusterLocation = location;

        displayLocation();
    }

    public static void animateMarker(final Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());
            final float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                    } catch (Exception ex) {
                    }
                }
            });
            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start;
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public void getAddress() {

        double lat = mLastLocation.getLatitude();
        double lng = mLastLocation.getLongitude();
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            String speech = obj.getAddressLine(0);

            Log.v("IGA", "Address" + add);
            locationAddress.speak(speech, TextToSpeech.QUEUE_FLUSH, null);

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    int treatPabo = 0;

    private void clustering() {
        double[][] items = new double[locationlistforcluster.size()][2];

        for (int i = 0; i < locationlistforcluster.size(); i++) {

            items[i][0] = locationlistforcluster.get(i).latitude;
            items[i][1] = locationlistforcluster.get(i).longitude;
        }
        if (locationlistforcluster.size() > 2) {

            kmeans.FindColMinMax(items);

            int k = 3;
            means = kmeans.CalculateMeans(k, items, 1000);
            isClustered = true;
        } else {
            if (locationlistforcluster.size() < 3) {
                try {
                    locationlistforcluster.add(new LatLng(clusterLocation.getLatitude(), clusterLocation.getLongitude()));

                    if (locationlistforcluster.size() != treatPabo) {

                        mAddLocation = FirebaseDatabase.getInstance().getReference().child("Clustering").child(mAuth.getCurrentUser().getUid().toString()).push();
                        GeoFire geoFire = new GeoFire(mAddLocation);
                        geoFire.setLocation("Location", new GeoLocation(clusterLocation.getLatitude(), clusterLocation.getLongitude()));

                    }
                    treatPabo = locationlistforcluster.size();
                } catch(NullPointerException e) {}
            }
        }
    }


    private double CalculationByDistance(double lat1, double lon1, double lat2, double lon2) {
        double Radius = EARTH_RADIUS;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }

    private void AddlocationforNewClassify(double lat, double lon) {
        double mini = 100000000.00;

        boolean isAdd = true;
        if (locationset.size() < 3) {
            for (int i = 0; i < locationlistforcluster.size(); i++) {
                locationset.add(locationlistforcluster.get(i));
            }
        }
        for (LatLng latLng : locationset) {
            double[] arr = new double[2];
            arr[0] = latLng.latitude;
            arr[1] = latLng.longitude;
            double distance = CalculationByDistance(lat, lon, arr[0], arr[1]);
            if (distance < mini) {
                mini = distance;
            }
        }

        if (mini < 0.250000 + 1e-9 || mini + 1e-9 >= 100000000.00) {
            isAdd = false;
        }

        if (isAdd == true) {
            locationset.add(new LatLng(lat, lon));

            mAddLocation = FirebaseDatabase.getInstance().getReference().child("Clustering").child(mAuth.getCurrentUser().getUid().toString()).push();
            GeoFire geoFire = new GeoFire(mAddLocation);
            geoFire.setLocation("Location", new GeoLocation(lat, lon));
        }

    }

    int NewClassify(double[][] means, double[] item) {
        double minimum = 100000000.00;
        int index = -1;
        for (int i = 0; i < means.length; i++) {
            double dis = CalculationByDistance(item[0], item[1], means[i][0], means[i][1]);

            if (dis < minimum) {
                minimum = dis;
                index = i;
            }
        }
        final double lat = item[0], lon = item[1];
        AddlocationforNewClassify(lat, lon);


        if (minimum > .50000 + 1e-9) {

            if (isRoamingNotification == true) {
                long tEnd = System.currentTimeMillis();
                long tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000.0;
                if(elapsedSeconds>20000)
                {
                    isRoamingNotification = false;
                }
            }

            //A notification of type irregular roaming will be updated in the database


            if (isRoamingNotification == false) {
                Log.wtf("notif", "Notification sent");
                Toast.makeText(this, "roaming notification", Toast.LENGTH_SHORT).show();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = currentUser.getUid().toString();
                NotificationtoFirebase mNotificationtoFirebase = new NotificationtoFirebase();

                mNotificationtoFirebase.setNotificationToFirebase(userId, "Roaming", true, lat, lon);
                tStart = System.currentTimeMillis();
                isRoamingNotification = true;
            }


        }

        return index;

    }


    private void ReadFromFirebase() {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("Clustering").child(mAuth.getCurrentUser().getUid().toString());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationlistforcluster.clear();

                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String lat = postSnapshot.child("Location").child("l").child("0").getValue().toString();
                        String lon = postSnapshot.child("Location").child("l").child("1").getValue().toString();

                        double latitude, longitude;
                        latitude = Double.parseDouble(lat);
                        longitude = Double.parseDouble(lon);

                        LatLng latLng = new LatLng(latitude, longitude);
                        locationlistforcluster.add(latLng);

                    }
                }
                clustering();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
