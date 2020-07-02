package com.glofora.whatstus.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;


import com.bumptech.glide.Glide;
import com.glofora.whatstus.HifyImageView;
import com.glofora.whatstus.R;

import java.util.List;

public class PagerPhotosAdapter extends PagerAdapter {

    private List<String> IMAGES;
    private Context context;
    private LayoutInflater inflater;
    private List<String> VIDEOLINKS;

    public PagerPhotosAdapter(Context context, List<String> IMAGES, List<String> VIDEOLINKS) {
        this.context = context;
        this.IMAGES =IMAGES;
        this.VIDEOLINKS=VIDEOLINKS;
        inflater= LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.item_viewpager_image, view, false);

        assert imageLayout !=null;
        HifyImageView imageView=imageLayout.findViewById(R.id.image);
        ImageView playButton=imageLayout.findViewById(R.id.play);

        Glide.with(context)
                .load(IMAGES.get(position))
                .into(imageView);

        if(!VIDEOLINKS.get(position).equals("null")){

            playButton.setVisibility(View.VISIBLE);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }else playButton.setVisibility(View.GONE);

        view.addView(imageLayout,0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
