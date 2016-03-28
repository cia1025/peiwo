package me.peiwo.peiwo.model.agora;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraCallEvent implements Parcelable {


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public AgoraCallEvent() {
    }

    protected AgoraCallEvent(Parcel in) {
    }

}
