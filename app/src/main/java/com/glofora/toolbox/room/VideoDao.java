package com.glofora.toolbox.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.glofora.toolbox.models.Video;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface VideoDao {

    @Query("SELECT * FROM video_recents")
    Single<List<Video>> getAll();

    @Insert
    void addVideo(Video video);

    @Update
    void updateVideo(Video video);

    @Delete
    void deleteVideo(Video video);

}
