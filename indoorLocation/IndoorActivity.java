/* From the book by Reto Meier, modified by S.W. Loke, 2013
 */
package com.example.sengloke.InfoGo.indoorLocation;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sengloke.InfoGo.R;
import com.example.sengloke.InfoGo.data.Wifi;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class IndoorActivity extends Activity {

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
    private ScanResult bestSignal;


    private Wifi w;
	private  ArrayAdapter adapter;
	private String LIST="items";
    private WifiManager wifi;



    private TextView txtWifi;
    private TextView txtLocator;
    private ImageView roomImage;
    private Button buttonStartTracking;
    private Button buttonStopTraking;
    private boolean stopPressed= false;
    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.indoor_location_activity);

        txtWifi = (TextView) findViewById(R.id.txtWifi);
        roomImage  = (ImageView) findViewById(R.id.bgmap);
        buttonStartTracking = (Button) findViewById(R.id.buttonStartTrack);
        buttonStopTraking = (Button) findViewById(R.id.buttonStopTrack);

        roomImage.setImageResource(R.drawable.bgmap);

        Firebase.setAndroidContext(this);
        ref = new Firebase("https://infogo-56377.firebaseio.com/wifiScan");

        mDatabase = FirebaseDatabase.getInstance().getReference();




        String service = Context.WIFI_SERVICE;
         wifi = (WifiManager) getSystemService(service);


        if (!wifi.isWifiEnabled())
            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifi.setWifiEnabled(true);


        WifiInfo info = wifi.getConnectionInfo();
        if (info.getBSSID() != null) {
            int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            int speed = info.getLinkSpeed();
            String units = WifiInfo.LINK_SPEED_UNITS;
            String ssid = info.getSSID();
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
                    //writeNewScanResult();
                  //  showDialog();
                    ctr++;
                }


                bestSignal = null;
                found = new String("SCAN RESULT:\n");
                for (ScanResult result : results) {
                    found = found.concat(new String("\n" + "SSID:" + result.SSID + "  BSSID:" + result.BSSID + "  RSSI:" + result.level + "\n" + "__________________________________"));

                    if (bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0) {

                        bestSignal = result;

                    }

                }

                String connSummary = String.format("\n%s networks found.\n" + "(%s) is" + " the strongest", results.size(), bestSignal.BSSID);
               // String connSummary2 = String.format("\n%s networks found.\n" + "(%s) is" + " the strongest", results.size(), bestSignal.BSSID);
                if(!stopPressed) {
                    if (bestSignal.BSSID.equals("c4:0a:cb:a0:f0:d1") || bestSignal.BSSID.equals("c4:0a:cb:a0:e7:80")) {
                        txtWifi.setText("BG114");
                        roomImage.setImageResource(R.drawable.bg114);
                    }
                    else if (bestSignal.BSSID.equals("c4:0a:cb:a0:e0:4e") || bestSignal.BSSID.equals("c4:0a:cb:a0:e0:41") || bestSignal.BSSID.equals("c4:0a:cb:a0:e0:4f")) {
                        txtWifi.setText("You are in BG 115");
                        roomImage.setImageResource(R.drawable.bg115);
                    }
                    else if (bestSignal.BSSID.equals("c4:0a:cb:a1:24:f3") || bestSignal.BSSID.equals("c4:0a:cb:a1:24:f0") || bestSignal.BSSID.equals("c4:0a:cb:a1:24:f1")) {
                        txtWifi.setText("BG116");
                        roomImage.setImageResource(R.drawable.bg116);
                    }

                    else {
                        txtWifi.setText("This location is unkown");
                        roomImage.setImageResource(R.drawable.unknown);
                    }


                }

            }
        };
        registerReceiver(r, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));



        buttonStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initiate a scan.
                wifi.startScan();
                stopPressed = false;
            }
        });


        buttonStopTraking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initiate a scan.
              //  unregisterReceiver(r);
                roomImage.setImageResource(R.drawable.bgmap);
                stopPressed = true;

            }
        });


    }


	 @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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

}