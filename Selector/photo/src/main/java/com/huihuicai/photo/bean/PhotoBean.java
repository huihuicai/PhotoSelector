package com.huihuicai.photo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ybm on 2017/8/2.
 */

public class PhotoBean implements Parcelable {

    public String name;
    public String path;
    public String parent;

    public PhotoBean() {

    }

    protected PhotoBean(Parcel in) {
        name = in.readString();
        path = in.readString();
        parent = in.readString();
    }

    public static final Creator<PhotoBean> CREATOR = new Creator<PhotoBean>() {
        @Override
        public PhotoBean createFromParcel(Parcel in) {
            return new PhotoBean(in);
        }

        @Override
        public PhotoBean[] newArray(int size) {
            return new PhotoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(parent);
    }
}
