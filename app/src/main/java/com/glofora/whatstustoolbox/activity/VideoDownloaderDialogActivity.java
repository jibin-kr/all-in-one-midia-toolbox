package com.glofora.whatstustoolbox.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;


import com.glofora.whatstustoolbox.R;
import com.glofora.whatstustoolbox.Utls.NotificationUtils;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class VideoDownloaderDialogActivity extends AppCompatActivity {

    private static final int ITAG_FOR_AUDIO = 140;

    private static String youtubeLink;

    private LinearLayout mainLayout;
    private ProgressBar mainProgressBar;
    private NotificationUtils notificationUtils;
    private com.ixuea.android.downloader.callback.DownloadManager downloadManager;
    private List<YtFragmentedVideo> formatsToShowList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        mainLayout = findViewById(R.id.main_layout);
        mainProgressBar =findViewById(R.id.progress_bar);
        notificationUtils=new NotificationUtils(this);
        downloadManager = DownloadService.getDownloadManager(this);
        getYoutubeDownloadUrl(getIntent().getStringExtra("url"));

    }

    @SuppressLint("StaticFieldLeak")
    private void getYoutubeDownloadUrl(String youtubeLink) {
        new YouTubeExtractor(this) {

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                mainProgressBar.setVisibility(View.GONE);
                if (ytFiles == null) {

                    finish();
                    return;
                }
                formatsToShowList = new ArrayList<>();
                for (int i = 0, itag; i < ytFiles.size(); i++) {
                    itag = ytFiles.keyAt(i);
                    YtFile ytFile = ytFiles.get(itag);

                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        addFormatToList(ytFile, ytFiles);
                    }
                }
                Collections.sort(formatsToShowList, new Comparator<YtFragmentedVideo>() {
                    @Override
                    public int compare(YtFragmentedVideo lhs, YtFragmentedVideo rhs) {
                        return lhs.height - rhs.height;
                    }
                });
                for (YtFragmentedVideo files : formatsToShowList) {
                    addButtonToMainLayout(vMeta.getTitle(), files);
                }
            }
        }.extract(youtubeLink, true, false);
    }

    private void addFormatToList(YtFile ytFile, SparseArray<YtFile> ytFiles) {
        int height = ytFile.getFormat().getHeight();
        if (height != -1) {
            for (YtFragmentedVideo frVideo : formatsToShowList) {
                if (frVideo.height == height && (frVideo.videoFile == null ||
                        frVideo.videoFile.getFormat().getFps() == ytFile.getFormat().getFps())) {
                    return;
                }
            }
        }
        YtFragmentedVideo frVideo = new YtFragmentedVideo();
        frVideo.height = height;
        if (ytFile.getFormat().isDashContainer()) {
            if (height > 0) {
                frVideo.videoFile = ytFile;
                frVideo.audioFile = ytFiles.get(ITAG_FOR_AUDIO);
            } else {
                frVideo.audioFile = ytFile;
            }
        } else {
            frVideo.videoFile = ytFile;
        }
        formatsToShowList.add(frVideo);
    }


    private void addButtonToMainLayout(final String videoTitle, final YtFragmentedVideo ytFrVideo) {
        String btnText;
        if (ytFrVideo.height == -1)

            btnText = "Audio " + ytFrVideo.audioFile.getFormat().getAudioBitrate() + " kbit/s";
        else
            btnText = (ytFrVideo.videoFile.getFormat().getFps() == 60) ? ytFrVideo.height + "p60" :
                    ytFrVideo.height + "p";
        Button btn = new Button(this);
        btn.setText(btnText);
        btn.setOnClickListener(v -> {
            String filename;
            if (videoTitle.length() > 55) {
                filename = videoTitle.substring(0, 55);
            } else {
                filename = videoTitle;
            }
            filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
            filename += (ytFrVideo.height == -1) ? "" : "-" + ytFrVideo.height + "p";
            String downloadIds = "";

            if (ytFrVideo.videoFile != null) {

                downloadNow(ytFrVideo.videoFile.getUrl(), videoTitle,
                        filename + "." + ytFrVideo.videoFile.getFormat().getExt(),"." +ytFrVideo.videoFile.getFormat().getExt());


            }
            if (ytFrVideo.audioFile != null) {
                downloadNow(ytFrVideo.audioFile.getUrl(), videoTitle,
                        filename + "." + ytFrVideo.audioFile.getFormat().getExt(),"." + ytFrVideo.audioFile.getFormat().getExt());

            }
            if (ytFrVideo.audioFile != null)
                cacheDownloadIds(downloadIds);
            finish();
        });

        mainLayout.addView(btn);
    }

    private long downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName, boolean hide) {
        Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show();
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        String directory = getSharedPreferences("directory", Context.MODE_PRIVATE).getString("path", Environment.getExternalStorageDirectory().toString() + "/Whatstus");
        request.setDestinationUri(Uri.fromFile(new File(directory+"/Youtube/"+fileName)));

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }
    private void downloadNow(String youtubeDlUrl, String downloadTitle, String fileName,String fileExtention){



            boolean success = true;
            String directory;
            File storageDir;
            directory=getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Whatstus/");

            storageDir = new File(directory + "/Youtube/");

            if (!storageDir.exists()) {
                success = storageDir.mkdirs();
            }

            if(success) {

                File imageFile = new File(storageDir, fileName);
                if(imageFile.exists()){

                    Date dNow = new Date();
                    SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
                    String datetime = ft.format(dNow);
                    downloadNow(youtubeDlUrl,downloadTitle,downloadTitle+"_"+datetime+fileExtention,fileExtention);

                }else
                {
                    final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(youtubeDlUrl)
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
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(VideoDownloaderDialogActivity.this);
                            notificationManager.cancel(downloadInfo.hashCode());
                        }

                        @Override
                        public void onDownloadSuccess() {
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri uri= FileProvider.getUriForFile(VideoDownloaderDialogActivity.this,getPackageName()+".provider",imageFile);
                            intent.setDataAndType(uri,"video/*");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            PendingIntent pendingIntent=PendingIntent.getActivity(VideoDownloaderDialogActivity.this,0,intent,0);
                            galleryAddPic(imageFile);
                            Toast.makeText(VideoDownloaderDialogActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                            notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download successful","Downloaded "+fileName,false,pendingIntent);
                        }

                        @Override
                        public void onDownloadFailed(DownloadException e) {
                            Toast.makeText(VideoDownloaderDialogActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                            notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download failed","Couldn't download "+fileName,false, null);
                        }
                    });

                    downloadManager.download(downloadInfo);
                }


            }


    }
    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
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
    private void cacheDownloadIds(String downloadIds) {
        File dlCacheFile = new File(this.getCacheDir().getAbsolutePath() + "/" + downloadIds);
        try {
            dlCacheFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class YtFragmentedVideo {
        int height;
        YtFile audioFile;
        YtFile videoFile;
    }


}
