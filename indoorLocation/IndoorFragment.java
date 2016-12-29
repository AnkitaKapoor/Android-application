package com.example.sengloke.InfoGo.indoorLocation;

/**
 * Created by MOY_0 on 23-Oct-16.
 */


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class IndoorFragment extends Fragment {

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
    private TextView txtWifiResult;
    private ImageView roomImage;
    private Button buttonStartTracking;
    private Button buttonStopTraking;
    private boolean stopPressed= false;

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
            view = inflater.inflate(R.layout.indoor_location_activity, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return view;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtWifi = (TextView) view.findViewById(R.id.txtWifi);
        roomImage  = (ImageView) view.findViewById(R.id.bgmap);
        buttonStartTracking = (Button) view.findViewById(R.id.buttonStartTrack);
        buttonStopTraking = (Button) view.findViewById(R.id.buttonStopTrack);

        roomImage.setImageResource(R.drawable.bgmap);

        Firebase.setAndroidContext(getActivity());
        ref = new Firebase("https://infogo-56377.firebaseio.com/wifiScan");

        mDatabase = FirebaseDatabase.getInstance().getReference();




        String service = Context.WIFI_SERVICE;
        final WifiManager wifi = (WifiManager) super.getActivity().getSystemService(service);


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
                txtWifiResult.setText(connSummary + "\n"  );
                if(!stopPressed) {
                    if (bestSignal.BSSID.equals("50:87:89:ca:4a:ce") || bestSignal.BSSID.equals("50:87:89:ca:4a:cf")) {
                        txtWifi.setText("You are in BG 114");
                        roomImage.setImageResource(R.drawable.bg114);
                    } else if (bestSignal.BSSID.equals("10:bd:18:c6:fd:c1") || bestSignal.BSSID.equals("10:bd:18:c6:fd:c2") || bestSignal.BSSID.equals("10:bd:18:c6:fd:c3")) {
                        txtWifi.setText("You are in BG 115");
                        roomImage.setImageResource(R.drawable.bg115);
                    } else {
                        txtWifi.setText("out of range");
                        roomImage.setImageResource(R.drawable.unknown);
                    }


                }

            }
        };
        super.getActivity().registerReceiver(r, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));



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
        super.getActivity().unregisterReceiver(r);
        Log.d("done:", "In the onDestroy() event");
    }




    // Formula from http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
    // but formula is for free space; so, likely not accurate due to obstructions
    public double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) - levelInDb) / 20.0;
        return Math.pow(10.0, exp);
    }

}