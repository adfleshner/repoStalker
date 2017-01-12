package com.bypassmobile.octo.image;


import android.content.Context;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ImageLoader {

    private static Picasso singleton;

    public static Picasso createImageLoader(Context context){
        if(singleton == null){
            singleton = new Picasso.Builder(context).memoryCache(new ImageCache(context)).build();
        }
        return singleton;
    }
}
