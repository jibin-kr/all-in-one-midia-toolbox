package com.glofora.toolbox.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.glofora.toolbox.R;
import com.glofora.toolbox.Utls.HttpHandler;
import com.glofora.toolbox.Utls.NetworkUtil;
import com.glofora.toolbox.Utls.NotificationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.callback.DownloadManager;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HDPictureActivity extends AppCompatActivity {

    private static final String TAG = HDPictureActivity.class.getSimpleName();
    private TextInputEditText username;
    private CircleImageView circleImageView;
    private FloatingActionButton floatingActionButton;
    private ProgressBar progressBar;
    private static String URL;
    private Map<String,Object> map=new HashMap<>();
    private String jsonStr;
    private DownloadManager downloadManager;
    private NotificationUtils notificationUtils;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int theme=getSharedPreferences("theme",MODE_PRIVATE).getInt("mode",2);
        switch (theme){
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdpicture);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        username = findViewById(R.id.username);
        progressBar = findViewById(R.id.pbar);
        circleImageView = findViewById(R.id.profile_pic);
        floatingActionButton = findViewById(R.id.fab);
        notificationUtils=new NotificationUtils(getApplicationContext());

        downloadManager = DownloadService.getDownloadManager(getApplicationContext());
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!map.isEmpty()) {

                    String fileName=map.get("username")+".png";
                    boolean success = true;
                    String directory;
                    File storageDir;

                    directory=getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Glofora Toolbox/");
                    if (!directory.endsWith("/Glofora Toolbox")) {
                        storageDir = new File(directory + "/Instagram/Profile Pictures/");
                    } else {
                        storageDir = new File(directory + "/Instagram/Profile Pictures/");
                    }
                    if (!storageDir.exists()) {
                        success = storageDir.mkdirs();
                    }

                    if(success) {
                        File imageFile = new File(storageDir, fileName);
                        if(imageFile.exists()){
                            Toast.makeText(getApplicationContext(), "File already exists..", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(map.get("profile_pic_url_hd").toString())
                                .setPath(imageFile.getAbsolutePath())
                                .build();
                        downloadInfo.setDownloadListener(new DownloadListener() {
                            @Override
                            public void onStart() {
                                Toast.makeText(getApplicationContext(), "Starting to download...", Toast.LENGTH_SHORT).show();
                            }

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
                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                notificationManager.cancel(downloadInfo.hashCode());
                            }

                            @Override
                            public void onDownloadSuccess() {
                                Intent intent=new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                Uri uri= FileProvider.getUriForFile(getApplicationContext(),getApplicationContext().getPackageName()+".provider",imageFile);
                                intent.setDataAndType(uri,"image/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,intent,0);
                                galleryAddPic(imageFile);
                                Toast.makeText(HDPictureActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                                notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download successful","Downloaded "+fileName,false,pendingIntent);
                            }

                            @Override
                            public void onDownloadFailed(DownloadException e) {
                                Toast.makeText(HDPictureActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                                notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download failed","Couldn't download "+fileName,false, null);
                            }
                        });
                        downloadManager.download(downloadInfo);
                    }

                }
                else{
                    Toast.makeText(HDPictureActivity.this, "Error saving profile picture", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public void getProfilePicture(View view) {

        String username_txt=username.getText().toString();
        username_txt=username_txt.replace("@","");
        if(TextUtils.isEmpty(username_txt) || username_txt.length()<2){
            Toast.makeText(this, "Enter a valid username", Toast.LENGTH_SHORT).show();
            return;
        }

        URL="https://www.instagram.com/"+username_txt+"/?__a=1";
        if(NetworkUtil.getConnectivityStatus(getApplicationContext())!=NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            new GetData().execute();
        }else{
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public static String formatFileSize(long size) {
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

    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(floatingActionButton.isShown()){
                Animation animation= AnimationUtils.loadAnimation(HDPictureActivity.this,R.anim.scale_down);
                floatingActionButton.startAnimation(animation);
                floatingActionButton.hide();
            }

            Glide.with(HDPictureActivity.this)
                    .load(R.drawable.ic_account)
                    .into(circleImageView);

            progressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.button).setEnabled(false);
            username.setEnabled(false);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            map.clear();
            HttpHandler sh = new HttpHandler();
            jsonStr = sh.makeServiceCall(URL);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONObject graphql = jsonObj.getJSONObject("graphql");
                    JSONObject user = graphql.getJSONObject("user");
                    JSONObject edge_followed_by=user.getJSONObject("edge_followed_by");
                    int followers_count=edge_followed_by.getInt("count");
                    JSONObject edge_follow=user.getJSONObject("edge_follow");
                    int following_count=edge_follow.getInt("count");
                    String full_name=user.getString("full_name");
                    String profile_pic_url_hd=user.getString("profile_pic_url_hd");
                    String username=user.getString("username");

                    map.put("profile_pic_url_hd",profile_pic_url_hd);
                    map.put("username",username);
                    map.put("full_name",full_name);
                    map.put("followers_count",followers_count);
                    map.put("following_count",following_count);

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Some technical error occurred",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(TextUtils.isEmpty(jsonStr)) {

                            Toast.makeText(getApplicationContext(),
                                    "No user found",
                                    Toast.LENGTH_LONG)
                                    .show();

                        }else{

                            Toast.makeText(getApplicationContext(),
                                    "Some technical error occurred",
                                    Toast.LENGTH_LONG)
                                    .show();

                        }
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            findViewById(R.id.button).setEnabled(true);
            username.setEnabled(true);

            if(!map.isEmpty()) {

                Animation animation= AnimationUtils.loadAnimation(HDPictureActivity.this,R.anim.scale_up);
                floatingActionButton.startAnimation(animation);
                floatingActionButton.show();

                Glide.with(HDPictureActivity.this)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_account))
                        .load(map.get("profile_pic_url_hd"))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(circleImageView);
            }else{
                progressBar.setVisibility(View.GONE);
            }
        }
    }


}
