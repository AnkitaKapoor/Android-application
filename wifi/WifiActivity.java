/* From the book by Reto Meier, modified by S.W. Loke, 2013
 */
package com.example.sengloke.InfoGo.wifi;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sengloke.InfoGo.BaseActivity;
import com.example.sengloke.InfoGo.R;
import com.example.sengloke.InfoGo.data.Wifi;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WifiActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "TAG";
    BroadcastReceiver r;
    private double lat = 0.0;
    private double lng = 0.0;
    private DatabaseReference mDatabase;
    private String found;
    private List<ScanResult> results;
    private String res;
    private Firebase ref;
    private int ctr = 0;
    private boolean isInstance = false;
    private ArrayList<String> old_scans;
    private ScanResult bestSignal;
    private ListView lv;
    private Wifi w;
	private  ArrayAdapter adapter;
	private String LIST="items";
    private WifiManager wifi;
    private TextView wifiResult;


    private Geocoder geocoder;
    private List<Address> addresses;
    private String address;
    protected GoogleApiClient mGoogleApiClient;

    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        setContentView(R.layout.main);
		old_scans = new ArrayList<>();
		lv = (ListView) findViewById(R.id.listid);
        wifiResult = (TextView) findViewById(R.id.wifiresult);

        if(savedInstanceState!=null)
        {
            lv.setAdapter(null);
            old_scans = savedInstanceState.getStringArrayList(LIST);
            adapter = new ArrayAdapter(WifiActivity.this, android.R.layout.simple_list_item_1, old_scans);
            lv.setAdapter(adapter);
            isInstance= true;

        }
		

        Firebase.setAndroidContext(this);
        ref = new Firebase("https://infogo-56377.firebaseio.com/wifiScan");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final TextView mymsg;
        // mymsg = (TextView) findViewById(R.id.msg);


        System.out.println("hello1");
        Log.v("test", "here");
        /**
         * Listing 16-14: Accessing the Wi-Fi Manager
         */
        String service = Context.WIFI_SERVICE;
         wifi = (WifiManager) getSystemService(service);

        System.out.println("hello2");
        /**
         * Listing 16-15: Monitoring and changing Wi-Fi state
         */
        if (!wifi.isWifiEnabled())
            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifi.setWifiEnabled(true);

        System.out.println("hello3");
        /**
         * Listing 16-16: Querying the active network connection
         */
        WifiInfo info = wifi.getConnectionInfo();
        if (info.getBSSID() != null) {
            System.out.println("hello4");
            int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            int speed = info.getLinkSpeed();
            String units = WifiInfo.LINK_SPEED_UNITS;
            String ssid = info.getSSID();

            String cSummary = String.format("Connected to %s at %s%s. " +
                            "Strength %s/5\n" + "Rssi:%s\n",
                    ssid, speed, units, strength, info.getRssi());
            Log.d(TAG, cSummary);


        }
        /**
         * Listing 16-17: Conducting a scan for Wi-Fi access points
         */
        // Register a broadcast receiver that listens for scan results.

        r = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                results = wifi.getScanResults();

                if (ctr < 1 & !isInstance) {
                    writeNewScanResult();
                    showDialog();
                    ctr++;
                }

                System.out.println("hello5");

                bestSignal = null;
                found = new String("SCAN RESULT:\n");
                for (ScanResult result : results) {
                    found = found.concat(new String("\n" + "SSID:" + result.SSID + "  BSSID:" + result.BSSID + "  RSSI:" + result.level + "\n" + "__________________________________"));

                    if (bestSignal == null ||
                            WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
                        bestSignal = result;
//            writeNewScanResult();

                }

                String connSummary = String.format("\n%s networks found.\n" + "%s(%s) is" + " the strongest. \nEst. distance from it is %sm. Latitude is %f longitude is %f",
                        results.size(), bestSignal.SSID, bestSignal.BSSID, calculateDistance(bestSignal.level, bestSignal.frequency),lat,lng);

                //       mymsg.setText(found.concat(connSummary));

                String wifiResultString = " Number of Wi-Fi AP found: " + results.size() + "\n The Strongers Signal Found was: " + bestSignal.SSID + "\n SSID: " +bestSignal.BSSID
                        + "\n Distance to Strongest AP: " +calculateDistance(bestSignal.level, bestSignal.frequency)+" Meters";
                wifiResult.setText(wifiResultString);


            }
        };
        registerReceiver(r, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Initiate a scan.
        wifi.startScan();


    }//oncreate




	 @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // All Parcelable objects of the API  (eg. LatLng, MarkerOptions, etc.) can be set
        // directly in the given Bundle.
        outState.putStringArrayList(LIST, old_scans);

    }


    private void writeNewScanResult() {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
//ref=mDatabase.child("wifiScan");

        res = "";
        if (results != null) {
            for (ScanResult r : results) {
                res += r.SSID + " " + r.BSSID + "\n";
            }

        } else {
            Log.i("null!!!", "null");
        }
        BaseActivity x = new BaseActivity();
        String userId = x.getUid();
        //String key = mDatabase.child("location").push().getKey();
        w = new Wifi(userId, address, res);
        Map<String, Object> postValues = w.toMap();
        mDatabase.child("wifiScan").push().setValue(postValues);
    }



    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(r); // be "polite", do the right thing and unregister when this we quite this app
        Log.d("done:", "In the onDestroy() event");
    }




    // Formula from http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
    // but formula is for free space; so, likely not accurate due to obstructions
    public double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) - levelInDb) / 20.0;
        return Math.pow(10.0, exp);
    }



    public void showDialog() {
        old_scans.clear();
        lv.setAdapter(null);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("VALUEEE IS:---> ");
                //System.out.println(snapshot.getValue());
                BaseActivity x = new BaseActivity();
                String userId = x.getUid();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Wifi post = postSnapshot.getValue(Wifi.class);
                    System.out.println(post.getResult());
                    if (post.getUser().equals(userId)) {
                        old_scans.add(post.getResult());
                    }
                }
                if (old_scans.size() > 0) {
                    adapter = new ArrayAdapter(WifiActivity.this, android.R.layout.simple_list_item_1, old_scans);
                    lv.setAdapter(adapter);
                } else {
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

    }


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
            lat = mLastLocation.getLatitude();
            Log.i("latitude is-!-!-!--->: ", Double.toString(lat));

            lng = mLastLocation.getLongitude();
            Log.i("latitude is:-!--!-!-> ", Double.toString(lng));


            //call a method for reverse geocoding
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

    public void reverseGeocode() {
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);// Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getLocality();
        } catch (Exception e) {
        }


    }
}