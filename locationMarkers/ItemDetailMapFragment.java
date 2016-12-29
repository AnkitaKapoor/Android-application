package com.example.sengloke.InfoGo.locationMarkers;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sengloke.InfoGo.BaseActivity;
import com.example.sengloke.InfoGo.R;
import com.example.sengloke.InfoGo.data.MapLocation;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
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
 * A fragment that launches other parts of the demo application.
 */
public class ItemDetailMapFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback{

    private MapView mapView;
    //private GoogleMap googleMap;
    private static View view;



    private static final String MARKER_POSITION = "markerPosition";
    private static final String MARKER_INFO = "markerInfo";
    private static final String LOCATION_INFO = "locationInfo";   
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
	

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.map_fragment, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return view;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.mapfragment);
        //fragment.getMapAsync(this);

        mapView = (MapView) view.findViewById(R.id.mapfragment);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);


        Firebase.setAndroidContext(getActivity());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        dbref = new Firebase("https://infogo-56377.firebaseio.com/location");

        addLocationButton = (Button) view.findViewById(R.id.addLocationButton);

		  readLocation();

        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        enableMyLocation();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.814251,144.963169 ), 6.0f)); // 10 is zoom level
        if (!mMoveCameraToMarker) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.814251,144.963169 ), 6.0f));
            map.animateCamera(CameraUpdateFactory.newLatLng(mMarkerPosition));
            addMyMarker(mMap, "green");
        }

	   
    }

    

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }
	


   public void addMyMarker(GoogleMap map, String color) {


        MarkerOptions mo = new MarkerOptions();
        mo.title("Marker");

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



    }
	
	
	
	
    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
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
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        Log.i("latitude is in resume: ", Double.toString(lat));
        lng=location.getLongitude();
        Log.i("latitude is in resume: ", Double.toString(lng));
        loc = new LatLng(lat, lng);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    public void onDestroy() {
        super.onDestroy();
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