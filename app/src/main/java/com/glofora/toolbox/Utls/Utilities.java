package com.glofora.toolbox.Utls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.glofora.toolbox.BuildConfig;
import com.glofora.toolbox.R;
import com.glofora.toolbox.TrimmerActivity;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import needle.Needle;
import needle.UiRelatedProgressTask;

import static android.os.Build.VERSION.SDK_INT;

public class Utilities {

    public static final String EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH";
    public static final int REQUEST_VIDEO_TRIMMER = 0x01;
    public static final int CAMERA_REQUEST_VIDEO_TRIMMER = 100;
    public static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    public static final int PICKER_REQUEST_CODE = 102;

    private static List<File> fileList = new ArrayList<>();
    private static String filePath;
    private static ProgressDialog mProgressDialog;
    private static AlertDialog aler_dialog;
    private static AlertDialog.Builder builder;




    /**
     * Method to call oncreate methode of activity to use split video method
     */
    public static void getReadySplitvideos(Context context){
        builder = new AlertDialog.Builder(context);
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog);
        aler_dialog = builder.create();
    }



    /**
     * Method to delete files from media store
     */
    public static void DeletefilesFromMediaStore(Context context, String path) {

    context.getContentResolver().delete(getUriFromPath(context,path), null, null);
    }

    public static   void deleteTempFilesfromdirectory(Context context){
        File dir = new File(context.getString(R.string.file_temp_path));
//                            Utilities.removeFilesFromGSplitvideos(HomeActivity.this);
        if (dir.isDirectory()) {

            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {

                try {
                    //Delete Video files from internal memory
                    Utilities.DeletefilesFromMediaStore(context, context.getString(R.string.file_temp_path) + children[i]);

                } catch (RuntimeException e) {
                } finally {

                }
                new File(dir, children[i]).delete();
            }

        }
    }


    /**
     * Method to get URI from given path
     * Only for video files.
     */
    public static Uri getUriFromPath(Context context,String filePath) {

        long videoId;
        Uri videoUri = MediaStore.Video.Media.getContentUri("external");

        String[] projection = {MediaStore.Video.Media._ID};
        // TODO This will break if we have no matching item in the MediaStore.
        Cursor cursor = context.getContentResolver().query(videoUri, projection, MediaStore.Video.Media.DATA + " LIKE ?", new String[]{filePath}, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        videoId = cursor.getLong(columnIndex);

        cursor.close();
        return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId);
    }
    /**
     * Method to share multiple files.
     */
    public static void shareMultiplefiles(List<Uri> uriArrayList, Context context) {


//This is important since we are sending multiple files
    final Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//Used temporarily to get Uri references


    if (SDK_INT < Build.VERSION_CODES.KITKAT) {

    } else {
        //Grant read Uri permissions to the intent
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    }

    ArrayList<Uri> fls = new ArrayList<Uri>(uriArrayList);

//I know that the files which will be sent will be one of the following
    sharingIntent.setType("*/*");
//pass the Array that holds the paths to the files


    sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fls);
    sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    context.startActivity(Intent.createChooser(sharingIntent, null));

}
    /**
     * Method to share Multiple files to whatsapp or others(tiktok, Instagram etc.).
     * For whatsapp , set app as 100
     * For whatsapp business ,set app as 99
     * For other social medial pass 101
     *
     */
    public static void shareMultiplefilesToWhatsappOrOthers(List<File> files, Context context,Integer app) {


//All Uri's are retrieved into this ArrayList
        ArrayList<Uri> uriArrayList = null;
//This is important since we are sending multiple files
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//Used temporarily to get Uri references
        Uri shareFileUri;

        if (SDK_INT < Build.VERSION_CODES.KITKAT) {


            //Create new instance of the ArrayList where the Uri will be stored
            uriArrayList = new ArrayList<>();

            for (File file : files) {
                uriArrayList.add(Uri.fromFile(file));
            }


        } else {


            uriArrayList = new ArrayList<>();


            for (File file : files) {
                shareFileUri = FileProvider.getUriForFile(context, context.getString(R.string.app_provider), file);

                uriArrayList.add(shareFileUri);
            }

            //Grant read Uri permissions to the intent
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        }

//I know that the files which will be sent will be one of the following
        sharingIntent.setType("*/*");
//pass the Array that holds the paths to the files
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArrayList);
        if(app==100) {
            if(Utilities.checkInstallation("com.whatsapp",context))
            {
                sharingIntent.setPackage("com.whatsapp");

            }else{
                sharingIntent.setPackage("com.whatsapp.w4b");

            }

            try {
                context.startActivity(sharingIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "WhatsApp Not Found on this Phone :(", Toast.LENGTH_SHORT).show();

            }
        }
        else if(app==99){

            if(Utilities.checkInstallation("com.whatsapp.w4b",context))
            {
                sharingIntent.setPackage("com.whatsapp.w4b");

            }else{
                sharingIntent.setPackage("com.whatsapp");

            }
            try {
                context.startActivity(sharingIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "WhatsApp Not Found on this Phone :(", Toast.LENGTH_SHORT).show();

            }
        }
        else {
            context.startActivity(Intent.createChooser(sharingIntent, null));
        }


    }
    public static boolean checkInstallation(String uri,Context context) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    /**
     * Method to start trim activity.
     */
    public static void startTrimActivity(@NonNull Uri uri,Context context,Integer app, Integer interval) {


        Intent intent = new Intent(context, TrimmerActivity.class);
        intent.putExtra(EXTRA_VIDEO_PATH,FileUtls.getPath(context, uri));
        //Create the bundle
        Bundle bundle = new Bundle();

//Add your data to bundle
        bundle.putInt("app", app);
        bundle.putInt("interval", interval);

//Add the bundle to the intent
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    /**
     * Method to share your App
     */
    public static void shareYourApp(Context context){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString( R.string.share_app_sub));
        String shareMessage = context.getString( R.string.share_app_msg );
        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        context.startActivity(Intent.createChooser(shareIntent, "choose one"));
    }
    /**
     * Method to share file to whatsapp or others(tiktok, Instagram etc.).
     * For whatsapp shre just pass app as 100
     * For other social medial pass 101
     */
    public static void shareSingleFileToWhatsappOrOthers(Uri selectedUri,Context context,Integer app) {


//All Uri's are retrieved into this ArrayList
        ArrayList<Uri> uriArrayList = null;
//This is important since we are sending multiple files
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        if (SDK_INT < Build.VERSION_CODES.KITKAT) {


            //Create new instance of the ArrayList where the Uri will be stored
            uriArrayList = new ArrayList<>();

//            for (File file : files) {
            uriArrayList.add(selectedUri);
//            }


        } else {


            uriArrayList = new ArrayList<>();
            uriArrayList.add(selectedUri);
            //Grant read Uri permissions to the intent
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        }

//I know that the files which will be sent will be one of the following
        sharingIntent.setType("*/*");
//pass the Array that holds the paths to the files
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArrayList);
        if(app==100) {
            if(Utilities.checkInstallation("com.whatsapp",context))
            {
                sharingIntent.setPackage("com.whatsapp");

            }else{
                sharingIntent.setPackage("com.whatsapp.w4b");

            }

            try {
                context.startActivity(sharingIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "WhatsApp Not Found on this Phone :(", Toast.LENGTH_SHORT).show();

            }
        }else {
            context.startActivity(Intent.createChooser(sharingIntent, null));
        }

    }
    /**
     * Method to get video duration in seconds
     */
    public  static Double getVideoDurationInSeconds(Context context, File videoFile){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(context, Uri.fromFile(videoFile));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Double video_duration=(Double.parseDouble(time)/1000);

        retriever.release();
        return video_duration;

    }

    /**
     * Method to get buildMediaSource (For Video)
     */
    public static MediaSource buildMediaSource(Uri uri, Context context) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context,  context.getString(R.string.app_name)));
// This is the MediaSource representing the media to be played.
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
        return videoSource;
    }
    /**
     * Method to add file to media library
     */
    public static void ScanMedia(File file,Context context)
    {


        //Broadcast the Media Scanner Intent to trigger it
        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file.getAbsoluteFile());
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }

    }

    /**
     * Command for Splitvideos
     */
    public static void Splitvideos(Uri uri,Context context,Integer interval,Integer app){
        fileList = new ArrayList<>();



        Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String, Integer>() {
            @Override
            protected  String doWork() {

                int result = 0;
                File videoFile = new File(FileUtls.getPath(context,uri));//File read from Source folder to Split.

                /**
                 * Get duration for video by calling getVideoDurationInSeconds method
                 */
                Double timeInMillisec=Utilities.getVideoDurationInSeconds(context,videoFile);


                boolean flag = true;
                Double flg_trm=0.0;
                Double flg_trm_2=0.0;
                Double start = 0.0;
                Double end;
                long Intalval=interval-1;




                if (timeInMillisec <= interval) {

                    result += 1;
                    publishProgress(result);
                    Utilities.executeCutVideoSpliiter(start, timeInMillisec, uri,context);

                    return "";

                }





                while (flag) {

                    Double Tim_start;
                    Double Trim_end;

                    if((start-flg_trm)<0){
                        Tim_start = 0.0;
                    }else{
                        Tim_start = start-flg_trm;
                    }
                    if((start+Intalval+flg_trm)>timeInMillisec){
                        Trim_end = timeInMillisec-flg_trm_2;
                    }else {
                        Trim_end = (start+ Intalval) + flg_trm;
                    }

                    result += 1;
                    publishProgress(result);
                    Utilities.executeCutVideoSpliiter(Tim_start, Trim_end, uri,context);

                    start = start + Intalval;
                    flag = start > timeInMillisec ? false : true;


                }



                return "The result is: " + result;

            }

            @Override
            protected void thenDoUiRelatedWork(String result) {
                if(fileList.size()>0) {

                    for (int i = 0; i < fileList.size(); i++) {
                        Utilities.ScanMedia(fileList.get(i),context);
                    }
                    aler_dialog.dismiss();
                     Utilities.deleteTempFilesfromdirectory(context);

                    Toast.makeText(context, "Splitted files saved to 'Whaststus Splitted Videos' Folder !", Toast.LENGTH_SHORT).show();

                    Utilities.shareMultiplefilesToWhatsappOrOthers(fileList, context,app);

                }else{
                    aler_dialog.dismiss();
                    Toast.makeText(context, "Please select a valid mp4 file!", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            protected void onProgressUpdate(Integer progress) {
                aler_dialog.show();
            }
        });
    }


    /**
     * Command for cutting video
     */
    synchronized  private static   boolean executeCutVideoSpliiter(Double startMs, Double endMs, final Uri uri,Context context) {


        File moviesDir = Environment.getExternalStorageDirectory();


        int unique_id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        String filePrefix = "G_" + unique_id + "" + startMs+""+endMs;
        String fileExtn = ".mp4";
        File folder = new File(moviesDir + "/"+ context.getString(R.string.app_name)+"/Splitted Videos/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File dest = new File(moviesDir, "/"+context.getString(R.string.app_name)+"/Splitted Videos/" + filePrefix + fileExtn);

        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, "/"+context.getString(R.string.app_name)+"/Splitted Videos/" + filePrefix + fileNo + fileExtn);
        }


        filePath = dest.getAbsolutePath();
        try {
            TrimVideoUtils.startTrim(new File(uri.getPath()),dest,startMs,endMs);
            fileList.add(dest);
            return  true;
        } catch (IOException e) {
            e.printStackTrace();
            return  false;
        }finally {

            return  false;
        }


    }
//    public static void requestPermission(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
//    }

    /**
     * Helper method that verifies whether the permissions of a given array are granted or not.
     *
     * @param context
     * @param permissions
     * @return {Boolean}
     */
    public static boolean hasPermissions(Context context, String... permissions) {
//        if (SDK_INT >= Build.VERSION_CODES.R) {
//            return Environment.isExternalStorageManager();
//        }else {


            if (context != null && permissions != null) {
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
            }
//        }
        return true;
    }

    /**
     * Requests given permission.
     * If the permission has been denied previously, a Dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    public static void requestPermission(final String permission, String rationale, final int requestCode,Context context) {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context, permission)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.permission_title_rationale));
            builder.setMessage(rationale);
            builder.setPositiveButton(context.getString(R.string.label_ok), (dialog, which) -> ActivityCompat.requestPermissions((Activity)context, new String[]{permission}, requestCode));
            builder.setNegativeButton(context.getString(R.string.label_cancel), null);
            builder.show();
        } else {
            ActivityCompat.requestPermissions((Activity)context, new String[]{permission}, requestCode);
        }
    }


}
