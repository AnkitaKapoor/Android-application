package com.example.sengloke.InfoGo.pictureTaking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sengloke.InfoGo.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayImage extends Activity
{

      private MyImage image;
    private ImageView imageView;
    private EditText et;
    private TextView description;
    private String jstring;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        imageView = (ImageView) findViewById(R.id.display_image_view);
        description = (TextView) findViewById(R.id.text_view_description);
        Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            jstring = extras.getString("IMAGE");
        }
        image = getMyImage(jstring);
        description.setText(image.details());
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        imageView.setImageBitmap(ImageResizer.decodeSampledBitmapFromFile(image.getPath(), width, height));
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
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    public void btnInsertOnClick(View v)
    {
        DAOdb db = new DAOdb(this);
        et = (EditText) findViewById(R.id.editText);
        String des = et.getText().toString();
        image.setDescription(des);
        db.updateImage(image);
        db.close();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void btnDeleteOnClick(View v)
    {
        DAOdb db = new DAOdb(this);
        db.deleteImage(image);
        db.close();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override protected void onSaveInstanceState(Bundle outState)
    {
            if (jstring != null)
        {
            outState.putString("jstring", jstring);
        }
            super.onSaveInstanceState(outState);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
            super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState.containsKey("jstring"))
        {
            jstring = savedInstanceState.getString("jstring");
        }
    }



}
