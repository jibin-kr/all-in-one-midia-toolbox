package com.glofora.toolbox.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.glofora.toolbox.R;
import com.glofora.toolbox.Utls.NotificationUtils;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.domain.DownloadInfo;

import java.io.File;

public class CancelDownloadActivity extends AppCompatActivity {
    private NotificationUtils notificationUtils;
    private com.ixuea.android.downloader.callback.DownloadManager downloadManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationUtils=new NotificationUtils(this);
        downloadManager = DownloadService.getDownloadManager(this);
        onNewIntent(getIntent());



        setContentView(R.layout.activity_cancel_download);
    }
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("downloadID")) {

                // extract the extra-data in the Notification
                String id = extras.getString("downloadID");

                final DownloadInfo downloadInfo=downloadManager.getDownloadById(id);
                downloadManager.remove(downloadInfo);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(CancelDownloadActivity.this);
                notificationManager.deleteNotificationChannel(id);

                    File file = new File(downloadInfo.getPath());
                    boolean deleted = file.delete();

                Toast.makeText(CancelDownloadActivity.this, "Download Cancelled", Toast.LENGTH_SHORT).show();

            }
        }


    }
}