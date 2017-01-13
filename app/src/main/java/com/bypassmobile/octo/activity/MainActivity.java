package com.bypassmobile.octo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bypassmobile.octo.R;
import com.bypassmobile.octo.adapter.StalkerAdapter;
import com.flesh.webservice.model.User;
import com.bypassmobile.octo.utils.SortingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends BaseActivity {

    private static final String USERS_KEY = "com.bypassmobile.octo MainActivity Users Key";
    public static final String USER_INTENT_KEY = "com.bybassmobile.octo User Intent Key";
    private Toolbar mToolbar;
    private RecyclerView rvUsers;
    private SwipeRefreshLayout srlUsers;
    private StalkerAdapter mAdapter;
    private ArrayList<User> mUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getData(savedInstanceState);
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        rvUsers = (RecyclerView) findViewById(R.id.list);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        srlUsers = (SwipeRefreshLayout) findViewById(R.id.swipeToRefreshLayout);
        srlUsers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //clear the data.
                mUsers = null;
                //get new data. By giving it null it forces the getData function to get new data from the web.
                getData(null);
            }
        });
    }


    public void getData(Bundle savedInstanceState) {
        //meaning it is the first time the user has entered the app.
        if (savedInstanceState != null && savedInstanceState.containsKey(USERS_KEY)) {
            //Populate list from local copy
            mUsers = savedInstanceState.getParcelableArrayList(USERS_KEY);
            populateList(mUsers);
        } else {
            //Get New List from the web.
            getEndpoint().getOrganizationMember("bypasslane", new Callback<List<User>>() {
                @Override
                public void success(List<User> users, Response response) {
                    if (srlUsers != null && srlUsers.isRefreshing()) {
                        srlUsers.setRefreshing(false);
                    }

                    //Adds me to the end of the users list to test and make sure it is sorting correctly
                    //ArrayList<User> usersAndAaron = new ArrayList<User>(users);
                    //usersAndAaron.add(new User("adfleshner","https://avatars.githubusercontent.com/u/3021508?v=3"));
                    //Collections.sort(usersAndAaron, new SortingUtils.UserAlphabeticalComparetor());
                    //mUsers = new ArrayList<>(usersAndAaron);

                    //sorts the list in alphabetical order
                    Collections.sort(users, new SortingUtils.UserAlphabeticalComparetor());
                    mUsers = new ArrayList<>(users);
                    populateList(mUsers);
                }

                @Override
                public void failure(RetrofitError error) {
                    if (srlUsers != null && srlUsers.isRefreshing()) {
                        srlUsers.setRefreshing(false);
                    }
                    Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void populateList(ArrayList<User> users) {
        mAdapter = new StalkerAdapter(users);
        mAdapter.setOnUserClickListener(new StalkerAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent userIntent = new Intent(MainActivity.this, UserActivity.class);
                userIntent.putExtra(USER_INTENT_KEY, user);
                startActivity(userIntent);
            }
        });
        rvUsers.setAdapter(mAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUsers != null)
            outState.putParcelableArrayList(USERS_KEY, mUsers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView svSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        //makes the data search able by keystroke or by enter key.
        svSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


}
