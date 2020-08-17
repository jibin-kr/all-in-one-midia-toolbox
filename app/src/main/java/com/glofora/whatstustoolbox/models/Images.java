package com.glofora.whatstustoolbox.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Images implements Parcelable {

    private String name,og_path,path;
    private long id;

    public Images(String name, String og_path, String path, long id) {
        this.name = name;
        this.og_path = og_path;
        this.path = path;
        this.id = id;
    }

    protected Images(Parcel in) {
        name = in.readString();
        og_path = in.readString();
        path = in.readString();
        id = in.readLong();
    }

    public static final Creator<Images> CREATOR = new Creator<Images>() {
        @Override
        public Images createFromParcel(Parcel in) {
            return new Images(in);
        }

        @Override
        public Images[] newArray(int size) {
            return new Images[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOg_path() {
        return og_path;
    }

    public void setOg_path(String og_path) {
        this.og_path = og_path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(og_path);
        dest.writeString(path);
        dest.writeLong(id);

    }
}
