package com.glofora.toolbox.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.glofora.toolbox.models.Repost;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface PostsDao {

    @Query("SELECT * FROM posts_recents")
    Single<List<Repost>> getAll();

    @Insert
    void addPost(Repost user);

    @Update
    void updatePost(Repost user);

    @Delete
    void deletePost(Repost user);

}
