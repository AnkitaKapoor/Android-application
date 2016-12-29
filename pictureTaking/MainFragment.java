package com.example.sengloke.InfoGo.pictureTaking;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.example.sengloke.InfoGo.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainFragment extends Fragment
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
	private View rootView;

	public MainFragment()
	{
	
	}
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	}
     
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       // setContentView(R.layout.main_picture);
       //fragment code	   
	   if(rootView!=null)
        {
            ViewGroup parent = (ViewGroup)rootView.getParent();
            if(parent!=null)
            {parent.removeView(rootView);}
        }

	    rootView = inflater.inflate(R.layout.main_picture, container, false);
       //fragment code ends
		
		
		
        images = new ArrayList();
        imageAdapter = new ImageAdapter(getActivity(), images);
        listView = (ListView) rootView.findViewById(R.id.main_list_view);
        listView.setAdapter(imageAdapter);
        addItemClickListener(listView);
        initDB();
        client = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).build();


        Button button1 = (Button) rootView.findViewById(R.id.viewMap);
        button1.setOnClickListener(new View.OnClickListener(){
                                       public void onClick(View view)
                                       {
                                           MapsFragment fragmentWithMap = new MapsFragment();
                                           getFragmentManager().beginTransaction()
                                                   .replace(R.id.layout, fragmentWithMap)
                                                   .commit();

                                       }

                                   }
        );


        Button button2 = (Button) rootView.findViewById(R.id.btnAdd);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.custom_dialog_box);
                // rootView = inflater.inflate(R.layout.main_picture, container, false);
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
        });


		return rootView;
    }

    private void initDB()
    {
        daOdb = new DAOdb(getActivity());
            for (MyImage mi : daOdb.getImages()) {
            images.add(mi);
        }

    }
/*
    public void btnAddOnClick(View view)
    {
        final Dialog dialog = new Dialog(getActivity());
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
*/

    private void activeTakePhoto()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(super.getActivity().getPackageManager()) != null) {
            String fileName = "temp.jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            mCapturedImageURI = super.getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void activeGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (requestCode == RESULT_LOAD_IMAGE &&
                        resultCode == getActivity().RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = super.getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    MyImage image = new MyImage();
                     image.setDescription("Click to add description");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    gps = new GPSTracker(rootView.getContext());
                    if(gps.canGetLocation())
                    {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = super.getActivity().managedQuery(mCapturedImageURI, projection, null, null, null);
                    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String picturePath = cursor.getString(column_index_data);
                    MyImage image = new MyImage();
                    image.setDescription("Click to add description");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    gps = new GPSTracker(rootView.getContext());
                    if(gps.canGetLocation())
                    {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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

                //replace this fragment with a new one
                MyImage image = (MyImage) listView.getItemAtPosition(position);
               // FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
              //  ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

               FragmentTransaction ft = getFragmentManager().beginTransaction();

                DisplayImageFrag frag = new DisplayImageFrag();

                Bundle bundles = new Bundle();
                if (image != null) {

                    bundles.putSerializable("image", image);
                    Log.e("aDivision", "is valid");

                } else {
                    Log.e("aDivision", "is null");

                }
                frag.setArguments(bundles);
                ft.replace(R.id.layout, frag);
                //ft.addToBackStack(null);
                ft.commit();
                /*
                Intent intent = new Intent(getActivity().getBaseContext(), DisplayImage.class);
               intent.putExtra("IMAGE", (new Gson()).toJson(image));
                startActivity(intent);
                */
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
            if (mCapturedImageURI != null) {
            outState.putString("mCapturedImageURI", mCapturedImageURI.toString());
        }
            super.onSaveInstanceState(outState);
    }

    /*
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
            super.getActivity().onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState.containsKey("mCapturedImageURI")) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString("mCapturedImageURI"));
        }
    }
*/
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
