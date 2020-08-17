package com.glofora.whatstustoolbox.recycler;


import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.glofora.whatstustoolbox.R;

/**
 * Created by SONU on 27/03/16.
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {


    public ImageView imageView,imageViewCheck;


    public ImageViewHolder(View view) {
        super(view);


        this.imageView = (ImageView) view.findViewById(R.id.imageView_wa_image);
        this.imageViewCheck = (ImageView) view.findViewById(R.id.imageView_wa_checked);

    }
}