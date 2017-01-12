package com.bypassmobile.octo.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class User implements Parcelable {

    @SerializedName("login")
    private final String name;

    @SerializedName("avatar_url")
    private final String profileURL;

    public User(String name, String profileURL) {
        this.name = name;
        this.profileURL = profileURL;
    }

    public String getName() {
        return name;
    }

    public String getProfileURL() {
        return profileURL;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.profileURL);
    }

    protected User(Parcel in) {
        this.name = in.readString();
        this.profileURL = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
