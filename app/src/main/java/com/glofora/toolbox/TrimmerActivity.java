package com.glofora.toolbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.glofora.toolbox.Utls.Utilities;
import com.video_trim.K4LVideoTrimmer;
import com.video_trim.interfaces.OnK4LVideoListener;
import com.video_trim.interfaces.OnTrimVideoListener;


public class TrimmerActivity extends AppCompatActivity implements OnTrimVideoListener, OnK4LVideoListener {

    private K4LVideoTrimmer mVideoTrimmer;
    private ProgressDialog mProgressDialog;
    private String filePath;
    private String TAG = "";
    private int choice = 0;
    private static int app;
    private int interval;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmer);
        Utilities.getReadySplitvideos(TrimmerActivity.this);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent extraIntent = getIntent();
        String path = "";

        if (extraIntent != null) {
            path = extraIntent.getStringExtra(Utilities.EXTRA_VIDEO_PATH);
        }

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

//Extract the data…
        app = bundle.getInt("app");
        interval=bundle.getInt("interval");

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));



        mVideoTrimmer = findViewById(R.id.timeLine);
        if (mVideoTrimmer != null) {
            mVideoTrimmer.setMaxDuration(60 * 7);

            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setOnK4LVideoListener(this);

            mVideoTrimmer.setVideoURI(Uri.parse(path));
            mVideoTrimmer.setVideoInformationVisibility(true);
            mVideoTrimmer.setDestinationPath(getString(R.string.file_temp_path));
        }

    }



    @Override
    public void onTrimStarted() {
        mProgressDialog.show();

    }

    @Override
    public void getResult(final Uri uri) {
        mProgressDialog.cancel();

        /**
         *Add trimmed video into media library
         *
         */
//        Utilities.ScanMedia(new File(uri.getPath()),TrimmerActivity.this);
        /**
         *Split trimmed videos into multiple parts then save it with share
         *
         */
        Utilities.Splitvideos(uri,TrimmerActivity.this,interval,app);


    }






    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
        finish();
    }

    @Override
    public void onError(String message) {
        mProgressDialog.cancel();

        runOnUiThread(() -> Toast.makeText(TrimmerActivity.this, message, Toast.LENGTH_SHORT).show());
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onVideoPrepared() {
        runOnUiThread(() -> {
        });
    }
}
