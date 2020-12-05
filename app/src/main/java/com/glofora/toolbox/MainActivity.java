package com.glofora.toolbox;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glofora.toolbox.Gallery.Glide4Engine;
import com.glofora.toolbox.Utls.NotificationUtils;
import com.glofora.toolbox.Utls.Utilities;
import com.glofora.toolbox.activity.HDPictureActivity;
import com.glofora.toolbox.activity.RecentsActivity;
import com.glofora.toolbox.activity.RepostActivity;
import com.glofora.toolbox.adapter.MainCardsAdapter;
import com.glofora.toolbox.models.Card;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hotchemi.android.rate.AppRate;

import static com.glofora.toolbox.Utls.Utilities.PICKER_REQUEST_CODE;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {
    // List that will contain the selected files/videos
    List<Uri> mSelected;
    TextView version;
    RecyclerView recyclerView;
    MainCardsAdapter cardsAdapter;
    List<Card> cardList=new ArrayList<>();
    private int num;
    private SharedPreferences sharedPreferences;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action=intent.getAction();
        String type=intent.getType();
        if(Intent.ACTION_SEND.equals(action) && type!=null){
            if(type.equals("text/plain")){
                String intentData=intent.getStringExtra(Intent.EXTRA_TEXT);
                if(intentData.contains("://www.instagram.com/")){

                    startActivity(new Intent(this, RepostActivity.class).putExtra("url",intentData));
                    finish();

                }
                //************ youtube *************** comment bellow code when upload to playstore
                else if(intentData.contains("://youtu.be/")||intentData.contains("youtube.com/watch?v=")){



                        startActivity(new Intent(this, VideoDownloadActivity.class).putExtra("url",intentData));
                        finish();



                }
            }
        }

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
        setContentView(R.layout.activity_main);

        sharedPreferences=getSharedPreferences("easter_egg",MODE_PRIVATE);

        String action=getIntent().getAction();
        String type=getIntent().getType();
        if(Intent.ACTION_SEND.equals(action) && type!=null){
            if(type.equals("text/plain")){
                String intentData=getIntent().getStringExtra(Intent.EXTRA_TEXT);
             if(intentData.contains("://www.instagram.com/")){

                    startActivity(new Intent(this, RepostActivity.class).putExtra("url",intentData));
                    finish();

                }
             //************ youtube *************** comment bellow code when upload to playstore
             else if(intentData.contains("://youtu.be/")||intentData.contains("youtube.com/watch?v=")){



                        startActivity(new Intent(this, VideoDownloadActivity.class).putExtra("url",intentData));
                        finish();



                }
            }
        }

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if(report.areAllPermissionsGranted()){
                            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                getSharedPreferences("directory",MODE_PRIVATE).edit().putString("path",getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()).apply();
                            }else{
                                String directory=getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Glofora Toolbox/");
                                File storageDir=new File(directory);
                                boolean success=true;
                                if(!storageDir.exists()){
                                    success=storageDir.mkdirs();
                                }

                            }*/
                            String directory=getSharedPreferences("directory",MODE_PRIVATE).getString("path",Environment.getExternalStorageDirectory()+"/Glofora Toolbox/");
                            File storageDir=new File(directory);
                            boolean success=true;
                            if(!storageDir.exists()){
                                success=storageDir.mkdirs();
                            }
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();

//        version = findViewById(R.id.version);
        recyclerView=findViewById(R.id.recyclerView);

        cardsAdapter=new MainCardsAdapter(cardList, new MainCardsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Card cardItem) {

                switch (cardItem.getText()){
                    case "Repost":
                        startActivity(new Intent(MainActivity.this,RepostActivity.class));
                        return;
                    case "HD Profile Picture":
                        startActivity(new Intent(MainActivity.this, HDPictureActivity.class));
                        return;
                    case "Video Splitter":
                        startActivity(new Intent(MainActivity.this,VideoSplitterActivity.class));
                        return;
                    case "Status Saver":
                        startActivity(new Intent(MainActivity.this, StatusSaverActivity.class));
                        return;
                    case "Video Downloader":
                        startActivity(new Intent(MainActivity.this, VideoDownloadActivity.class));
                        return;
                    case "Gallery":
                    {
                        String[] PERMISSIONS = {
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        };

                        if (Utilities.hasPermissions(MainActivity.this, PERMISSIONS)) {
                            ShowPicker();

                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PICKER_REQUEST_CODE);
                        }




                    }
                    }



            }
        });



        addCards();
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2);
        if(cardList.size()%2!=0) {
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return position == cardList.size()-1 ? 2 : 1;
                }
            });
        }
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cardsAdapter);
        // callback listener.
        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(5) // default 10
                .setRemindInterval(10) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(which -> Log.d(StatusSaverActivity.class.getName(), Integer.toString(which)))
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);


        try {
            if (!isMyServiceRunning(Class.forName("com.glofora.toolbox.service.NotificationService"))) {
                try {
                    startService(new Intent(this, Class.forName("com.glofora.toolbox.service.NotificationService")));
                } catch (Throwable e) {
                    throw new NoClassDefFoundError(e.getMessage());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        stash();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null) {

            navigationView.getMenu().getItem(0).setChecked(true);
//            Fragment fragment = new WAFragment();
//            FragmentManager fm = getSupportFragmentManager();
//            fm.beginTransaction().replace(R.id.framelayout, fragment).commit();


        }

        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.delete_prgs_msg));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannels(this);
        }


    }
    private boolean isMyServiceRunning(Class<?> cls) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void addCards() {

        cardList.clear();

        Card card=new Card("Repost",R.mipmap.repost,R.drawable.ic_instagram_black_256,R.color.colorOrange);
        cardList.add(card);

        card=new Card("HD Profile Picture",R.mipmap.crop,R.drawable.ic_instagram_black_256,R.color.colorYellow);
        cardList.add(card);

        card=new Card("Status Saver",R.mipmap.save_status,R.drawable.ic_whatsapp_black_256,R.color.colorGreen);
        cardList.add(card);

//************ youtube *************** comment bellow code when upload to playstore
            card=new Card("Video Downloader",R.mipmap.download_video,R.drawable.ic_youtube_black_256,R.color.colorRed);
            cardList.add(card);


        card=new Card("Video Splitter",R.drawable.ic_edit_video_black_21,R.drawable.ic_cinema_black_256,R.color.colorBlue);
        cardList.add(card);
        card=new Card("Gallery",R.drawable.ic_gallery_black_24dp,R.drawable.ic_photo_library_black_256,R.color.colorLightBlack);
        cardList.add(card);
        
        cardsAdapter.notifyDataSetChanged();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void openWebsite(){
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://glofora.com/glofora-toolbox-all-in-one-media-toolbox.html"));
        startActivity(intent);
    }
    public void openGithub(View view) {

        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/lvamsavarthan/Media-Toolbox"));
        startActivity(intent);

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_whatsapp) {
//                Fragment fragment = new WAFragment();
//                FragmentManager fm = getSupportFragmentManager();
//                fm.beginTransaction().replace(R.id.framelayout, fragment).commit();
//        }
//         else if (id == R.id.nav_business) {
//            if(checkInstallation("com.whatsapp.w4b")) {
//                Fragment fragment = new BWAFragment();
//                FragmentManager fm = getSupportFragmentManager();
//                fm.beginTransaction().replace(R.id.framelayout, fragment).commit();
//            }
//            else{
//                Toast.makeText(this, "Business Whatsapp Not Installed", Toast.LENGTH_SHORT).show();
//            }
//        }
//        else if(id==R.id.nav_splitter){
//            Fragment fragment = new WASplitterFragment();
//                FragmentManager fm = getSupportFragmentManager();
//                fm.beginTransaction().replace(R.id.framelayout, fragment).commit();
//        }
//        else
        if (id==R.id.insta){
            startActivity(new Intent(MainActivity.this,RepostActivity.class));
        }
        else if (id==R.id.insta){
            startActivity(new Intent(MainActivity.this,RepostActivity.class));
        }
        else if(id==R.id.insta_recents){
            startActivity(new Intent(MainActivity.this, RecentsActivity.class).putExtra("type","instagram"));

        }
        else if(id==R.id.insta_hd_profile_pic){
            startActivity(new Intent(MainActivity.this, HDPictureActivity.class));

        }
//        else if(id==R.id.id_github){
//           openGithub();
//
//        }
        else if(id==R.id.id_website)
        {
            openWebsite();
        }

        else {
            AppRate.with(this).showRateDialog(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

     if (id == R.id.gallery) {
            String[] PERMISSIONS = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            if (Utilities.hasPermissions(MainActivity.this, PERMISSIONS)) {
                ShowPicker();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PICKER_REQUEST_CODE);
            }
        }


        if (id == R.id.share_app) {
            try {
                Utilities.shareYourApp(MainActivity.this);
            } catch (Exception e) {
                //e.toString();
            }


        }
        return super.onOptionsItemSelected(item);


    }


    /**
     * Method that displays the image/video chooser.
     */
    public void ShowPicker() {
        Matisse.from(MainActivity.this)
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
                Utilities.shareMultiplefiles(mSelected, MainActivity.this);
            }
        }
    }

}
