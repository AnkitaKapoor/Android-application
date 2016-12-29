/* From the book by Reto Meier, modified by S.W. Loke, 2013
 */
package com.example.sengloke.InfoGo.wifi;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class WifiFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final String TAG = "TAG";
  BroadcastReceiver r;
  private double lat = 0.0;
  private double lng = 0.0;
  private DatabaseReference mDatabase;
    private String found;
    private List<ScanResult> results;
    private String res;
    private  Firebase ref;
private int ctr=0;
private ArrayList<String> old_scans=new ArrayList<>();
  private  ScanResult bestSignal;
private ListView lv;
    private Wifi w;
   private View rootView;
    private GoogleApiClient mGoogleApiClient;
    private String address;
    private Geocoder geocoder;
    private List<Address> addresses;
    private TextView wifiResult;



    public WifiFragment() {

    }
  
  
  @Override
    public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
//      Firebase.setAndroidContext(this);
 buildGoogleApiClient();
  }
	
public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
 {

     if(rootView!=null)
     {
         ViewGroup parent = (ViewGroup)rootView.getParent();
         if(parent!=null)
         {parent.removeView(rootView);}
     }

    rootView = inflater.inflate(R.layout.main, container, false);
    
//	setContentView(R.layout.main);
    Firebase.setAndroidContext(getActivity());
   //works-->  Firebase.setAndroidContext(super.getActivity());

    ref = new Firebase("https://infogo-56377.firebaseio.com/wifiScan");

    mDatabase=FirebaseDatabase.getInstance().getReference();

    Log.i("STARTED11111","STARTED!!!!!!!!!!!!");
    LocationManager locationManager = (LocationManager) super.getActivity().getSystemService(Context.LOCATION_SERVICE);

    Criteria criteria = new Criteria();
    String provider = locationManager.getBestProvider(criteria, false);

    Location location = locationManager.getLastKnownLocation(provider);
	if(location!=null)
    {
		lat=location.getLatitude();
        lng=location.getLongitude();
	}
    Log.i("ended","ENDEDDDDDDDDDDDDD");

    final TextView mymsg;
   // mymsg = (TextView) findViewById(R.id.msg);


    System.out.println("hello1");
    Log.v("test", "here");
    /**
     * Listing 16-14: Accessing the Wi-Fi Manager
     */
    String service = Context.WIFI_SERVICE;
    final WifiManager wifi = (WifiManager) super.getActivity().getSystemService(service);

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

          if(ctr<1) {
              writeNewScanResult();
              showDialog();
              ctr++;
          }

        System.out.println("hello5");

        bestSignal = null;found = new String("SCAN RESULT:\n");
        for (ScanResult result : results) {
         found = found.concat(new String("\n" + "SSID:" + result.SSID + "  BSSID:" + result.BSSID + "  RSSI:" + result.level+"\n"+"__________________________________"));

          if (bestSignal == null ||
                  WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
            bestSignal = result;
//            writeNewScanResult();

        }

        String connSummary = String.format("\n%s networks found.\n" + "%s(%s) is" + " the strongest. \nEst. distance from it is %sm. Latitude is %f longitude is %f",
                results.size(), bestSignal.SSID, bestSignal.BSSID, calculateDistance(bestSignal.level, bestSignal.frequency), lat,lng);

   //       mymsg.setText(found.concat(connSummary));
        Toast.makeText(getActivity(), connSummary, Toast.LENGTH_LONG).show();



      }
              };
     super.getActivity().registerReceiver(r, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    // Initiate a scan.
    wifi.startScan();

     return rootView;
  }//on createview



    private void writeNewScanResult() {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
//ref=mDatabase.child("wifiScan");

res="";
        if(results!=null) {
            for (ScanResult r : results)
            {res+=r.SSID+" "+r.BSSID+"\n";}

        }
        else
        {Log.i("null!!!","null");}
        BaseActivity x = new BaseActivity();
        String userId = x.getUid();
        //String key = mDatabase.child("location").push().getKey();
         w = new Wifi(userId,address,res);
        Map<String, Object> postValues = w.toMap();
        mDatabase.child("wifiScan").push().setValue(postValues);
           }

    public void onDestroy() {
    super.onDestroy();
    super.getActivity().unregisterReceiver(r); // be "polite", do the right thing and unregister when this we quite this app
    Log.d("done:", "In the onDestroy() event");
  }

  // Formula from http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
  // but formula is for free space; so, likely not accurate due to obstructions
  public double calculateDistance(double levelInDb, double freqInMHz) {
    double exp = (27.55 - (20 * Math.log10(freqInMHz)) - levelInDb) / 20.0;
    return Math.pow(10.0, exp);
  }


    //show data retrieved
  /*public void showData(View v)
  {
      showDialog();
  }
*/
  public void showDialog()
  {
     // Dialog d=new Dialog(this);
     // d.setTitle("Previous scans");
    //  d.setContentView(R.layout.dialoglayout_wifi);
      lv=(ListView)rootView.findViewById(R.id.listid);
      old_scans.clear();

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
                  if(post.getUser().equals(userId))
                  {old_scans.add(post.getResult());}
              }
              if (old_scans.size() > 0) {
                  ArrayAdapter adapter = new ArrayAdapter(rootView.getContext(),android.R.layout.simple_list_item_1, old_scans);
                //  ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, old_scans);

                  if(adapter!=null)
                      lv.setAdapter(adapter);
              } else {
                  Toast.makeText(getActivity(), "No scans found", Toast.LENGTH_SHORT).show();
              }

          }

          @Override
          public void onCancelled(FirebaseError firebaseError) {
              System.out.println("The read failed: " + firebaseError.getMessage());
          }

      });


  }

 	
 protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       // client.connect();
       mGoogleApiClient.connect();
     }

    @Override
    public void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();

    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {


        //get current location and fetch lat and lng
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lat=mLastLocation.getLatitude();
            Log.i("latitude is-!-!-!--->: ",Double.toString(lat));

            lng=mLastLocation.getLongitude();
            Log.i("latitude is:-!--!-!-> ", Double.toString(lng));
			
			
			//call a method for reverse geocoding reverseGeocode()
			reverseGeocode();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {


    }

    @Override
    public void onConnectionSuspended(int cause) {

 }
 
 
    public void reverseGeocode()
	{
		 geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);// Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0) +" "+ addresses.get(0).getLocality();
        } catch (Exception e) {
        }
 
	}

}