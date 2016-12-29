package com.example.sengloke.InfoGo.pictureTaking;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.sengloke.InfoGo.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private double latitude;
    private double longitude;
    private String title;
    private GoogleMap mMap;
    private DAOdb daOdb;
    private MyImage image;
   private List<MyImage> MyImages = new ArrayList<>();
    private MapView mMapView;
    protected static final String TAG = "MapsFragment";

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        /*
         SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        */
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        daOdb = new DAOdb(this);
        MyImages= new ArrayList<>();
        MyImages = daOdb.getLatLong();

        if(MyImages.size() >0) {
            for (int i = 0; i < MyImages.size(); i++) {
                String rat_values = MyImages.get(i).toString();
                String[] value_split = rat_values.split("~");
                latitude = Double.parseDouble(value_split[1]);
                longitude = Double.parseDouble(value_split[2]);
                title = value_split[0];
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(title).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon1)));
            }
        }
        else
        {
            mMap.addMarker(new MarkerOptions().position(new LatLng(0.0, 0.0)).title("No images to dispay").icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon1)));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12));

    }
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }


      @Override
    public void onStop() {
        super.onStop();
  AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }


    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }
    public void onDestroy() {
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e(TAG, "Error while mapview on destroy");
            }
        }
        super.onDestroy();
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

}
