package com.glofora.whatstus.viewer;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import com.glofora.whatstus.HelperMethods;
import com.glofora.whatstus.R;
import com.glofora.whatstus.Utls.Utilities;
import com.glofora.whatstus.adapter.FilesAdapter;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayer extends AppCompatActivity implements EasyVideoCallback{
    HelperMethods helperMethods;
    FloatingActionMenu menu;
    private EasyVideoPlayer player;
    private FilesAdapter flpadp;
    int position=0;
    boolean is_saved=false;
    File f;
    class SomeClass implements View.OnClickListener {
        private final VideoPlayer videoPlayer;
        private final File file;

        class SomeOtherClass implements Runnable {
            private final VideoPlayer.SomeClass context;
            private final File file;

            SomeOtherClass(VideoPlayer.SomeClass someClass, File file) {
                context = someClass;
                this.file = file;
            }



            @Override
            public void run() {
                try {
                    HelperMethods.transfer(this.file);
                    Toast.makeText(getApplicationContext(), "Video Saved to Gallery :)", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("GridView", new StringBuffer().append("onClick: Error: ").append(e.getMessage()).toString());
                }

            }
        }

        SomeClass(VideoPlayer videoPlayer, File file) {
            this.videoPlayer = videoPlayer;
            this.file = file;
        }

        @Override
        public void onClick(View view) {
            new VideoPlayer.SomeClass.SomeOtherClass(this, this.file).run();
        }
    }

    @Override
    public void onPaused(EasyVideoPlayer easyVideoPlayer) {
    }

    @Override
    public void onStarted(EasyVideoPlayer easyVideoPlayer) {
    }

    @Override
    public void onSubmit(EasyVideoPlayer easyVideoPlayer, Uri uri) {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this.f = new File(intent.getExtras().getString("pos"));
        this.position = intent.getExtras().getInt("position");
        is_saved=intent.getExtras().getBoolean("is_saved");
        if(!is_saved) {

            setContentView(R.layout.activity_video_player);
        }else {
            setContentView(R.layout.activity_wa_saved_video_player);

        }
            helperMethods = new HelperMethods(this);
            this.helperMethods = new HelperMethods(this);
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//        getSupportActionBar().setIcon(R.drawable.business_notif);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().hide();

        this.menu = (FloatingActionMenu) findViewById(R.id.menu);
        if(!is_saved) {
            FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.save);
            floatingActionButton.setOnClickListener(downloadMediaItem(this.f));
        }
        FloatingActionButton floatingActionButton2 = (FloatingActionButton) findViewById(R.id.rep);
        FloatingActionButton floatingActionButton3 = (FloatingActionButton) findViewById(R.id.dlt);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("deleteFab", true)) {
            floatingActionButton3.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton3.setVisibility(View.GONE);
        }

        floatingActionButton2.setOnClickListener(new View.OnClickListener() {


            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {

                Uri uriForFile;
//All Uri's are retrieved into this ArrayList
                ArrayList<Uri> uriArrayList = null;
//This is important since we are sending multiple files
                final Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {


                    //Create new instance of the ArrayList where the Uri will be stored
                    uriArrayList = new ArrayList<>();

                    uriArrayList.add(uriForFile=Uri.fromFile(f));

                } else {
                    uriArrayList = new ArrayList<>();
                    uriForFile = FileProvider.getUriForFile(getApplicationContext(), new StringBuffer().append(getPackageName()).append(".provider").toString(),f);

                    uriArrayList.add(uriForFile);
                    //Grant read Uri permissions to the intent
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                }

//I know that the files which will be sent will be one of the following
                sharingIntent.setType("*/*");
//pass the Array that holds the paths to the files
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArrayList);

                startActivity(Intent.createChooser(sharingIntent, null));

            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this.this$0);
//                builder.setMessage("Sure to Delete this Video?").setNegativeButton("Nope", new DialogInterface.OnClickListener(this) {
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        sendBackData();
//                        Toast.makeText(getApplicationContext(), "Video Deleted", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                builder.create().show();
                sendBackData();
                Toast.makeText(getApplicationContext(), "Video Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        this.player = (EasyVideoPlayer) findViewById(R.id.player);
        this.player.setCallback(this);
        this.player.setSource(Uri.fromFile(this.f));
    }

    public void sendBackData() {
        if (this.f.exists()) {
            this.f.delete();
        }

        Intent intent = new Intent();
        intent.putExtra("pos", this.position);
        setResult(-1, intent);
        finish();
    }


    public View.OnClickListener downloadMediaItem(File file) {
        return new VideoPlayer.SomeClass(this, file);
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.player.pause();
    }

    @Override
    public void onPreparing(EasyVideoPlayer easyVideoPlayer) {
        Log.d("EVP-Sample", "onPreparing()");
    }

    @Override
    public void onPrepared(EasyVideoPlayer easyVideoPlayer) {
        Log.d("EVP-Sample", "onPrepared()");
    }

    @Override
    public void onBuffering(int i) {
        Log.d("EVP-Sample", new StringBuffer().append(new StringBuffer().append("onBuffering(): ").append(i).toString()).append("%").toString());
    }

    @Override
    public void onError(EasyVideoPlayer easyVideoPlayer, Exception exception) {
        Log.d("EVP-Sample", new StringBuffer().append("onError(): ").append(exception.getMessage()).toString());
    }

    @Override
    public void onCompletion(EasyVideoPlayer easyVideoPlayer) {
        Log.d("EVP-Sample", "onCompletion()");
    }

    @Override
    public void onRetry(EasyVideoPlayer easyVideoPlayer, Uri uri) {
        Toast.makeText(this, "Retry", Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onClickVideoFrame(EasyVideoPlayer easyVideoPlayer) {
//        if (this.menu.isMenuButtonHidden()) {
//            this.menu.showMenuButton(true);
//            easyVideoPlayer.hideControls();
//            return;
//        }
//        this.menu.hideMenuButton(true);
//        easyVideoPlayer.showControls();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }


}
