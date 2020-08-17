package com.glofora.whatstustoolbox;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.glofora.whatstustoolbox.fragments.bwa.BWAFragment;
import com.glofora.whatstustoolbox.fragments.wa.WAFragment;
import java.util.List;
import static com.glofora.whatstustoolbox.Utls.Common.APP_DIR;

public class StatusSaverActivity extends AppCompatActivity{
    List<Uri> mSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_saver);
        APP_DIR= Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+this.getString(R.string.app_name)+ "/Whatstus Saved Statuses/";
        if(savedInstanceState == null) {


            Fragment fragment = new WAFragment();
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.framelayout, fragment).commit();


        }
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(3.0f);

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
        getMenuInflater().inflate(R.menu.status_saver_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_help){
            View inflate = LayoutInflater.from(this).inflate(R.layout.tut_status, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(inflate).setPositiveButton("Ok!", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
        }

        if(id==R.id.change_whatsapp){
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(StatusSaverActivity.this);
            builder.setTitle("Choose your WhatsApp");

// add a list
            String[] App = {"WhatsApp", "Business WhatsApp"};
            builder.setItems(App, (dialog, which) -> {
                if(which==0){
                    Fragment fragment = new WAFragment();
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.framelayout, fragment).commit();
                }
                if(which==1){
                    if(checkInstallation("com.whatsapp.w4b")) {
                        Fragment fragment = new BWAFragment();
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

