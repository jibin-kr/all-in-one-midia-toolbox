package com.glofora.toolbox;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.glofora.toolbox.Utls.Utilities;
import com.glofora.toolbox.fragments.bwa.BWASplitterFragment;
import com.glofora.toolbox.fragments.wa.WASplitterFragment;

import java.io.File;
import java.util.List;

import needle.Needle;
import needle.UiRelatedProgressTask;

public class VideoSplitterActivity extends AppCompatActivity{
    List<Uri> mSelected;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_splitter);
        if(savedInstanceState == null) {


            Fragment fragment = new WASplitterFragment();
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.framelayout, fragment).commit();


        }
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(3.0f);
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.delete_prgs_msg));
    }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.video_splitter_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_help){
            View inflate = LayoutInflater.from(this).inflate(R.layout.tut_video_splitter, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(inflate).setPositiveButton("Ok!", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
        }

        if(id==R.id.change_whatsapp){
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(VideoSplitterActivity.this);
            builder.setTitle("Choose your WhatsApp");

// add a list
            String[] App = {"WhatsApp", "Business WhatsApp"};
            builder.setItems(App, (dialog, which) -> {
                if(which==0){
                    Fragment fragment = new WASplitterFragment();
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.framelayout, fragment).commit();
                }
                if(which==1){
                    if(checkInstallation("com.whatsapp.w4b")) {
                        Fragment fragment = new BWASplitterFragment();
                        FragmentManager fm = getSupportFragmentManager();
                        fm.beginTransaction().replace(R.id.framelayout, fragment).commit();
                    }
                    else{
                        Toast.makeText(this, "Business Whatsapp Not Installed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

// create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if (id == R.id.delete_split_files) {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_msg);

            builder.setPositiveButton("Yes", (dialog, which) -> {

                Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String, Integer>() {
                    @Override
                    protected String doWork() {
                        int result = 0;
                        //        Remove files from splitdirectory
                        File dir = new File(getString(R.string.file_split_path)+"/Splitted Videos/");
                        if (dir.isDirectory()) {

                            String[] children = dir.list();
                            for (int i = 0; i < children.length; i++) {
                                result += 1;
                                publishProgress(result);
                                try {
                                    //Delete Video files from internal memory
                                    Utilities.DeletefilesFromMediaStore(getApplicationContext(), getString(R.string.file_split_path)+"Splitted Videos/" + children[i]);

                                } catch (RuntimeException e) {
                                } finally {
                                    result += 1;
                                    publishProgress(result);
                                }
                                new File(dir, children[i]).delete();
                            }

                        } else {
                            result += 1;
                            publishProgress(result);
                        }

                        return "The result is: " + result;
                    }

                    @Override
                    protected void thenDoUiRelatedWork(String result) {

                        Toast.makeText(VideoSplitterActivity.this, "Files removed successfully", Toast.LENGTH_SHORT).show();
                        mProgressDialog.cancel();
                    }

                    @Override
                    protected void onProgressUpdate(Integer progress) {
                        mProgressDialog.setMessage("Removing.........");
                        mProgressDialog.show();

                    }
                });


            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

            builder.show();


        }


        return super.onOptionsItemSelected(item);


    }


    private boolean checkInstallation(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


}

