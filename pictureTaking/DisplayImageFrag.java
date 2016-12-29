package com.example.sengloke.InfoGo.pictureTaking;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sengloke.InfoGo.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayImageFrag extends Fragment
{

    private MyImage image;
    private ImageView imageView;
    private EditText et;
    private TextView description;
    private String jstring;
    private View rootView;
	
		public DisplayImageFrag()
	{
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	}
	  
	      public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
     //  setContentView(R.layout.activity_display_image);


              savedInstanceState = getArguments();

             // MyImage image= (MyImage) savedInstanceState.getSerializable("image");

              rootView = inflater.inflate(R.layout.activity_display_image, container, false);
       
		imageView = (ImageView) rootView.findViewById(R.id.display_image_view);
        description = (TextView) rootView.findViewById(R.id.text_view_description);
        //Bundle extras = getIntent().getExtras();

        if (savedInstanceState != null)
        {
            image = (MyImage)savedInstanceState.getSerializable("image");
        }
       // image = getMyImage(jstring);
        description.setText(image.details());
        Display display = super.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        imageView.setImageBitmap(ImageResizer.decodeSampledBitmapFromFile(image.getPath(), width, height));


              Button button1 = (Button) rootView.findViewById(R.id.btnBack);
              button1.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View view) {

  /*                                               Fragment currentFragment =  getFragmentManager().findFragmentById(R.id.display_layout);

                                                  EmptyFrag fragA= new EmptyFrag();
                                                  getFragmentManager().beginTransaction()
                                                 .remove(currentFragment)
                                                 .add(R.id.fragmentcontainer, fragA)
                                                         .commit();


                                                 MainFragment fragment = new MainFragment();
                                                 getFragmentManager().beginTransaction()
                                                         .replace(R.id.fragmentcontainer, fragment)
                                                         .commit();
*/
                                             }

                                         }
              );


              Button button2 = (Button) rootView.findViewById(R.id.btnDelete);
              button2.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View view) {
                                                 DAOdb db = new DAOdb(getActivity());
                                                 db.deleteImage(image);
                                                 db.close();
                                             }

                                         }
              );

              Button button3 = (Button) rootView.findViewById(R.id.btnInsert);
              button3.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View view) {
                                                 DAOdb db = new DAOdb(getActivity());
                                                 et = (EditText) rootView.findViewById(R.id.editText);
                                                 String des = et.getText().toString();
                                                 image.setDescription(des);
                                                 db.updateImage(image);
                                                 db.close();
                                             }

                                         }
              );
              return rootView;
    }

    private MyImage getMyImage(String image)
    {
        try
        {
            JSONObject job = new JSONObject(image);
            return (new MyImage(job.getString("title"),
                    job.getString("description"), job.getString("path"),
                    job.getLong("datetimeLong"), job.getDouble("latitude"), job.getDouble("longitude")
            ));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public void btnBackOnClick(View v)
    {
        /*
        startActivity(new Intent(getActivity(), MainActivity.class));
        super.getActivity().finish();
        */

    }
    public void btnInsertOnClick(View v)
    {
        DAOdb db = new DAOdb(getActivity());
        et = (EditText) rootView.findViewById(R.id.editText);
        String des = et.getText().toString();
        image.setDescription(des);
        db.updateImage(image);
        db.close();
        startActivity(new Intent(super.getActivity(), MainActivity.class));
        super.getActivity().finish();
    }

    public void btnDeleteOnClick(View v)
    {
        DAOdb db = new DAOdb(getActivity());
        db.deleteImage(image);
        db.close();
        startActivity(new Intent(super.getActivity(), MainActivity.class));
        super.getActivity().finish();
    }

    @Override public void onSaveInstanceState(Bundle outState)
    {
            if (jstring != null)
        {
            outState.putString("jstring", jstring);
        }
            super.onSaveInstanceState(outState);
    }
/*
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
            super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState.containsKey("jstring"))
        {
            jstring = savedInstanceState.getString("jstring");
        }
    }
    */



}
