package com.flesh.webservice.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by aaronfleshner on 1/11/17.
 */

public class ByPassImageCacheExpirationHelper {

    private String EXP_DATE_KEY = "com.flesh.webservice ByPass Expiration";

    private Context mContext;
    private int mExpires = 24;// The Cache will now expire every 24 hours.
    private String format = "MM-dd-yyyy'T'HH:mm:ss";

    public ByPassImageCacheExpirationHelper(Context cxt) {
        this.mContext = cxt;
    }

    //First it checks to see if there has been a date created and saved.
    //If not it will create one and return true.
    //If there is a date it checks to see it has been 24 hours since that date was created.
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
            if (Hours.hoursBetween(dateFromPrefs, now).isGreaterThan(Hours.hours(mExpires))) {
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
}
