package com.example.sengloke.InfoGo.pictureTaking;


import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.example.sengloke.InfoGo.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity
{

    private ArrayList<MyImage> images;
    private ImageAdapter imageAdapter;
    private ListView listView;
    private Uri mCapturedImageURI;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private DAOdb daOdb;
    GPSTracker gps;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_picture);
        images = new ArrayList();
        imageAdapter = new ImageAdapter(this, images);
        listView = (ListView) findViewById(R.id.main_list_view);
        listView.setAdapter(imageAdapter);
        addItemClickListener(listView);
        initDB();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initDB()
    {
        daOdb = new DAOdb(this);
            for (MyImage mi : daOdb.getImages()) {
            images.add(mi);
        }

    }
    public void btnMapOnClick(View view)
    {
        Intent intent = new Intent(getBaseContext(), MapsActivity.class);
         startActivity(intent);
    }

    public void btnAddOnClick(View view)
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.setTitle("Choose Image");
        Button btnExit = (Button) dialog.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btnChoosePath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeGallery();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTakePhoto();
                dialog.dismiss();
            }
        });
    dialog.show();
    }

    private void activeTakePhoto()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            String fileName = "temp.jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void activeGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (requestCode == RESULT_LOAD_IMAGE &&
                        resultCode == RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    MyImage image = new MyImage();
                     image.setDescription("Click to add description");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    gps = new GPSTracker(MainActivity.this);
                    if(gps.canGetLocation())
                    {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        List<Address> addresses  = null;
                        try
                        {
                            addresses = geocoder.getFromLocation(latitude,longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String zip = addresses.get(0).getPostalCode();
                        String country = addresses.get(0).getCountryName();
                        String loc = city + "," + state + "," + zip + "," + country;

                        image.setTitle(loc);

                        image.setLatitude(latitude);
                        image.setLongitude(longitude);
                    }
                    else
                    {
                        gps.showSettingsAlert();
                    }
                    imageAdapter.add(image);
                    daOdb.addImage(image);
                }
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(mCapturedImageURI, projection, null, null, null);
                    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String picturePath = cursor.getString(column_index_data);
                    MyImage image = new MyImage();
                    image.setDescription("Click to add description");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    gps = new GPSTracker(MainActivity.this);
                    if(gps.canGetLocation())
                    {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                         List<Address> addresses  = null;
                        try
                        {
                            addresses = geocoder.getFromLocation(latitude,longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String zip = addresses.get(0).getPostalCode();
                        String country = addresses.get(0).getCountryName();
                        String loc = city + "," + state + "," + zip + "," + country;

                        image.setTitle(loc);
                        image.setLatitude(latitude);
                        image.setLongitude(longitude);
                    }
                    else
                    {
                        gps.showSettingsAlert();
                    }
                    imageAdapter.add(image);
                    daOdb.addImage(image);
                }
        }
    }

    private void addItemClickListener(final ListView listView)
    {
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MyImage image = (MyImage) listView.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), DisplayImage.class);
               intent.putExtra("IMAGE", (new Gson()).toJson(image));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
            if (mCapturedImageURI != null) {
            outState.putString("mCapturedImageURI", mCapturedImageURI.toString());
        }
            super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
            super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState.containsKey("mCapturedImageURI")) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString("mCapturedImageURI"));
        }
    }

    public Action getIndexApiAction()
    {
        Thing object = new Thing.Builder()
                .setName("Main Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
