package com.glofora.whatstustoolbox;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glofora.whatstustoolbox.activity.RecentsActivity;
import com.glofora.whatstustoolbox.adapter.SingleVideoAdapter;
import com.glofora.whatstustoolbox.models.Video;
import com.glofora.whatstustoolbox.room.VideoDatabase;

import java.util.ArrayList;
import java.util.List;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class VideoDownloadActivity extends AppCompatActivity {

    private static final String TAG = VideoDownloadActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ProgressDialog pDialog;
    private List<Video> videos;
    private SingleVideoAdapter mAdapter;
    private VideoDatabase videoDatabase;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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
            startActivity(new Intent(this, RecentsActivity.class).putExtra("type","youtube"));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_youtube);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        videos = new ArrayList<>();
        mAdapter = new SingleVideoAdapter(videos,this);
        mRecyclerView = findViewById(R.id.recyclerView);
        videoDatabase = VideoDatabase.getInstance(this);
        findViewById(R.id.default_layout).setVisibility(View.VISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        String intentData=getIntent().getStringExtra("url");
        if(intentData!=null){
            if(intentData.contains("://youtu.be/")|| intentData.contains("youtube.com/watch?v=")){
                getYoutubeDownloadUrl(intentData);
            }else {
                Toast.makeText(VideoDownloadActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private void getYoutubeDownloadUrl(final String youtubeLink) {

        new YouTubeExtractor(this){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(VideoDownloadActivity.this);
                pDialog.setMessage("Getting video information..");
                pDialog.setCancelable(false);
                pDialog.show();

            }
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {

                if (pDialog.isShowing())
                    pDialog.dismiss();

                if(ytFiles==null){
                    videos.clear();
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(VideoDownloadActivity.this, "Sorry we couldn't fetch the video", Toast.LENGTH_SHORT).show();
                    return;
                }

                Video video = new Video(youtubeLink, videoMeta.getMaxResImageUrl(), videoMeta.getHqImageUrl(),videoMeta.getTitle(),videoMeta.getAuthor());

                Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        videoDatabase.videoDao().addVideo(video);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                videos.add(0,video);
                                findViewById(R.id.default_layout).setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });


            }
        }.extract(youtubeLink, true, false);

    }

    public void openYoutube(View view) {
        String url="https://www.youtube.com/";
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.google.android.youtube");
        try {
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(VideoDownloadActivity.this,"Youtube not found",Toast.LENGTH_SHORT).show();
        }
    }

}
