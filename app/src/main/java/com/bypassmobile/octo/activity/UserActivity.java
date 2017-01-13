package com.bypassmobile.octo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bypassmobile.octo.ObservedState;
import com.bypassmobile.octo.R;
import com.bypassmobile.octo.adapter.StalkerAdapter;
import com.bypassmobile.octo.utils.ConnectionUtils;
import com.flesh.webservice.image.ImageLoader;
import com.flesh.webservice.model.User;
import com.bypassmobile.octo.utils.SortingUtils;
import com.squareup.picasso.Picasso;

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

public class UserActivity extends BaseActivity implements Observer{

    private static final String USER_KEY = "com.bypassmobile.octo User Key";
    private static final String FOLLOWERS_KEY = "com.bypassmobile.octo Followers Key";
    private static final String STATE_KEY = "com.bypassmobile.octo Current State";
    private TextView tvSelectedUser;
    private ImageView ivSelectedUser;
    private User mSelectedUser;
    private ArrayList mUserFollowers;
    private RecyclerView rvFollowers;
    private StalkerAdapter mAdapter;
    private Toolbar mToolbar;
    private View mEmptyView,mLoadingView;
    private ObservedState mCurrentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        getViewCurrentState(savedInstanceState);
        init();
        getData(savedInstanceState);
    }

    //gets the current state that the view is in
    private void getViewCurrentState(Bundle savedInstanceState) {
        if(savedInstanceState!=null && savedInstanceState.containsKey(STATE_KEY)) {
            mCurrentState = savedInstanceState.getParcelable(STATE_KEY);
        }else{
            mCurrentState = new ObservedState();
        }
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        }
        ivSelectedUser = (ImageView) findViewById(R.id.ivUserProfileImage);
        tvSelectedUser = (TextView) findViewById(R.id.tvSelectedUser);
        rvFollowers = (RecyclerView) findViewById(R.id.followersList);
        rvFollowers.setLayoutManager(new LinearLayoutManager(this));
        mEmptyView = findViewById(R.id.emptyView);
        mLoadingView = findViewById(R.id.loadingView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //adds observer so fun observed magic happens
        if(mCurrentState!=null) {
            mCurrentState.addObserver(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //removes observer to prevent memory leaks
        if(mCurrentState!=null){
            mCurrentState.deleteObserver(this);
        }
    }

    //Gets the data from the Previous Intent or the bundle that is passed in. Or it closes the Activity.
    private void getData(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(USER_KEY)) {
            //if the saveInstanceState is not null and has the USER_KEY then the system get the object from the savedInstance.
            //Will happen on screen rotation.
            mSelectedUser = savedInstanceState.getParcelable(USER_KEY);
        } else if (getIntent() != null && getIntent().hasExtra(MainActivity.USER_INTENT_KEY)) {
            //if the getIntent is not null and has the extra for that key it will get the object from there
            //This will happen when the The Actvity is first created.
            mSelectedUser = getIntent().getParcelableExtra(MainActivity.USER_INTENT_KEY);
        } else {
            //if nothing is met the system will close that activity and tell the user why.
            Toast.makeText(this, R.string.no_user, Toast.LENGTH_SHORT).show();
            finish();
        }
        populateUserData(savedInstanceState);
    }

    //Populates the views with the data from the in mSelected user. This also passes the savedInstanceState
    //so that it can be used to either populate the followers if they are contained within.
    private void populateUserData(Bundle savedInstanceState) {
        tvSelectedUser.setText(getString(R.string.user_follows, mSelectedUser.getName()));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mSelectedUser.getName());
        }
        ImageLoader.createImageLoader(this).load(mSelectedUser.getProfileURL()).priority(Picasso.Priority.HIGH).into(ivSelectedUser);
        getFollowersData(savedInstanceState);
    }

    //checks to see if there is a local copy of the follower in the savedInstanceState
    //if not it makes a webcall to get followers.
    private void getFollowersData(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(FOLLOWERS_KEY)) {
            mUserFollowers = savedInstanceState.getParcelableArrayList(FOLLOWERS_KEY);
            populateFollowersData(mUserFollowers);
        } else {
            if(ConnectionUtils.isNetworkAvailable(this)) {
                mCurrentState.setCurrentState(ObservedState.STATE.LOADING);
                getEndpoint().getFollowingUser(mSelectedUser.getName(), new Callback<List<User>>() {
                    @Override
                    public void success(List<User> users, Response response) {
                        //sorts the list in alphabetical order
                        Collections.sort(users, new SortingUtils.UserAlphabeticalComparetor());
                        mUserFollowers = new ArrayList<>(users);
                        populateFollowersData(mUserFollowers);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        showError(error);
                    }
                });
            }else{
                Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                //populate data with data that is there if there is any
                populateFollowersData(mUserFollowers);
            }
        }
    }

    private void populateFollowersData(ArrayList<User> mUserFollowers) {
        //show empty view and return
        if(mUserFollowers == null || mUserFollowers.isEmpty()){
            Toast.makeText(this, getString(R.string.follows_no_one,mSelectedUser.getName()), Toast.LENGTH_SHORT).show();
            mCurrentState.setCurrentState(ObservedState.STATE.EMPTY);
            return;
        }
        //show data view and show data.
        mCurrentState.setCurrentState(ObservedState.STATE.DATA);
        mAdapter = new StalkerAdapter(mUserFollowers);
        mAdapter.setOnUserClickListener(new StalkerAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent userIntent = new Intent(UserActivity.this, UserActivity.class);
                userIntent.putExtra(MainActivity.USER_INTENT_KEY, user);
                startActivity(userIntent);
            }
        });
        rvFollowers.setAdapter(mAdapter);
    }

    private void showError(RetrofitError error) {
        mCurrentState.setCurrentState(ObservedState.STATE.EMPTY);
        Toast.makeText(this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //saves all of the data for rotation
        if (mSelectedUser != null) outState.putParcelable(USER_KEY, mSelectedUser);
        if (mUserFollowers != null)outState.putParcelableArrayList(FOLLOWERS_KEY, mUserFollowers);
        if(mCurrentState!=null) outState.putParcelable(STATE_KEY,mCurrentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search,menu);
        //gets search view for filtering in the menu.
        SearchView svSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        svSearchView.setQueryHint(getString(R.string.user_search_hint,mSelectedUser.getName()));
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Causes the home or up button to perform the same as the back button
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    //View Helper Functions.
    private void showCorrectView(ObservedState.STATE mCurrentState) {
        switch (mCurrentState){
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
    private void showDataView() {rvFollowers.setVisibility(VISIBLE);}
    private void hideLoadingView() {mLoadingView.setVisibility(GONE);}
    private void hideEmptyView() {mEmptyView.setVisibility(GONE);}
    private void showEmptyView() {mEmptyView.setVisibility(VISIBLE);}
    private void hideDataView() {rvFollowers.setVisibility(GONE);}
    private void showLoadingView() {mLoadingView.setVisibility(VISIBLE);}



    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservedState){
            showCorrectView(((ObservedState) observable).getCurrentState());
        }
    }
}
