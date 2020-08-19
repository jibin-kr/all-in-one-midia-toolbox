package com.glofora.whatstustoolbox.adapter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.glofora.whatstustoolbox.R;
import com.glofora.whatstustoolbox.Utls.NotificationUtils;
import com.glofora.whatstustoolbox.activity.VideoDownloaderDialogActivity;
import com.glofora.whatstustoolbox.models.Video;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.callback.DownloadManager;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.glofora.whatstustoolbox.activity.RepostActivity.formatFileSize;


public class SingleVideoAdapter extends RecyclerView.Adapter<SingleVideoAdapter.ViewHolder> {

    private Context context;
    private List<Video> videos;
    private Activity activity;
    private NotificationUtils notificationUtils;

    public SingleVideoAdapter(List<Video> video, Activity activity) {
        this.videos=video;
        this.activity=activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_youtube_single_video, parent, false);
        context=parent.getContext();
        notificationUtils=new NotificationUtils(context);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int i) {

        final Video video=videos.get(i);

        holder.title.setText(video.getTitle());

        Glide.with(context)
                .load(video.getThumbnail_url())
                .into(holder.thumbnail);

        //Load later
        Glide.with(context)
                .load(video.getThumbnail_max_url())
                .into(holder.thumbnail);

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(context)
                        .setTitle("Download")
                        .setIcon(R.drawable.ic_file_download)
                        .setMessage("Which you want to download?")
                        .setCancelable(true)
                        .setPositiveButton("Video", (dialog, which) -> {
                            dialog.dismiss();
                            download(video.getTitle(),video.getVideo_url(),null,"video");
                        })
                        .setNegativeButton("Thumbnail", (dialog, which) -> {
                            dialog.dismiss();
                            download(video.getTitle(),null,video.getThumbnail_max_url(),"thumbnail");

                        })
                        .show();

            }
        });

    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private void downloadThumbnail(String title,final String url,String type) {

        DownloadManager downloadManager = DownloadService.getDownloadManager(context);
        String fileName=title+".jpg";

        boolean success = true;
        String directory;
        File storageDir;

        directory=context.getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Whatstus/");
        storageDir = new File(directory + "/Youtube/Thumbnails/");

        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if(success) {
            File imageFile = new File(storageDir, fileName);
            if (imageFile.exists()) {
                Toast.makeText(context, "File already exists..", Toast.LENGTH_SHORT).show();
                return;
            }

            final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(url)
                    .setPath(imageFile.getAbsolutePath())
                    .build();
            downloadInfo.setDownloadListener(new DownloadListener() {
                @Override
                public void onStart() {
                    Toast.makeText(context, "Starting to download...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onWaited() {
                }

                @Override
                public void onPaused() {
                }

                @Override
                public void onDownloading(long progress, long size) {
                    notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Downloading " + fileName, "Downloaded " + formatFileSize(progress) + "/" + formatFileSize(size), true, null);
                }

                @Override
                public void onRemoved() {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.cancel(downloadInfo.hashCode());
                }

                @Override
                public void onDownloadSuccess() {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", imageFile);
                    intent.setDataAndType(uri, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    galleryAddPic(imageFile);
                    Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
                    notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download successful", "Downloaded " + fileName, false, pendingIntent);
                }

                @Override
                public void onDownloadFailed(DownloadException e) {
                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                    notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download failed", "Couldn't download " + fileName, false, null);
                }
            });
            downloadManager.download(downloadInfo);
        }

    }

    private void download(final String video_title,final String video_url,final String thumb_url, final String type) {

        Dexter.withActivity(activity)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(isOnline()) {

                            switch (type){

                                case "video":
                                    context.startActivity(new Intent(context, VideoDownloaderDialogActivity.class)
                                            .putExtra("url",video_url));
                                    return;
                                case "thumbnail":
                                    Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();
                                    downloadThumbnail(video_title,thumb_url,type);

                            }

                        }else{
                            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            DialogOnDeniedPermissionListener.Builder
                                    .withContext(context)
                                    .withTitle("Storage permission")
                                    .withMessage("Storage permission is needed for downloading images.")
                                    .withButtonText(android.R.string.ok)
                                    .build();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

       KenBurnsView thumbnail;
       ExtendedFloatingActionButton download;
       TextView title;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail=itemView.findViewById(R.id.thumbnail);
            download=itemView.findViewById(R.id.download);
            title=itemView.findViewById(R.id.title);

        }
    }

}
