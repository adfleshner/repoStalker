package com.flesh.webservice.image;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.flesh.webservice.preferences.ByPassImageCacheExpirationHelper;
import com.squareup.picasso.Cache;


public class ImageCache implements Cache {

    private static final String TAG = "ImageCache";
    private LruCache<String, Bitmap> cacheMap = new LruCache<>(4*1024*1024); //4mb
    private ByPassImageCacheExpirationHelper expirationAndInvalidaterHelper;

    public ImageCache(Context cxt) {
        expirationAndInvalidaterHelper = new ByPassImageCacheExpirationHelper(cxt);
    }

    @Override
    public Bitmap get(String stringResource) {
        //Clears the Cache If it has been longer then the alloted time. (Currently 24 hours)
        //Currently Clears the entire cache from the time that first Image was gotten.
        if(expirationAndInvalidaterHelper.hasMapExpired()){
            clear();
        }
        return cacheMap.get(stringResource);
    }

    @Override
    public void set(String stringResource, Bitmap bitmap) {
        Log.d("SET", stringResource);
        synchronized (cacheMap) {
            if (cacheMap.get(stringResource) == null) {
                cacheMap.put(stringResource, bitmap);
            }
        }
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void clear() {
        Log.d(TAG,"Cache Cleared");
        cacheMap.evictAll();
    }

    @Override
    public void clearKeyUri(String keyPrefix) {

    }


}
