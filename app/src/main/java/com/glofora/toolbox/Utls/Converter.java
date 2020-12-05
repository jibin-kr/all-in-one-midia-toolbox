package com.glofora.toolbox.Utls;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converter {

    @TypeConverter
    public static ArrayList<String> fromString(String data){
        Type listType=new TypeToken<ArrayList<String>>(){}.getType();
        return new Gson().fromJson(data,listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<String> data){
        Gson gson=new Gson();
        String json=gson.toJson(data);
        return json;
    }

}
