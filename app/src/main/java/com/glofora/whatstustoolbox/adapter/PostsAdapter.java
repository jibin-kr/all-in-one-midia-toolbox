package com.glofora.whatstustoolbox.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.glofora.whatstustoolbox.R;
import com.glofora.whatstustoolbox.Utls.NotificationUtils;
import com.glofora.whatstustoolbox.models.Repost;
import com.glofora.whatstustoolbox.room.PostsDatabase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.callback.DownloadManager;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private boolean recents;
    private List<Repost> repostLists;
    private Context context;
    private LinearLayout defaultLayout;
    private NotificationUtils notificationUtils;
    private DownloadManager downloadManager;

    public PostsAdapter(List<Repost> repostLists, Context context, boolean recents, LinearLayout defaultLayout) {
        this.recents=recents;
        this.defaultLayout=defaultLayout;
        this.repostLists = repostLists;
        this.context = context;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_post,parent,false));
    }

    private static String formatFileSize(long size) {
        String sFileSize = "";
        if (size > 0) {
            double dFileSize = (double) size;

            double kiloByte = dFileSize / 1024;
            if (kiloByte < 1 && kiloByte > 0) {
                return size + "Byte";
            }
            double megaByte = kiloByte / 1024;
            if (megaByte < 1) {
                sFileSize = String.format("%.2f", kiloByte);
                return sFileSize + "K";
            }

            double gigaByte = megaByte / 1024;
            if (gigaByte < 1) {
                sFileSize = String.format("%.2f", megaByte);
                return sFileSize + "M";
            }

            double teraByte = gigaByte / 1024;
            if (teraByte < 1) {
                sFileSize = String.format("%.2f", gigaByte);
                return sFileSize + "G";
            }

            sFileSize = String.format("%.2f", teraByte);
            return sFileSize + "T";
        }
        return "0K";
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Repost repost=repostLists.get(position);
        notificationUtils=new NotificationUtils(context);
        downloadManager = DownloadService.getDownloadManager(context);

        if(!recents){
            holder.buttonsLayout.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }else{

            holder.download.setOnClickListener(v -> {

                if(!repostLists.isEmpty()){

                    Toast.makeText(context, "Starting to download...", Toast.LENGTH_SHORT).show();
                    for(String url:repost.getDisplay_url()) {

                        String[] text = url.split("\\?");
                        String[] code=text[0].split("/");
                        String fileName=code[code.length-1];

                        boolean success = true;
                        String directory;
                        File storageDir;

//                            directory=context.getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Whatstus/");
                        directory=context.getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Whatstus/");
                        storageDir = new File(directory + "/Instagram/Whatstus Repost/");

                        if (!storageDir.exists()) {
                            success = storageDir.mkdirs();
                        }

                        if(success){
                            File imageFile = new File(storageDir, fileName);
                            if(imageFile.exists()){
                                Toast.makeText(context, "File already exists..", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(url)
                                    .setPath(imageFile.getAbsolutePath())
                                    .build();
                            downloadInfo.setDownloadListener(new DownloadListener() {
                                @Override
                                public void onStart() { }

                                @Override
                                public void onWaited() { }

                                @Override
                                public void onPaused() { }

                                @Override
                                public void onDownloading(long progress, long size) {
                                    notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Downloading "+fileName,"Downloaded "+formatFileSize(progress)+"/"+formatFileSize(size),true, null);
                                }

                                @Override
                                public void onRemoved() {
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                    notificationManager.cancel(downloadInfo.hashCode());
                                }

                                @Override
                                public void onDownloadSuccess() {
                                    Intent intent=new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    Uri uri= FileProvider.getUriForFile(context,context.getPackageName()+".provider",imageFile);
                                    intent.setDataAndType(uri,"image/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
                                    galleryAddPic(imageFile);
                                    Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
                                    notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download successful","Downloaded "+fileName,false,pendingIntent);
                                }

                                @Override
                                public void onDownloadFailed(DownloadException e) {
                                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                                    notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download failed","Couldn't download "+fileName,false, null);
                                }
                            });
                            downloadManager.download(downloadInfo);
                        }

                    }
                    for(String url:repost.getVideo_urls()) {

                        String[] text = url.split("\\?");
                        String[] code=text[0].split("/");
                        String fileName=code[code.length-1];

                        boolean success = true;
                        String directory;
                        File storageDir;

//                            directory=context.getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Whatstus/");
                        directory=context.getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Whatstus/");

                        storageDir = new File(directory + "/Instagram/Whatstus Repost/");

                        if (!storageDir.exists()) {
                            success = storageDir.mkdirs();
                        }

                        if(success) {

                            File imageFile = new File(storageDir, fileName);
                            if(imageFile.exists()){
                                Toast.makeText(context, "File already exists..", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(url)
                                    .setPath(imageFile.getAbsolutePath())
                                    .build();
                            downloadInfo.setDownloadListener(new DownloadListener() {
                                @Override
                                public void onStart() { }

                                @Override
                                public void onWaited() { }

                                @Override
                                public void onPaused() { }

                                @Override
                                public void onDownloading(long progress, long size) {
                                    notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Downloading "+fileName,"Downloaded "+formatFileSize(progress)+"/"+formatFileSize(size),true, null);
                                }

                                @Override
                                public void onRemoved() {
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                    notificationManager.cancel(downloadInfo.hashCode());
                                }

                                @Override
                                public void onDownloadSuccess() {
                                    Intent intent=new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    Uri uri= FileProvider.getUriForFile(context,context.getPackageName()+".provider",imageFile);
                                    intent.setDataAndType(uri,"video/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
                                    galleryAddPic(imageFile);
                                    Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
                                    notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download successful","Downloaded "+fileName,false,pendingIntent);
                                }

                                @Override
                                public void onDownloadFailed(DownloadException e) {
                                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                                    notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download failed","Couldn't download "+fileName,false, null);
                                }
                            });
                            downloadManager.download(downloadInfo);

                        }

                    }

                }

            });

            holder.delete.setOnClickListener(v -> {

                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(context);
                builder.setTitle("Delete post");
                builder.setMessage("Are you sure do you want to remove this post from your recents?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                PostsDatabase.getInstance(context).postsDao().deletePost(repost);
                            }
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        repostLists.remove(repost);
                                        notifyItemRemoved(position);

                                        if(repostLists.isEmpty()){
                                            defaultLayout.setVisibility(View.VISIBLE);
                                        }

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }
                                });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();

            });
        }

        holder.username.setText(String.format("@%s",repost.getUsername()));
        holder.timestamp.setText(new PrettyTime().format(new Date(System.currentTimeMillis())));
        Glide.with(context)
                .load(repost.getProfile_pic_url())
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        if(recents){
                            holder.caption.setVisibility(View.GONE);
                        }else{
                            holder.caption.setText(repost.getCaption());

                        }

                        if (repost.getDisplay_url().size() == 1) {
                            PagerPhotosAdapter photosAdapter = new PagerPhotosAdapter(context, repost.getDisplay_url(),repost.getVideo_urls());
                            holder.pager.setAdapter(photosAdapter);
                            holder.indicator_holder.setVisibility(View.GONE);
                            photosAdapter.notifyDataSetChanged();
                            return false;
                        }

                        if (repost.getDisplay_url().size() > 1) {
                            PagerPhotosAdapter photosAdapter = new PagerPhotosAdapter(context, repost.getDisplay_url(),repost.getVideo_urls());
                            holder.pager.setAdapter(photosAdapter);
                            holder.indicator_holder.setVisibility(View.VISIBLE);
                            holder.indicator.setDotsClickable(true);
                            holder.indicator.setViewPager(holder.pager);
                            photosAdapter.notifyDataSetChanged();
                        }
                        return false;

                    }
                })
                .into(holder.user_image);

    }

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    @Override
    public int getItemCount() {
        return repostLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username,timestamp,caption;
        private DotsIndicator indicator;
        private RelativeLayout indicator_holder;
        private ViewPager pager;
        private CircleImageView user_image;
        private ImageView delete;
        private Button download,copy;
        private LinearLayout buttonsLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.post_username);
            timestamp=itemView.findViewById(R.id.post_timestamp);
            download=itemView.findViewById(R.id.download);
            copy=itemView.findViewById(R.id.copy);
            buttonsLayout=itemView.findViewById(R.id.buttonsLayout);
            caption=itemView.findViewById(R.id.post_desc);
            indicator=itemView.findViewById(R.id.indicator);
            indicator_holder=itemView.findViewById(R.id.indicator_holder);
            pager=itemView.findViewById(R.id.pager);
            user_image=itemView.findViewById(R.id.post_user_image);
            delete=itemView.findViewById(R.id.delete_button);

        }
    }

}
