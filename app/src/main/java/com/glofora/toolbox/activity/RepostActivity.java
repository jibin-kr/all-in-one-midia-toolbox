package com.glofora.toolbox.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glofora.toolbox.Gallery.Glide4Engine;
import com.glofora.toolbox.R;
import com.glofora.toolbox.Utls.HttpHandler;
import com.glofora.toolbox.Utls.NetworkUtil;
import com.glofora.toolbox.Utls.NotificationUtils;
import com.glofora.toolbox.Utls.Utilities;
import com.glofora.toolbox.adapter.PostsAdapter;
import com.glofora.toolbox.models.Repost;
import com.glofora.toolbox.room.PostsDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.callback.DownloadManager;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.glofora.toolbox.Utls.Utilities.PICKER_REQUEST_CODE;


public class RepostActivity extends AppCompatActivity {

    private static final String TAG = RepostActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ArrayList<String> display_urls=new ArrayList<>();
    private ArrayList<String> video_urls=new ArrayList<>();
    private ArrayList<String> img_urls=new ArrayList<>();

    private List<String> thumbnails=new ArrayList<>();
    List<Uri> mSelected;
    private static String URL;
    private RecyclerView recyclerView;
    private PostsAdapter adapter;
    private List<Repost> repostList;
    private Boolean instant_download;
    private FloatingActionButton fab;
    private DownloadManager downloadManager;
    private NotificationUtils notificationUtils;
    private TextInputEditText txtInstaLink;

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
        setContentView(R.layout.activity_repost);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(3.0f);

        fab=findViewById(R.id.fab);
        txtInstaLink=findViewById(R.id.txtInstaLink);
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        downloadManager = DownloadService.getDownloadManager(getApplicationContext());
        notificationUtils=new NotificationUtils(this);

        repostList=new ArrayList<>();
        adapter=new PostsAdapter(repostList,this,false, null);
        recyclerView.setAdapter(adapter);
        fab.setVisibility(View.GONE);

        String url=getIntent().getStringExtra("url");
        loadInstagranPost(url,false);
        if (TextUtils.isEmpty(url)) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            try {
                url = String.valueOf(clipboard.getPrimaryClip().getItemAt(0).getText());
                loadInstagranPost(url, false);
            } catch (Exception e) {
  return;
            }
        }

        findViewById(R.id.default_layout).setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void openInstagram(View view) {
        String url="https://www.instagram.com";
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
        intent.setPackage("com.instagram.android");
        try {
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(RepostActivity.this,"Instagram not found",Toast.LENGTH_SHORT).show();
        }
    }
    public void loadIGPostFromURL(View view) {
        repostList=new ArrayList<>();
        adapter=new PostsAdapter(repostList,this,false, null);
        recyclerView.setAdapter(adapter);
        String url;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        try {
             url =String.valueOf( clipboard.getPrimaryClip().getItemAt(0).getText());
            loadInstagranPost(url,false);
        } catch (Exception e) {
            Toast.makeText(RepostActivity.this, "No Instagram URL Found", Toast.LENGTH_SHORT).show();

            return;
        }





    }
    public void oneClickDowload(View view) {
        repostList=new ArrayList<>();
        adapter=new PostsAdapter(repostList,this,false, null);
        recyclerView.setAdapter(adapter);
        fab.setVisibility(View.GONE);
        String url;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            url =String.valueOf( clipboard.getPrimaryClip().getItemAt(0).getText());
            loadInstagranPost(url,true);
        } catch (Exception e) {
            Toast.makeText(RepostActivity.this, "No Instagram URL Found", Toast.LENGTH_SHORT).show();

            return;
        }





    }

    public void loadInstagranPost(String url,boolean download){
        if (!TextUtils.isEmpty(url)) {
            findViewById(R.id.default_layout).setVisibility(View.GONE);
            int index=url.lastIndexOf("/");
            if(index>0){
                url=url.substring(0,index+1);
            }
            instant_download=download;

            URL=url+"?__a=1";
            if(URL.contains("://www.instagram.com/")){
                if(NetworkUtil.getConnectivityStatus(getApplicationContext())!=NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    new GetData().execute();

                }else{
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.default_layout).setVisibility(View.VISIBLE);
                }
            }else {
                Toast.makeText(RepostActivity.this, "No Instagram URL Found", Toast.LENGTH_SHORT).show();
                findViewById(R.id.default_layout).setVisibility(View.VISIBLE);
            }
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.recents_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.recents){
            startActivity(new Intent(RepostActivity.this,RecentsActivity.class).putExtra("type","instagram"));
        }
        if(item.getItemId()==R.id.hd_pro_pick){
            startActivity(new Intent(RepostActivity.this,HDPictureActivity.class));
        }
        if(item.getItemId()==R.id.newDownload)
        {
            repostList=new ArrayList<>();
            adapter=new PostsAdapter(repostList,this,false, null);
            recyclerView.setAdapter(adapter);
            fab.setVisibility(View.GONE);
            findViewById(R.id.default_layout).setVisibility(View.VISIBLE);

        }
        if(item.getItemId()==R.id.idGallery){
            ShowPicker();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that displays the image/video chooser.
     */
    public void ShowPicker() {
        Matisse.from(RepostActivity.this)
                .choose(MimeType.ofAll(), false)
                .countable(true)
                .maxSelectable(30)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(PICKER_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            /**
             * In App Gallery Activity result.
             */
            if (requestCode == PICKER_REQUEST_CODE) {
                mSelected = Matisse.obtainResult(data);

                /**
                 * Call this method by passing File URL List and context.
                 */
                Utilities.shareMultiplefiles(mSelected, RepostActivity.this);
            }
        }
    }
    public void downloadPost(View view) {

        downLoad();

    }
    public void downLoad(){
        if(!repostList.isEmpty()){
            Toast.makeText(this, "Starting to download...", Toast.LENGTH_SHORT).show();
            for(String url:repostList.get(0).getImg_urls()) {

                //StringBuilder fileName= new StringBuilder("RE_" + repostList.get(0).getUsername() + "_" + System.currentTimeMillis());
                String[] text = url.split("\\?");
                String[] code=text[0].split("/");
                String fileName=code[code.length-1];

                boolean success = true;
                String directory;
                File storageDir;

                directory=getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Glofora Toolbox/");
                if (!directory.endsWith("/Glofora Toolbox")) {
                    storageDir = new File(directory + "/Instagram/Repost/");
                } else {
                    storageDir = new File(directory + "/Instagram/Repost/");
                }
                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }

                if(success){

                    File imageFile = new File(storageDir, fileName);
                    if(imageFile.exists()){
                        Toast.makeText(getApplicationContext(), "Some Files already exists..", Toast.LENGTH_SHORT).show();

                    }else{
                        final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(url)
                                .setPath(imageFile.getAbsolutePath())
                                .build();
                        downloadInfo.setDownloadListener(new DownloadListener() {
                            @Override
                            public void onStart() {

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
//
                                galleryAddPic(imageFile);
//
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            intent= new Intent(getApplicationContext(), HomeActivity.class);

                                PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,intent,0);
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    v.vibrate(500);
                                }
                                notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download successful","Downloaded "+fileName,false,pendingIntent);

                            }

                            @Override
                            public void onDownloadFailed(DownloadException e) {
                                notificationUtils.showNotification(downloadInfo.hashCode(),downloadInfo.getId(),"Download failed","Couldn't download "+fileName,false, null);
                            }
                        });
                        downloadManager.download(downloadInfo);
                    }



                }

            }
            for(String url:repostList.get(0).getVideo_urls()) {

                //StringBuilder fileName= new StringBuilder("RE_" + repostList.get(0).getUsername() + "_" + System.currentTimeMillis());
                String[] text = url.split("\\?");
                String[] code=text[0].split("/");
                String fileName=code[code.length-1];

                boolean success = true;
                String directory;
                File storageDir;

                directory=getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Glofora Toolbox/");
                if (!directory.endsWith("/Glofora Toolbox")) {
                    storageDir = new File(directory + "/Instagram/Repost/");
                } else {
                    storageDir = new File(directory + "/Instagram/Repost/");
                }
                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }

                if(success){
                    File imageFile = new File(storageDir, fileName);
                    if(imageFile.exists()){
                        Toast.makeText(getApplicationContext(), "Some Files already exists..", Toast.LENGTH_SHORT).show();

                    }
                    else {

                        final DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(url)
                                .setPath(imageFile.getAbsolutePath())
                                .build();
                        downloadInfo.setDownloadListener(new DownloadListener() {
                            @Override
                            public void onStart() {
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
                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                notificationManager.cancel(downloadInfo.hashCode());
                            }

                            @Override
                            public void onDownloadSuccess() {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", imageFile);
                                intent.setDataAndType(uri, "video/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            intent= new Intent(getApplicationContext(), HomeActivity.class);

                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                                galleryAddPic(imageFile);
                                Toast.makeText(RepostActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    v.vibrate(500);
                                }
                                notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download successful", "Downloaded " + fileName, false, pendingIntent);

                            }

                            @Override
                            public void onDownloadFailed(DownloadException e) {
                                Toast.makeText(RepostActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                                notificationUtils.showNotification(downloadInfo.hashCode(), downloadInfo.getId(), "Download failed", "Couldn't download " + fileName, false, null);
                            }
                        });
                        downloadManager.download(downloadInfo);
                    }
                }

            }
        }
    }

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
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
            // Showing progress dialog
            pDialog = new ProgressDialog(RepostActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            display_urls.clear();
            video_urls.clear();
            thumbnails.clear();
            img_urls.clear();
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(URL);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    String caption = "",video_url = "null",img_url="null";

                    JSONObject graphql = jsonObj.getJSONObject("graphql");
                    JSONObject shortcode_media = graphql.getJSONObject("shortcode_media");
                    String display_url = shortcode_media.getString("display_url");
                    boolean is_video = shortcode_media.getBoolean("is_video");

                    if (is_video) {
                        video_url = shortcode_media.getString("video_url");

                    }else {
                        img_url=display_url;
                    }

                    JSONObject owner = shortcode_media.getJSONObject("owner");
                    String username = owner.getString("username");
                    String full_name = owner.getString("full_name");
                    String profile_pic_url = owner.getString("profile_pic_url");

                    JSONObject edge_media_to_caption = shortcode_media.getJSONObject("edge_media_to_caption");
                    JSONArray edges_caption = edge_media_to_caption.getJSONArray("edges");
                    if (edges_caption != null && edges_caption.length() > 0) {
                        caption = edges_caption
                                .getJSONObject(0)
                                .getJSONObject("node")
                                .getString("text");
                    }

                    JSONObject edge_web_media_to_related_media = shortcode_media.getJSONObject("edge_web_media_to_related_media");
                    JSONArray edges = edge_web_media_to_related_media.getJSONArray("edges");

                    JSONArray edges1 = null;
                    try {
                        JSONObject edge_sidecar_to_children = shortcode_media.getJSONObject("edge_sidecar_to_children");
                        edges1 = edge_sidecar_to_children.getJSONArray("edges");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Checks for multiple posts
                    String edge_display_url,edge_video_url="null",edge_img_url;
                    boolean edge_is_video;
                    if (edges != null && edges.length() > 0) {

                        for (int i = 0; i < edges.length(); i++) {

                            JSONObject node = edges.getJSONObject(i).getJSONObject("node");

                            edge_display_url = node.getString("display_url");
                            edge_is_video = node.getBoolean("is_video");

                            if (edge_is_video) {
                                edge_video_url = node.getString("video_url");
                                edge_img_url="null";
                            }else {
                                edge_img_url=edge_display_url;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.default_layout).setVisibility(View.GONE);
                                }
                            });

                            video_urls.add(edge_video_url);
                            display_urls.add(edge_display_url);
                            img_urls.add(edge_img_url);
                        }
                        Repost post_model = new Repost(profile_pic_url, video_urls, caption, username, full_name, display_urls, URL, String.valueOf(System.currentTimeMillis()),img_urls);
                        repostList.add(0,post_model);

                    }
                    //Also checks for multiple posts another node
                    else if (edges1 != null && edges1.length() > 0) {

                        for (int i = 0; i < edges1.length(); i++) {

                            JSONObject node = edges1.getJSONObject(i).getJSONObject("node");

                            edge_display_url = node.getString("display_url");
                            edge_is_video = node.getBoolean("is_video");

                            if (edge_is_video) {
                                edge_video_url = node.getString("video_url");
                                edge_img_url="null";
                            }else {
                                edge_img_url=edge_display_url;
                            }

                            display_urls.add(edge_display_url);
                            video_urls.add(edge_video_url);
                            img_urls.add(edge_img_url);

                        }
                        Repost post_model = new Repost(profile_pic_url, video_urls, caption, username, full_name, display_urls, URL, String.valueOf(System.currentTimeMillis()),img_urls);
                        repostList.add(0,post_model);

                    }
                    else {

                        display_urls.add(display_url);
                        video_urls.add(video_url);
                        img_urls.add(img_url);
                        Repost post_model = new Repost(profile_pic_url, video_urls, caption, username, full_name, display_urls, URL, String.valueOf(System.currentTimeMillis()),img_urls);
                        repostList.add(0,post_model);

                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(),
                                "Technical error : " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                        repostList=new ArrayList<>();
                        adapter=new PostsAdapter(repostList,getApplicationContext(),false, null);
                        recyclerView.setAdapter(adapter);
                        fab.setVisibility(View.GONE);
                        findViewById(R.id.default_layout).setVisibility(View.VISIBLE);
                        pDialog.cancel();
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(),
                            "Couldn't get post, maybe the account is private.",
                            Toast.LENGTH_LONG)
                            .show();
                    repostList=new ArrayList<>();
                    adapter=new PostsAdapter(repostList,getApplicationContext(),false, null);
                    recyclerView.setAdapter(adapter);
                    fab.setVisibility(View.GONE);
                    findViewById(R.id.default_layout).setVisibility(View.VISIBLE);
                    pDialog.cancel();
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


            Completable.fromAction(() -> {
                PostsDatabase.getInstance(getApplicationContext()).postsDao().addPost(repostList.get(0));
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            findViewById(R.id.default_layout).setVisibility(View.GONE);

                            if (pDialog.isShowing())
                                pDialog.dismiss();



                            if(instant_download){
                                downLoad();
                            }else{
                                fab.setVisibility(View.INVISIBLE);
                                Animation animation= AnimationUtils.loadAnimation(RepostActivity.this,R.anim.scale_up);
                                fab.startAnimation(animation);
                                fab.show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });



        }
    }
}
