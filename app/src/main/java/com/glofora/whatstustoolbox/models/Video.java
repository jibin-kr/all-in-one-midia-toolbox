package com.glofora.whatstustoolbox.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "video_recents")

public class Video {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "video_url")
    private String video_url;
    @ColumnInfo(name = "thumbnail_max_url")
    private String thumbnail_max_url;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnail_url;
    @ColumnInfo(name = "author")
    private String author;
    @ColumnInfo(name = "title")
    private String title;

    public Video(){

    }

    @Ignore
    public Video(String video_url, String thumbnail_max_url, String thumbnail_url, String title, String author) {
        this.video_url = video_url;
        this.thumbnail_max_url = thumbnail_max_url;
        this.thumbnail_url = thumbnail_url;
        this.title = title;
        this.author=author;
    }


    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail_max_url() {
        return thumbnail_max_url;
    }

    public void setThumbnail_max_url(String thumbnail_max_url) {
        this.thumbnail_max_url = thumbnail_max_url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
