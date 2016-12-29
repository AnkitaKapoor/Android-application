package com.example.sengloke.InfoGo.locationMarkers;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sengloke.InfoGo.BaseActivity;
import com.example.sengloke.InfoGo.ItemDetailFragment;
import com.example.sengloke.InfoGo.ItemListActivity;
import com.example.sengloke.InfoGo.PermissionUtils;
import com.example.sengloke.InfoGo.R;
import com.example.sengloke.InfoGo.data.MapLocation;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.*/

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ItemDetailFragment}.
 */

public class ItemDetailMapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
                                                                        GoogleApiClient.OnConnectionFailedListener,
                                                                        LocationListener,
                                                                        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{
    private static final String MARKER_POSITION = "markerPosition";
    private static final String MARKER_INFO = "markerInfo";
    private static final String LOCATION_INFO = "locationInfo";    // Bundle keys.
    private static final String OTHER_OPTIONS = "options";


    private LatLng mMarkerPosition;
    private boolean mMoveCameraToMarker = false;
    // this double are for storing the lat and long before assigning to loc
    private double lat=0.0;
    private double lng=0.0;
    //this is the variable of the type LatLng that we need for storing lat and lng.
    //we will use also for saving the instance
    LatLng loc;
    LatLng storedLoc;
    //this array is for storing the locations saved inn the database
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private boolean isLocationSaved= false;



    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private MapFragment myMapFragment;
    private LocationRequest mLocationRequest;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;




    //Database Declaration
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private Firebase dbref;

    private Button addLocationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_item_detail);

        Firebase.setAndroidContext(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        dbref = new Firebase("https://infogo-56377.firebaseio.com/location");


        setUpMapIfNeeded();
        readLocation();
        addLocationButton = (Button) findViewById(R.id.addLocationButton);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        mMoveCameraToMarker = true;
        mMarkerPosition = new LatLng(-37.814251,144.963169);


        if( savedInstanceState == null ) {
            mMoveCameraToMarker = true;
        }
        else
        {
            // Extract the state of the MapFragment:
            // - Objects from the API (eg. LatLng, MarkerOptions, etc.) were stored directly in
            //   the savedInsanceState Bundle.
            // - Custom Parcelable objects were wrapped in another Bundle.
            Toast.makeText(this, "extracting instance", Toast.LENGTH_SHORT).show();
            mMarkerPosition = savedInstanceState.getParcelable(MARKER_POSITION);
            Bundle bundle = savedInstanceState.getBundle(OTHER_OPTIONS);
            loc = bundle.getParcelable(LOCATION_INFO);
            mMoveCameraToMarker = false;
        }


        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLocationSaved)
                {
                    isLocationSaved= true;
                    writeNewLocation();
                    addMyMarker(mMap, "green");

                }


            }
        });


    }







    private void setUpMapIfNeeded() {

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;
        enableMyLocation();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.814251,144.963169 ), 6.0f)); // 10 is zoom level

        if (!mMoveCameraToMarker) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.814251,144.963169 ), 6.0f));
            map.animateCamera(CameraUpdateFactory.newLatLng(mMarkerPosition));
            //addMyMarker(mMap, "red");
        }

    }



    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }




    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }




    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        Log.i("latitude is in resume: ", Double.toString(lat));
        lng=location.getLongitude();
        Log.i("latitude is in resume: ", Double.toString(lng));
        loc = new LatLng(lat, lng);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lat=mLastLocation.getLatitude();
            Log.i("latitude is: ",Double.toString(lat));

            lng=mLastLocation.getLongitude();
            Log.i("latitude is: ", Double.toString(lng));
            loc = new LatLng(lat, lng);
        }
        else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }




    public void addMyMarker(GoogleMap map, String color) {


        MarkerOptions mo = new MarkerOptions();


        if(color.equals("red"))
        {
            mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mo.position(storedLoc);
            mo.title(storedLoc.toString());
        }
        else
        {
            //Toast.makeText(this, "else part of the iquals for color ", Toast.LENGTH_SHORT).show();
            mo.position(loc);
            mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mo.zIndex(mo.getZIndex()+1);
            mo.title("Last Recorded Location..." + loc.toString());

        }
            map.addMarker(mo);

        Log.i("insert","marker added on map!!!!!!!");

    }





    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        }
        setUpMapIfNeeded();
    //    Toast.makeText(this, "on Resume length"+ latlngs.size(), Toast.LENGTH_SHORT).show();

    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    protected void onStart() {
        super.onStart();
       mGoogleApiClient.connect();
     //   writeNewLocation();
      //  readLocation();
  //      Toast.makeText(this, "length"+ latlngs.size(), Toast.LENGTH_SHORT).show();

    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // All Parcelable objects of the API  (eg. LatLng, MarkerOptions, etc.) can be set
        // directly in the given Bundle.
        outState.putParcelable(MARKER_POSITION, mMarkerPosition);

        // All other custom Parcelable objects must be wrapped in another Bundle. Indeed,
        // failing to do so would throw a ClassNotFoundException. This is due to the fact that
        // this Bundle is being parceled (losing its ClassLoader at this time) and unparceled
        // later in a different ClassLoader.
       Bundle bundle = new Bundle();
       bundle.putParcelable(LOCATION_INFO, loc);
       outState.putBundle(OTHER_OPTIONS, bundle);

    }



    public void  writeNewLocation(){

        BaseActivity x = new BaseActivity();
        String userId = x.getUid();
        Date currentDate = new Date(System.currentTimeMillis());
        String key = mDatabase.child("location").push().getKey();
        MapLocation maploc = new MapLocation(userId,loc.latitude,loc.longitude,currentDate.toString() );
        Map<String, Object> postValues = maploc.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/location/" + key, postValues);
        childUpdates.put("/user-location/" + userId + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
        mDatabase.child("location").push().setValue(postValues);
        latlngs.add(loc);

     }


    public void readLocation()
    {
        BaseActivity x = new BaseActivity();
        final String userId = x.getUid();
       // Firebase locRef = dbref.child("location");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren() ){
                    if(!isLocationSaved) {

                        MapLocation ml = data.getValue(MapLocation.class);
                        if(ml.username.equals(userId)) {
                            storedLoc = new LatLng(ml.lati, ml.longi);
                            latlngs.add(storedLoc);
                            addMyMarker(mMap, "red");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }








}
