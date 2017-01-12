package com.bypassmobile.octo.image;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bypassmobile.octo.preferences.ByPassImageCacheExpirationAndInvalidatorHelper;
import com.squareup.picasso.Cache;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageCache implements Cache {

    private static final String TAG = "ImageCache";
    private Map<String, Bitmap> cacheMap = new LinkedHashMap<>();
    private Context mContext;
    private ByPassImageCacheExpirationAndInvalidatorHelper expirationAndInvalidaterHelper;

    public ImageCache(Context cxt) {
        this.mContext = cxt;
        expirationAndInvalidaterHelper = new ByPassImageCacheExpirationAndInvalidatorHelper(cxt);
        if (!expirationAndInvalidaterHelper.hasMapExpired()) {
            try {
                cacheMap = expirationAndInvalidaterHelper.loadImageHashMap();
            } catch (IOException e) {

            } catch (ClassNotFoundException e1) {

            }
        } else {
            expirationAndInvalidaterHelper.deleteMap();
        }
    }

    @Override
    public Bitmap get(String stringResource) {
        return cacheMap.get(stringResource);
    }

    @Override
    public void set(String stringResource, Bitmap bitmap) {
        Log.d("SET", stringResource);
        cacheMap.put(stringResource, bitmap);
        expirationAndInvalidaterHelper.saveImageHashMap(cacheMap);
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
        expirationAndInvalidaterHelper.deleteMap();
        cacheMap.clear();
    }

    @Override
    public void clearKeyUri(String keyPrefix) {

    }


}
