package com.glofora.whatstustoolbox.viewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.glofora.whatstustoolbox.HelperMethods;
import com.glofora.whatstustoolbox.R;


import java.io.File;
import java.util.ArrayList;

public class ImageViewer extends AppCompatActivity {
    HelperMethods helperMethods;
    FloatingActionMenu floatingMenu;
    int position=0;
    boolean is_saved=false;
    File f;
    class SomeClass implements View.OnClickListener {
        private final ImageViewer imageViewer;
        private final File file;

        class SomeOtherClass implements Runnable {
            private final SomeClass context;
            private final File file;

            SomeOtherClass(SomeClass someClass, File file) {
                context = someClass;
                this.file = file;
            }



            @Override
            public void run() {
                try {
                    HelperMethods.transfer(this.file);
                    Toast.makeText(getApplicationContext(), "Image Saved to Gallery", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("GridView", new StringBuffer().append("onClick: Error: ").append(e.getMessage()).toString());
                }
            }
        }

        SomeClass(ImageViewer imageViewer, File file) {
            this.imageViewer = imageViewer;
            this.file = file;

        }

        @Override
        public void onClick(View view) {
            new SomeOtherClass(this, this.file).run();
        }
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        String string = intent.getExtras().getString("pos");
        position = intent.getExtras().getInt("position");
        is_saved=intent.getExtras().getBoolean("is_saved");
        if(!is_saved) {
            setContentView(R.layout.activity_image_viewer);
        }else
        {
            setContentView(R.layout.activity_wa_saved_image_viewer);

        }
        helperMethods = new HelperMethods(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//        getSupportActionBar().setIcon(R.drawable.business_notif);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();


        f = new File(string);
        PhotoView photoView = (PhotoView) findViewById(R.id.photo);
        floatingMenu = (FloatingActionMenu) findViewById(R.id.menu);


        FloatingActionButton floatingActionButton2 = (FloatingActionButton) findViewById(R.id.wall);
        FloatingActionButton floatingActionButton3 = (FloatingActionButton) findViewById(R.id.rep);
        FloatingActionButton floatingActionButton4 = (FloatingActionButton) findViewById(R.id.dlt);
        if(!is_saved) {
            FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.save);
            floatingActionButton.setOnClickListener(downloadMediaItem(f));

        }
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("deleteFab", true)) {
            floatingActionButton4.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton4.setVisibility(View.GONE);
        }
        Glide.with(this).load(this.f).into(photoView);
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                Intent intent;
                Uri uriForFile;
                if (Build.VERSION.SDK_INT >= 24) {
                    uriForFile = FileProvider.getUriForFile(getApplicationContext(), new StringBuffer().append(getApplicationContext().getPackageName()).append(".provider").toString(),f);
                    intent = new Intent("android.intent.action.ATTACH_DATA");
                    intent.setDataAndType(uriForFile, "image/*");
                    intent.putExtra("mimeType", "image/*");
                    intent.addFlags(1);
                    startActivity(Intent.createChooser(intent, "Set as: "));
                    return;
                }
                uriForFile = Uri.parse(new StringBuffer().append("file://").append(f.getAbsolutePath()).toString());
                intent = new Intent("android.intent.action.ATTACH_DATA");
                intent.setDataAndType(uriForFile, "image/*");
                intent.putExtra("mimeType", "image/*");
                intent.addFlags(Intent.EXTRA_DOCK_STATE_DESK);
                startActivity(Intent.createChooser(intent, "Set as: "));
            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {


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
        floatingActionButton4.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setMessage("Sure to Delete this Image?").setNegativeButton("Nope", new DialogInterface.OnClickListener() {
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
//                       digStash();
//                        Toast.makeText(getApplicationContext(), "Image Deleted", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                builder.create().show();
                digStash();
            }
        });
    }

    public void digStash() {
        if (this.f.exists()) {
            this.f.delete();
            Toast.makeText(getApplicationContext(), "Image Deleted", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent();
        intent.putExtra("pos", this.position);
        setResult(-1, intent);
        finish();
    }

    public View.OnClickListener downloadMediaItem(File file) {
        return new SomeClass(this, file);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

}
