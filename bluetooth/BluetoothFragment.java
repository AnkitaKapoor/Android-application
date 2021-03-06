package com.example.sengloke.InfoGo.bluetooth;


import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sengloke.InfoGo.BaseActivity;
import com.example.sengloke.InfoGo.R;
import com.example.sengloke.InfoGo.data.Bluetooth;
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

public class BluetoothFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private DatabaseReference mDatabase;
    private Firebase ref;
    public BluetoothAdapter mBluetoothAdapter = null;
    private int ENABLE_BLUETOOTH = 2;
    private double lat=0.0;
    private double lng=0.0;
    private ArrayList<String> scans=new ArrayList<>();
    private ListView lv;
    private BroadcastReceiver discoveryResult;
    private BluetoothDevice remoteDevice;
    private int ctr=0;
    private ArrayList<String> items;
    private Bluetooth b;
    private View rootView;
    private ArrayAdapter adapter;
    private String LIST="items";
	private GoogleApiClient mGoogleApiClient;
	private String address;
	private Geocoder geocoder;
    private List<Address> addresses;
	
	public BluetoothFragment()
	{
	}

	public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
		   buildGoogleApiClient();

    }

/*
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // All Parcelable objects of the API  (eg. LatLng, MarkerOptions, etc.) can be set
        // directly in the given Bundle.
        outState.putStringArrayList(LIST, items);

    }
    */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        if(rootView!=null)
        {
            ViewGroup parent = (ViewGroup)rootView.getParent();
            if(parent!=null)
            {parent.removeView(rootView);}
        }

	    rootView = inflater.inflate(R.layout.activity_bluetooth, container, false);
        lv=(ListView)rootView.findViewById(R.id.listid);
        items= new ArrayList<>();
/*
        if(savedInstanceState!=null)
        {
            items = savedInstanceState.getStringArrayList(LIST);
            adapter = new ArrayAdapter(rootView.getContext(), android.R.layout.simple_list_item_1, items);
            lv.setAdapter(adapter);

        }
*/
        Firebase.setAndroidContext(getActivity());
        mDatabase= FirebaseDatabase.getInstance().getReference();
        ref = new Firebase("https://infogo-56377.firebaseio.com/bluetoothScan");



        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initBluetooth();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();

        //ArrayList<String>items=new ArrayList<>();


          discoveryResult = new BroadcastReceiver() {


            public void onReceive(Context context, Intent intent) {

                String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BluetoothActivity--->>>", "");
                Toast.makeText(getActivity(), remoteDevice.getName().toString(), Toast.LENGTH_LONG).show();
                writeNewScanResult();
                showScans();

            }
        };
        super.getActivity().registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));


        return rootView;

    }// oncreateView

        public void writeNewScanResult() {
            {Log.i("Lets write result!!!","result");}
            BaseActivity x = new BaseActivity();
            String userId = x.getUid();
            //String key = mDatabase.child("location").push().getKey();
            b = new Bluetooth(userId,address,remoteDevice.getName().toString());
            Map<String, Object> postValues = b.toMap();
            mDatabase.child("bluetoothScan").push().setValue(postValues);


        }

    private void showScans() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                items.clear();
                lv.setAdapter(null);

                System.out.println("VALUEEE IS in bluefrag:---> ");
                BaseActivity x = new BaseActivity();
                String userId = x.getUid();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Bluetooth post = postSnapshot.getValue(Bluetooth.class);
                    System.out.println(post.getResult());
                    if (post.getUser().equals(userId)) {
                        items.add(post.getResult());
                    }
                }
                if (items.size() > 0) {
                    adapter = new ArrayAdapter(rootView.getContext(), android.R.layout.simple_list_item_1, items);
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

        if(mBluetoothAdapter.isDiscovering())
        { mBluetoothAdapter.cancelDiscovery();}
      //  super.getActivity().unregisterReceiver(discoveryResult);
    }

    private void initBluetooth() {
        if (!mBluetoothAdapter.isEnabled())        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
        } else

        {
            //initBluetoothUI();
            Log.i("already done", "already done");
            // mBluetoothAdapter.startDiscovery();
        }

    }



    public void onDestroy()
    {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        super.getActivity().unregisterReceiver(discoveryResult);

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