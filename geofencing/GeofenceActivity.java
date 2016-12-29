/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sengloke.InfoGo.geofencing;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.sengloke.InfoGo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * Demonstrates how to create and remove geofences using the GeofencingApi. Uses an IntentService
 * to monitor geofence transitions and creates notifications whenever a device enters or exits
 * a geofence.
 * <p/>
 * This sample requires a device's Location settings to be turned on. It also requires
 * the ACCESS_FINE_LOCATION permission, as specified in AndroidManifest.xml.
 * <p/>
 * Note that this Activity implements ResultCallback<Status>, requiring that
 * {@code onResult} must be defined. The {@code onResult} runs when the result of calling
 * {@link GeofencingApi#addGeofences(GoogleApiClient, GeofencingRequest, PendingIntent)}  addGeofences()} or
 * {@link GeofencingApi#removeGeofences(GoogleApiClient, List)}  removeGeofences()}
 * becomes available.
 */
public class GeofenceActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>,GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    protected static final String TAG = "GeofenceActivity";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The list of geofences used in this sample.
     */
    protected ArrayList<Geofence> mGeofenceList;

    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    /**
     * Used to persist application state about whether geofences were added.
     */
    private SharedPreferences mSharedPreferences;

    // Buttons for kicking off the process of adding or removing geofences.
    //private Button mAddGeofencesButton;
    //private Button mRemoveGeofencesButton;

    private GoogleMap mMap;
    private MapFragment myMapFragment;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //
    // private GoogleApiClient client;
            private boolean cam;
    private double lat;
    private double lng;
	private  LatLng loc;
	private String MARKER_POSITION="marker position";
    private Geocoder geocoder;
    private List<Address> addresses;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if( savedInstanceState == null ) {
            cam = true;
        }
        else
        {
            // Extract the state of the MapFragment:
            // - Objects from the API (eg. LatLng, MarkerOptions, etc.) were stored directly in
            //   the savedInsanceState Bundle.
            // - Custom Parcelable objects were wrapped in another Bundle.

            loc = savedInstanceState.getParcelable(MARKER_POSITION);
          cam=false;

        }
		
		

        if (mMap == null) {
            // Try to obtain the map
            Log.i("latitude is: ",Double.toString(lat));
            Log.i("longitude is: ", Double.toString(lng));

        }
        if(mMap==null)
        {Log.i("mMAP--> ","is still null");
        }


        // Get the UI widgets.
        //mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        //mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
      //  setButtonsEnabledState();

        // Get the geofences used. Geofence data is hard coded in this sample.
    //  populateGeofenceList();

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();

    }
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // All Parcelable objects of the API  (eg. LatLng, MarkerOptions, etc.) can be set
        // directly in the given Bundle.
        outState.putParcelable(MARKER_POSITION, loc);

    }
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       // client.connect();
       mGoogleApiClient.connect();
     }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();

    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

        Log.i(TAG, "Connected to GoogleApiClient");
        //get current location and fetch lat and lng
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lat=mLastLocation.getLatitude();
            Log.i("latitude is-!-!-!--->: ",Double.toString(lat));

            lng=mLastLocation.getLongitude();
            Log.i("latitude is:-!--!-!-> ", Double.toString(lng));
            loc=new LatLng(lat,lng);
            reverseGeocode();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
       Log.i(TAG, "Connection suspended");
 }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        populateGeofenceList();

        try {

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().

            Log.i("FOR YOUR INFORMATION-->","added marker here");
            addMyMarker(mMap);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
/*
    public void removeGeofencesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }
*/
    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     * <p/>
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            //mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.
            //setButtonsEnabledState();

            Toast.makeText(
                    this,
                    getString(R.string.geofences_added),
                    Toast.LENGTH_SHORT
            ).show();

/*
            Toast.makeText(
                    this,
                    getString(mGeofencesAdded ? R.string.geofences_added :
                            R.string.geofences_removed),
                    Toast.LENGTH_SHORT
            ).show();
*/
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }


    public void addMyMarker(GoogleMap map) {
        MarkerOptions mo = new MarkerOptions();
       //loc = new LatLng(lat, lng);
        mo.position(loc);
        mo.title("Marker");
        map.addMarker(mo);

        Log.i("insert","marker added on map!!!!!!!");
        // move focus to where the marker is
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12)); // 10 is zoom level
        //populateGeofenceList();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
     //   Log.i("pending intent added outside","PENDINGGGGGG");
        if (mGeofencePendingIntent != null) {
            Log.i("pending intent added","PENDINGGGGGG");

            return mGeofencePendingIntent;


        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        Log.i("pending intent--> ", String.valueOf(intent));

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    //it is called when addgeofence button is tapped
    public void populateGeofenceList() {
//        for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {
        Log.i("LATITUDE IN POP",String.valueOf(lat));
        Log.i("LONGGITUDE IN POP",String.valueOf(lng));
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.//here id is made from latlng got from onConnected
                .setRequestId(String.valueOf(new Random().nextInt(96) + 32))

                        // Set the circular region of this geofence.
                .setCircularRegion(
                        lat , lng , Constants.GEOFENCE_RADIUS_IN_METERS
                )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                .build());
  //      }
        Log.i("latitude is-----> ",Double.toString(lat));
        Log.i("longitude is:------> ",Double.toString(lng));
    }

    /**
     * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
     * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
     * user has added geofences.
     */
    /*
    private void setButtonsEnabledState() {
        if (mGeofencesAdded) {
            mAddGeofencesButton.setEnabled(false);
            mRemoveGeofencesButton.setEnabled(true);
        } else {
            mAddGeofencesButton.setEnabled(true);
            mRemoveGeofencesButton.setEnabled(false);
        }
    }
*/

/*

    myMapFragment.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

        @Override
        public void onMapClick(LatLng point) {
            // TODO Auto-generated method stub
            lstLatLngs.add(point);
            map.clear();
            map.addMarker(new MarkerOptions().position(point));
        }
    });
*/


/*
    @SuppressLint("NewApi")
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map
            Log.i("latitude is: ",Double.toString(lat));
            Log.i("longitude is: ",Double.toString(lng));

            myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mMap = myMapFragment.getMap();

           // mMap.setOnMarkerClickListener(this);

        }
        if(mMap==null)
        {Log.i("mMAP--> ","is still null");
        }
    }
*/

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;
        if (!cam)
        {  //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.814251,144.963169 ), 10.0f));
            //  mMap.setOnMyLocationButtonClickListener(this);
            // enableMyLocation();
             addMyMarker(mMap);
        }
    }


    public void reverseGeocode() {
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);// Here 1 represent max location result to returned, by documents it recommended 1 to 5
            Constants.place = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getLocality();
            Log.i("reversed address is: ",Constants.place);
        } catch (Exception e) {
        }


    }






}