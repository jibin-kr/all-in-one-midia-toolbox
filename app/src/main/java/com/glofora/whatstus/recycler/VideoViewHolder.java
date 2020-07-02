package com.glofora.whatstus.recycler;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.glofora.whatstus.R;

/**
 * Created by umer on 01-May-18.
 */

public class VideoViewHolder extends RecyclerView.ViewHolder {


    public ImageView imageView,imageViewCheck,imageViewPlay;


    public VideoViewHolder(View view) {
        super(view);


        this.imageView = (ImageView) view.findViewById(R.id.imageView_wa_image);
        this.imageViewCheck = (ImageView) view.findViewById(R.id.imageView_wa_checked);
        this.imageViewPlay = (ImageView) view.findViewById(R.id.imageView_wa_play);

    }
}