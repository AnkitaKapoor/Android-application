package com.example.sengloke.InfoGo.places;

import android.app.Fragment;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sengloke.InfoGo.R;
import com.example.sengloke.InfoGo.util.Constants;
import com.example.sengloke.InfoGo.util.FetchAddressIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

/**
 * Created by MOY_0 on 30-Sep-16.
 */

public class placesFragment extends Fragment  implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks

{
    private GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "itemPlaces-activity";
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    protected static final String LOCATION_PLACES_KEY = "location-places";
    private static final String OTHER_OPTIONS = "options";


    private Button showPlace;
    private TextView showPlaceTv;
    private TextView showAddressTv;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    protected boolean mAddressRequested;
    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */

    /**
     * The formatted location address.
     */
    protected String mAddressOutput;
    protected  String mPlacesOutput;
    ProgressBar mProgressBar;


    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    private static View view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.places_item, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return view;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showPlace = (Button)view.findViewById(R.id.showPlaces);
        showPlaceTv = (TextView) view.findViewById(R.id.textView2);
        showAddressTv = (TextView) view.findViewById(R.id.textView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        mResultReceiver = new AddressResultReceiver(new Handler());

        // Set defaults, then update using values stored in the Bundle.
        mAddressRequested = false;
        mAddressOutput = "";
        mPlacesOutput="";
        updateValuesFromBundle(savedInstanceState);
        updateUIWidgets();



        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();
    }



        @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // showLatlonTv.setText(mLastLocation.toString());
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(getActivity(), R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                startIntentService();
            }
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();


        showPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // We only start the service to fetch the address if GoogleApiClient is connected.
                if (mGoogleApiClient.isConnected() && mLastLocation != null) {
                    startIntentService();
                }
                // If GoogleApiClient isn't connected, we process the user's request by setting
                // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
                // fetch the address. As far as the user is concerned, pressing the Fetch Address button
                // immediately kicks off the process of getting the address.
                mAddressRequested = true;
                updateUIWidgets();

                getDescreptionPlace();

            }
        });
    }

    @Override
    public void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        getActivity().startService(intent);
    }


    //stuff for fetching the adress for given cordinates

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in WifiActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                //   showToast(getString(R.string.address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
            updateUIWidgets();
        }
    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
        showAddressTv.setText(mAddressOutput);
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    private void updateUIWidgets() {
        if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            showPlace.setEnabled(false);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
            showPlace.setEnabled(true);
        }
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
                Bundle bundle = savedInstanceState.getBundle(OTHER_OPTIONS);
                mPlacesOutput= bundle.getString(LOCATION_PLACES_KEY);
                showPlaceTv.setText(mPlacesOutput);
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save whether the address has been requested.
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);

        // Save the address string.
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);

        Bundle bundle = new Bundle();
        bundle.putString(LOCATION_PLACES_KEY, mPlacesOutput);
        savedInstanceState.putBundle(OTHER_OPTIONS, bundle);
        super.onSaveInstanceState(savedInstanceState);
    }



    public void getDescreptionPlace()
    {

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace( mGoogleApiClient, null );
        result.setResultCallback( new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult( PlaceLikelihoodBuffer likelyPlaces ) {
                if (likelyPlaces.getStatus().isSuccess()) {

                    PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                    String content = "";
                    if (placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty(placeLikelihood.getPlace().getName()))
                        content = "Most likely place: " + placeLikelihood.getPlace().getName() + "\n";
                    if (placeLikelihood != null)
                        content += "Percent chance of being there: " + (int) (placeLikelihood.getLikelihood() * 100) + "%\n\n\n Address: \n";
                    mPlacesOutput=content;
                    showPlaceTv.setText(content);
                }
                else {
                    //Toast.makeText(getActivity(), "No places found ", Toast.LENGTH_SHORT).show();
                    mPlacesOutput="No places were Found for the Current Location\n\n\n"+
                            "but we did find the Address....\n\n";
                    showPlaceTv.setText(mPlacesOutput);
                }

                likelyPlaces.release();

            }
        });
    }



}
