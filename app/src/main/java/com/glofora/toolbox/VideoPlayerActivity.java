package com.glofora.toolbox;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.glofora.toolbox.Utls.FileUtls;
import com.glofora.toolbox.Utls.Utilities;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import needle.Needle;
import needle.UiRelatedProgressTask;

public class VideoPlayerActivity extends AppCompatActivity {

    private SimpleExoPlayer player;
    private PlayerView playerView;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private ProgressDialog mProgressDialog;
    MediaSource mediaSource;
    private File videoFile;
    Uri videoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_share_reciever);
        playerView = findViewById(R.id.video_view);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("video/")) {
                handleVideoIntent(intent); // Handle text being sent
            }
        }
        findViewById(R.id.btCancel)
                .setOnClickListener(
                        view -> onCancelClicked()
                );
        findViewById(R.id.btSave)
                .setOnClickListener(
                        view -> splitSaveAndShareVideosToWhatsApp(videoURI)
                );

        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading.....");


    }

    private void onCancelClicked() {
      finish();
    }
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
//        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
////                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer( player);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);
    }
    void handleVideoIntent(Intent intent) {
        videoURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (videoURI != null) {

           mediaSource= Utilities.buildMediaSource(videoURI,VideoPlayerActivity.this);

            // Update UI to reflect image being shared

        }
    }

    public void splitSaveAndShareVideosToWhatsApp(Uri selectedUri)
    {
        if (selectedUri != null) {

            try {


                Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String, Integer>() {
                    @Override
                    protected String doWork() {
                        int result = 0;

                        result += 1;
                        publishProgress(result);
                        videoFile = new File(FileUtls.getPath(getApplicationContext(), selectedUri));

                        return "The result is: " + result;
                    }

                    @Override
                    protected void thenDoUiRelatedWork(String result) {
                        long length = videoFile.length();
                        length = length / (1024*1024);
                        mProgressDialog.cancel();
//                        if(length<=150) {



                            if(Utilities.getVideoDurationInSeconds( VideoPlayerActivity.this, videoFile)<=15)
                            {
                                /**
                                 *
                                 * whatsapp share pass 100 as app
                                 * for other social medias pass 101 as app
                                 */
                                Utilities.shareSingleFileToWhatsappOrOthers(selectedUri,VideoPlayerActivity.this,100);
                            }else {
                                /**
                                 *
                                 * For whatsapp  pass 100 as app
                                 * for other social medias pass 101 as app
                                 */
                                Utilities.startTrimActivity(selectedUri,VideoPlayerActivity.this,100,30);
                            }


//                        }else{
//                            Toast.makeText(VideoPlayerActivity.this, "files should be less than 150MB", Toast.LENGTH_SHORT).show();
//
//                        }
                    }

                    @Override
                    protected void onProgressUpdate(Integer progress) {
                        mProgressDialog.show();
                        mProgressDialog.setMessage("Loading.........");
                    }
                });



            } catch (Exception ex) {
                Toast.makeText(VideoPlayerActivity.this, "Large File not supported", Toast.LENGTH_SHORT).show();

            }

        } else {
            Toast.makeText(VideoPlayerActivity.this, R.string.toast_cannot_retrieve_selected_video, Toast.LENGTH_SHORT).show();
        }

    }







}
