package com.bypassmobile.octo.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by aaronfleshner on 1/11/17.
 */

public class ByPassImageCacheExpirationAndInvalidatorHelper {

    private String EXP_DATE_KEY = "com.bypassmobile.octo Expiration";

    private Context mContext;
    private int mExpires = 60;
    private String format = "MM-dd-yyyy'T'HH:mm:ss";

    public ByPassImageCacheExpirationAndInvalidatorHelper(Context cxt) {
        this.mContext = cxt;
    }

    //First it checks to see if there has been a date created and saved.
    //If not it will create one and return true.
    //If there is a date it checks to see it has been 60 mins since that date was created.
    //If it has been 60 mins since it will delete the date and create a new one and return true telling the
    //system that the cache has expired.
    //If the date stored is not farther then 60 mins. it returns false.
    public boolean hasMapExpired() {
        Date date;
        SharedPreferences preferences = mContext.getSharedPreferences("com.bypassmobile.octo", Context.MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        if (preferences.contains(EXP_DATE_KEY)) {
            try {
                date = sdf.parse(preferences.getString(EXP_DATE_KEY, ""));
            } catch (ParseException e) {
                date = new Date();
            }
            DateTime now = new DateTime();
            DateTime dateFromPrefs = new DateTime(date);
            if (Minutes.minutesBetween(dateFromPrefs, now).isGreaterThan(Minutes.minutes(mExpires))) {
                //remove the last one
                if (preferences.edit().remove(EXP_DATE_KEY).commit()) {
                    //then add a new one.
                    preferences.edit().putString(EXP_DATE_KEY, sdf.format(new Date())).apply();
                }
                return true;
            }else{
                return false;
            }
        }else{
            preferences.edit().putString(EXP_DATE_KEY, sdf.format(new Date())).apply();
            return true;
        }
    }

    //saves the hashmap to a file for local storage.
    //First it creates a byte[] from the bitmap then adds it to the HashMap<String,byte[]>
    //Then it writes the HashMap to a file.
    public void saveImageHashMap(final Map<String, Bitmap> imageMap) {
       final File file = new File(mContext.getDir("data", Context.MODE_PRIVATE), "map");
        Handler saveHashMapHandler = new Handler();
        saveHashMapHandler.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String, byte[]> byteMap = new HashMap<>();
                Iterator<Map.Entry<String, Bitmap>> it = imageMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Bitmap> entry = it.next();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    entry.getValue().compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    byteMap.put(entry.getKey(), byteArray);
                    try {
                        stream.close();
                        stream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                    outputStream.writeObject(byteMap);
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //loads the map from file.
    //Then converts all of the byte[]'s back to bitmaps and then returns a new HasMap<String,Bitmap>
    //for the Image Cache
    public HashMap<String, Bitmap> loadImageHashMap() throws IOException, ClassNotFoundException {
        File file = new File(mContext.getDir("data", Context.MODE_PRIVATE), "map");
        ObjectInputStream intputStream = new ObjectInputStream(new FileInputStream(file));
        HashMap<String, byte[]> byteMap = (HashMap<String, byte[]>) intputStream.readObject();
        HashMap<String, Bitmap> imageMap = new HashMap<>();
        Iterator<Map.Entry<String, byte[]>> it = byteMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, byte[]> entry = it.next();
            byte[] bitmapdata = entry.getValue();
            imageMap.put(entry.getKey(), BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length));
        }
        return imageMap;
    }

    //deletes the map from storage.
    public void deleteMap() {
        File fdelete = new File(mContext.getDir("data", Context.MODE_PRIVATE), "map");
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("Map Deleted");
            } else {
                System.out.println("Map not Deleted");
            }
        }
    }
}
