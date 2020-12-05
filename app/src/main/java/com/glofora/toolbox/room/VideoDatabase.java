package com.glofora.toolbox.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.glofora.toolbox.models.Video;


@Database(entities = Video.class,version = 1,exportSchema = false)
public abstract class VideoDatabase extends RoomDatabase {

    private static final String DB_NAME="videos_recents_db";
    private static VideoDatabase instance;

    public static synchronized VideoDatabase getInstance(Context context){
        if(instance==null){
            instance= Room.databaseBuilder(context.getApplicationContext(), VideoDatabase.class,DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract VideoDao videoDao();
}
