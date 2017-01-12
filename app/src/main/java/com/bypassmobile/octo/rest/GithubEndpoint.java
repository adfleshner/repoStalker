package com.bypassmobile.octo.rest;


import com.bypassmobile.octo.model.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface GithubEndpoint {

    static final String SERVER = "https://api.github.com";

    @GET("/users/{id}")
    void getUser(@Path("id") String user, Callback<User> callback);

    @GET("/users/{id}/following")
    void getFollowingUser(@Path("id") String user, Callback<List<User>> callback);

    @GET("/orgs/{id}/members")
    void getOrganizationMember(@Path("id") String organization, Callback<List<User>> callback);
}
