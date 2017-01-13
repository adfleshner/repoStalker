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
import android.view.View;
import android.widget.Toast;

import com.bypassmobile.octo.ObservedState;
import com.bypassmobile.octo.R;
import com.bypassmobile.octo.adapter.StalkerAdapter;
import com.bypassmobile.octo.utils.ConnectionUtils;
import com.flesh.webservice.model.User;
import com.bypassmobile.octo.utils.SortingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends BaseActivity implements Observer {

    private static final String USERS_KEY = "com.bypassmobile.octo MainActivity Users Key";
    public static final String USER_INTENT_KEY = "com.bybassmobile.octo User Intent Key";
    private static final String STATE_KEY = "com.bypassmobile.octo Current State";
    private Toolbar mToolbar;
    private RecyclerView rvUsers;
    private SwipeRefreshLayout srlUsers;
    private StalkerAdapter mAdapter;
    private ArrayList<User> mUsers;
    private View mEmptyView, mLoadingView;
    private ObservedState mCurrentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //gets the current state from the savedInstance or creates a new one.
        getViewCurrentState(savedInstanceState);
        //inits the view
        init();
        //shows the correct state of the view.
        if (mCurrentState != null) {
            showCorrectView(mCurrentState.getCurrentState());
        }
        //gets the data from the proper source
        getData(savedInstanceState);

    }

    //gets the current state that the view is in
    private void getViewCurrentState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_KEY)) {
            mCurrentState = savedInstanceState.getParcelable(STATE_KEY);
        } else {
            mCurrentState = new ObservedState();
        }
    }

    //Inits all of the views on the activity and sets them to their initial state.
    private void init() {
        mCurrentState = new ObservedState();
        mCurrentState.addObserver(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        rvUsers = (RecyclerView) findViewById(R.id.list);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        srlUsers = (SwipeRefreshLayout) findViewById(R.id.swipeToRefreshLayout);
        srlUsers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //get new data. By giving it null it forces the getData function to get new data from the web.
                getData(null);
            }
        });
        mEmptyView = findViewById(R.id.emptyView);
        mLoadingView = findViewById(R.id.loadingView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //adds observer so fun observed magic happens
        if (mCurrentState != null) {
            mCurrentState.addObserver(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //removes observer to prevent memory leaks
        if (mCurrentState != null) {
            mCurrentState.deleteObserver(this);
        }
    }

    public void getData(Bundle savedInstanceState) {
        //meaning it is the first time the user has entered the app.
        if (savedInstanceState != null && savedInstanceState.containsKey(USERS_KEY)) {
            //Populate list from local copy
            mUsers = savedInstanceState.getParcelableArrayList(USERS_KEY);
            populateList(mUsers);
        } else {
            //Get New List from the web.
            mCurrentState.setCurrentState(ObservedState.STATE.LOADING);
            if (ConnectionUtils.isNetworkAvailable(this)) {
                getEndpoint().getOrganizationMember("bypasslane", new Callback<List<User>>() {
                    @Override
                    public void success(List<User> users, Response response) {
                        stopSrlRefreshingAnimation();

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
                        stopSrlRefreshingAnimation();

                        showError(error);
                    }
                });
            } else {
                stopSrlRefreshingAnimation();
                Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                //populate data with data that is there if there is any
                populateList(mUsers);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //saves data for screen rotation.
        if (mUsers != null) outState.putParcelableArrayList(USERS_KEY, mUsers);
        if (mCurrentState != null) outState.putParcelable(STATE_KEY, mCurrentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView svSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        svSearchView.setQueryHint(getString(R.string.main_search_hint));
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

    //Helper Functions
    private void stopSrlRefreshingAnimation() {
        if (srlUsers != null && srlUsers.isRefreshing()) {
            srlUsers.setRefreshing(false);
        }
    }

    private void showError(RetrofitError error) {
        mCurrentState.setCurrentState(ObservedState.STATE.EMPTY);
        Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    private void populateList(ArrayList<User> users) {
        //show empty view and return
        if (users == null || users.isEmpty()) {
            mCurrentState.setCurrentState(ObservedState.STATE.EMPTY);
            Toast.makeText(MainActivity.this, R.string.no_data, Toast.LENGTH_SHORT).show();
            return;
        }
        //set to data sate and show data
        mCurrentState.setCurrentState(ObservedState.STATE.DATA);
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

    private void showCorrectView(ObservedState.STATE mCurrentState) {
        switch (mCurrentState) {
            case LOADING:
                showLoadingView();
                hideDataView();
                hideEmptyView();
                break;
            case EMPTY:
                hideLoadingView();
                hideDataView();
                showEmptyView();
                break;
            case DATA:
                hideLoadingView();
                showDataView();
                hideEmptyView();
                break;
        }
    }

    //Show and hides all of the views
    private void showDataView() {
        rvUsers.setVisibility(VISIBLE);
    }

    private void hideLoadingView() {
        mLoadingView.setVisibility(GONE);
    }

    private void hideEmptyView() {
        mEmptyView.setVisibility(GONE);
    }

    private void showEmptyView() {
        mEmptyView.setVisibility(VISIBLE);
    }

    private void hideDataView() {
        rvUsers.setVisibility(GONE);
    }

    private void showLoadingView() {
        mLoadingView.setVisibility(VISIBLE);
    }


    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof ObservedState) {
            showCorrectView(((ObservedState) observable).getCurrentState());
        }
    }
}
