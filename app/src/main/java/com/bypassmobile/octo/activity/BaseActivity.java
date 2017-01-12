package com.bypassmobile.octo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bypassmobile.octo.rest.GithubEndpoint;

import retrofit.RestAdapter;

public class BaseActivity extends AppCompatActivity {

    private GithubEndpoint endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RestAdapter adapter = new RestAdapter.Builder()
                .setServer(GithubEndpoint.SERVER)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        endpoint = adapter.create(GithubEndpoint.class);
    }

    public GithubEndpoint getEndpoint() {
        return endpoint;
    }
}
