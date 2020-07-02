package com.glofora.whatstus.room;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.glofora.whatstus.Utls.Converter;
import com.glofora.whatstus.models.Repost;


@Database(entities = Repost.class,version = 1,exportSchema = false)
@TypeConverters({Converter.class})
public abstract class PostsDatabase extends RoomDatabase {

    private static final String DB_NAME="posts_recents_db";
    private static PostsDatabase instance;

    public static synchronized PostsDatabase getInstance(Context context){
        if(instance==null){
            instance= Room.databaseBuilder(context.getApplicationContext(),PostsDatabase.class,DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract PostsDao postsDao();
}
