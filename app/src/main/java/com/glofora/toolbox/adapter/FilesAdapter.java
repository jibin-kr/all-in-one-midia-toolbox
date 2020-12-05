package com.glofora.toolbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.glofora.toolbox.R;
import com.glofora.toolbox.model.Status;
import com.glofora.toolbox.viewer.ImageViewer;
import com.glofora.toolbox.viewer.VideoPlayer;
import com.squareup.picasso.Picasso;


import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private List<Status> imagesList;
    private View savedItemsView;
    private Context context;

    public FilesAdapter(List<Status> imagesList,View view) {
        this.imagesList = imagesList;
        this.savedItemsView=view;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_files, parent, false);
        return new ItemViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {

        holder.save.setVisibility(View.VISIBLE);
        holder.share.setVisibility(View.VISIBLE);

        final Status status = imagesList.get(position);

        if (status.isVideo()) {
            holder.imageView.setImageBitmap(status.getThumbnail());
            holder.videoBG.setVisibility(View.VISIBLE);
        }
        else
        {
            Picasso.get().load(status.getFile()).into(holder.imageView);
            holder.videoBG.setVisibility(View.INVISIBLE);

        }

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                if (status.isVideo())
                    shareIntent.setType("image/mp4");
                else
                    shareIntent.setType("image/jpg");

                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+status.getFile().getAbsolutePath()));
                context.startActivity(Intent.createChooser(shareIntent, "Share image"));

            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (status.isVideo()){
                    try {
                        Intent intent = new Intent(context, VideoPlayer.class);
                        intent.putExtra("pos", status.getPath());
                        intent.putExtra("position", position);
                        intent.putExtra("is_saved", true);
                        ((Activity) context).startActivityForResult(intent, 1);
                    } catch (Throwable e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
//
//                    final AlertDialog.Builder alertDg = new AlertDialog.Builder(context);
//
//                    FrameLayout mediaControls = view1.findViewById(R.id.videoViewWrapper);
//
//                    if (view1.getParent() != null){
//                        ((ViewGroup)view1.getParent()).removeView(view1);
//                    }
//
//                    alertDg.setView(view1);
//
//                    final VideoView videoView = view1.findViewById(R.id.video_full);
//
//                    final MediaController mediaController = new MediaController(context, false);
//
//                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//
//                            mp.start();
//                            mediaController.show(0);
//                            mp.setLooping(true);
//                        }
//                    });
//
//                    videoView.setMediaController(mediaController);
//                    mediaController.setMediaPlayer(videoView);
//                    videoView.setVideoURI(Uri.fromFile(status.getFile()));
//                    videoView.requestFocus();
//
//                    ((ViewGroup) mediaController.getParent()).removeView(mediaController);
//
//                    if (mediaControls.getParent() != null){
//                        mediaControls.removeView(mediaController);
//                    }
//
//                    mediaControls.addView(mediaController);
//
//                    final AlertDialog alert2 = alertDg.create();
//
//                    alert2.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
//                    alert2.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                    alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                    alert2.show();

                }else {

                    try {
                        Intent intent = new Intent(context, ImageViewer.class);
                        intent.putExtra("pos", status.getPath());
                        intent.putExtra("position", position);
                        intent.putExtra("is_saved", true);
                        ((Activity) context).startActivityForResult(intent, 1);
                    } catch (Throwable e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
//
//                    final AlertDialog.Builder alertD = new AlertDialog.Builder(context);
//                    LayoutInflater inflater = LayoutInflater.from(context);
//                    View view = inflater.inflate(R.layout.view_image_full_screen, null);
//                    alertD.setView(view);
//
//                    ImageView imageView = view.findViewById(R.id.img);
//                    Picasso.get().load(status.getFile()).into(imageView);
//
//                    AlertDialog alert = alertD.create();
//                    alert.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
//                    alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                    alert.show();

                }

            }
        });
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(status.getFile().exists()) {
                  status.getFile().delete();
              }
              imagesList.remove(position);
              FilesAdapter.this.notifyDataSetChanged();
                if(imagesList.isEmpty()){
//                    defaultLayout.setVisibility(View.VISIBLE);
                    savedItemsView.findViewById(R.id.no_files_found).setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

}
