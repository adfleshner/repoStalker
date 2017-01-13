package com.bypassmobile.octo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by aaronfleshner on 1/13/17.
 */

public class ObservedState extends Observable implements Parcelable {

    public enum STATE{LOADING,EMPTY,DATA}
    private STATE mCurrentState = STATE.EMPTY;


    public STATE getCurrentState() {
        return mCurrentState;
    }

    public void setCurrentState(STATE mCurrentState) {
        this.mCurrentState = mCurrentState;
        setChanged();
        notifyObservers();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCurrentState == null ? -1 : this.mCurrentState.ordinal());
    }

    public ObservedState() {
    }

    protected ObservedState(Parcel in) {
        int tmpMCurrentState = in.readInt();
        this.mCurrentState = tmpMCurrentState == -1 ? null : STATE.values()[tmpMCurrentState];
    }

    public static final Parcelable.Creator<ObservedState> CREATOR = new Parcelable.Creator<ObservedState>() {
        @Override
        public ObservedState createFromParcel(Parcel source) {
            return new ObservedState(source);
        }

        @Override
        public ObservedState[] newArray(int size) {
            return new ObservedState[size];
        }
    };
}
