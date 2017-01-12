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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bypassmobile.octo.R;
import com.bypassmobile.octo.adapter.StalkerAdapter;
import com.bypassmobile.octo.image.ImageLoader;
import com.bypassmobile.octo.model.User;
import com.bypassmobile.octo.utils.SortingUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserActivity extends BaseActivity {

    private static final String USER_KEY = "com.bypassmobile.octo User Key";
    private static final String FOLLOWERS_KEY = "com.bypassmobile.octo Followers Key";
    private TextView tvSelectedUser;
    private ImageView ivSelectedUser;
    private User mSelectedUser;
    private ArrayList mUserFollowers;
    private RecyclerView rvFollowers;
    private StalkerAdapter mAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        init();
        getData(savedInstanceState);
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
        }
    }

    private void populateFollowersData(ArrayList mUserFollowers) {
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
        Toast.makeText(this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedUser != null)
            outState.putParcelable(USER_KEY, mSelectedUser);
        if (mUserFollowers != null) {
            outState.putParcelableArrayList(FOLLOWERS_KEY, mUserFollowers);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search,menu);
        SearchView svSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
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
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
