package com.example.sengloke.InfoGo.pictureTaking;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sengloke.InfoGo.R;

import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<MyImage>
{
    private final int THUMBSIZE = 96;
   private static class ViewHolder
    {
        ImageView imgIcon;
        TextView description;
    }

    public ImageAdapter(Context context, ArrayList<MyImage> images)
    {
        super(context, 0, images);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_image, parent, false);
            viewHolder.description = (TextView) convertView.findViewById(R.id.item_img_infor);
            viewHolder.imgIcon = (ImageView) convertView.findViewById(R.id.item_img_icon);
            convertView.setTag(viewHolder);

        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
         MyImage image = getItem(position);
           viewHolder.description.setText(image.details());
           viewHolder.imgIcon.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(image.getPath()),THUMBSIZE, THUMBSIZE));
     return convertView;
    }
}
