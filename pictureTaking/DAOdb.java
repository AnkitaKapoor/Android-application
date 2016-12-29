package com.example.sengloke.InfoGo.pictureTaking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DAOdb
{
    private SQLiteDatabase database;
    private DBhelper dbHelper;

    public DAOdb(Context context)
    {
        dbHelper = new DBhelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void close()
    {
        dbHelper.close();
    }


    public long addImage(MyImage image)
    {
        ContentValues cv = new ContentValues();
        cv.put(DBhelper.COLUMN_PATH, image.getPath());
        cv.put(DBhelper.COLUMN_TITLE, image.getTitle());
        cv.put(DBhelper.COLUMN_DESCRIPTION, image.getDescription());
        cv.put(DBhelper.COLUMN_DATETIME, System.currentTimeMillis());
        cv.put(DBhelper.COLUMN_LATITUDE, image.getLatitude());
        cv.put(DBhelper.COLUMN_LONGITUDE, image.getLongitude());
        return database.insert(DBhelper.TABLE_NAME, null, cv);
    }

    public void updateImage(MyImage image)
    {
        ContentValues cv = new ContentValues();
        cv.put(DBhelper.COLUMN_DESCRIPTION, image.getDescription());
        String whereClause = DBhelper.COLUMN_TITLE + "=? AND " + DBhelper.COLUMN_DATETIME + "=?";
        String[] whereArgs = new String[]{image.getTitle(), String.valueOf(image.getDatetimeLong())};
        database.update(DBhelper.TABLE_NAME, cv, whereClause, whereArgs);

    }

    public void deleteImage(MyImage image)
    {
        String whereClause = DBhelper.COLUMN_TITLE + "=? AND " + DBhelper.COLUMN_DATETIME + "=?";
        String[] whereArgs = new String[]{image.getTitle(), String.valueOf(image.getDatetimeLong())};
        database.delete(DBhelper.TABLE_NAME, whereClause, whereArgs);
    }

    public List<MyImage> getImages()
    {
        List<MyImage> MyImages = new ArrayList<>();
        Cursor cursor = database.rawQuery( "SELECT * FROM " + DBhelper.TABLE_NAME, null );
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            MyImage MyImage = cursorToMyImage(cursor);
            MyImages.add(MyImage);
            cursor.moveToNext();
        }
        cursor.close();
        return MyImages;
    }
    public List<MyImage> getLatLong()
    {
        List<MyImage> MyLat = new ArrayList<>();
        Cursor cursor = database.rawQuery( "SELECT * FROM " + DBhelper.TABLE_NAME, null );
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            MyImage MyImage = cursorToMyImage(cursor);
            MyLat.add(MyImage);
            cursor.moveToNext();
        }
        cursor.close();
        return MyLat;
    }

    private MyImage cursorToMyImage(Cursor cursor)
    {
        MyImage image = new MyImage();
        image.setPath(cursor.getString(cursor.getColumnIndex(DBhelper.COLUMN_PATH)));
        image.setTitle(cursor.getString(cursor.getColumnIndex(DBhelper.COLUMN_TITLE)));
        image.setDatetime(cursor.getLong(cursor.getColumnIndex(DBhelper.COLUMN_DATETIME)));
        image.setDescription(cursor.getString(cursor.getColumnIndex(DBhelper.COLUMN_DESCRIPTION)));
        image.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBhelper.COLUMN_LATITUDE)));
        image.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBhelper.COLUMN_LONGITUDE)));
        return image;
    }
}
