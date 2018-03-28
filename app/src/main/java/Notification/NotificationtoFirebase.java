package Notification;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class NotificationtoFirebase {


    public NotificationtoFirebase() {
    }

    static final Double EARTH_RADIUS = 6371.00;

    private DatabaseReference mNotificationDatabase, mUserListDatabase, mUserNameDatabase,mOwnerList,mDriverList;

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

    //Adds a child "notification" under main project in firebase and updates the title, body, type of the notification and saves them
    public void setNotificationToFirebase(final String userId, final String type, final boolean isDriver, final double currentlat, final double currentlon) {
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        if (isDriver) {
            mUserListDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username");
        } else {
            mUserListDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("username");
        }
        if(type.equals("Emergency"))
        {

            mUserListDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for ( DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final String driverUserId = postSnapshot.child("UID").getValue().toString();

                        Log.wtf("print", "driver user id or ownerrrrrrrrrrrrrr   " + driverUserId);
                        if (driverUserId.equals(userId)) {

                        } else {
                            String lat = postSnapshot.child("Location").child("l").child("0").getValue().toString();
                            String lon = postSnapshot.child("Location").child("l").child("1").getValue().toString();
                            double latt = Double.parseDouble(lat);
                            double lonn = Double.parseDouble(lon);

                            double distance = CalculationByDistance(latt,lonn,currentlat,currentlon);

                            if(distance+1e-9<=20.0000 )
                            {
                                if (isDriver) {
                                    mUserNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userId);
                                } else {
                                    mUserNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userId);
                                }

                                mUserNameDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String currentUserName = dataSnapshot.child("username").getValue().toString();

                                        HashMap<String, String> notificationData = new HashMap<String, String>();

                                        notificationData.put("Title", "Emergency Help!!! ");
                                        notificationData.put("Body", currentUserName + " has asked for emergency help");
                                        notificationData.put("from", userId);
                                        notificationData.put("type", "Emergency");
                                        if(isDriver) notificationData.put("isdriver","true");
                                        else notificationData.put("isdriver","false");
                                        notificationData.put("extratype","1");

                                        mNotificationDatabase.child(driverUserId).push().setValue(notificationData);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }


                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(isDriver)
        {
            mOwnerList = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userId).child("OwnerList");
            mOwnerList.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final String ownerUserId = postSnapshot.child("UID").getValue().toString();


                        mUserNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(userId);
                        mUserNameDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String currentUserName = dataSnapshot.child("username").getValue().toString();

                                HashMap<String, String> notificationData = new HashMap<String, String>();

                                if (type.equals("Emergency")) {
                                    notificationData.put("Title", "Emergency Help!!! ");
                                    notificationData.put("Body", currentUserName + " has asked for emergency help");
                                    notificationData.put("from", userId);
                                    notificationData.put("type", "Emergency");
                                    notificationData.put("isdriver","true");
                                    notificationData.put("extratype","2");

                                } else if (type.equals("Status")) {
                                    notificationData.put("Title", "New Status ");
                                    notificationData.put("Body", currentUserName + " has posted a new update");
                                    notificationData.put("from", userId);
                                    notificationData.put("type", "Status");
                                    notificationData.put("isdriver","true");
                                    notificationData.put("extratype","3");
                                }
                                else if(type.equals("Roaming"))
                                {
                                    notificationData.put("Title", "New Area");
                                    notificationData.put("Body", currentUserName + " is roaming irregularly");
                                    notificationData.put("from", userId);
                                    notificationData.put("type", "Roaming");
                                    notificationData.put("isdriver","true");
                                    notificationData.put("extratype","4");
                                }
                                mNotificationDatabase.child(ownerUserId).push().setValue(notificationData);
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
        else
        {
            mDriverList = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userId).child("DriverList");
            mDriverList.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final String driverUserId = postSnapshot.child("UID").getValue().toString();
                        mUserNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Owners").child("UID").child(userId);
                        mUserNameDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String currentUserName = dataSnapshot.child("username").getValue().toString();

                                HashMap<String, String> notificationData = new HashMap<>();

                                if (type.equals("Emergency")) {
                                    notificationData.put("Title", "Emergency Help!!! ");
                                    notificationData.put("Body", currentUserName + " has asked for emergency help");
                                    notificationData.put("from", userId);
                                    notificationData.put("type", "Emergency");
                                    notificationData.put("isdriver","false");
                                } else if (type.equals("Status")) {
                                    notificationData.put("Title", "New Status ");
                                    notificationData.put("Body", currentUserName + " has posted a new update");
                                    notificationData.put("from", userId);
                                    notificationData.put("type", "Status");
                                    notificationData.put("isdriver","false");
                                }

                                mNotificationDatabase.child(driverUserId).push().setValue(notificationData);
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
    }
}


