package com.glofora.toolbox.Utls;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.glofora.toolbox.R;
import com.glofora.toolbox.activity.CancelDownloadActivity;


public class NotificationUtils {


    private static String TAG = NotificationUtils.class.getSimpleName();
    private Context mContext;
    int count;

    public NotificationUtils(Context mContext) {
        count=0;
        this.mContext = mContext;
    }

    public void showNotification(int id, String groupId, String title, String message, boolean ongoing, PendingIntent pendingIntent) {

        count++;
        NotificationCompat.Builder mBuilder;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);

        mBuilder = new NotificationCompat.Builder(mContext, "download_channel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(mContext);
        }
        Notification notification;
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);


        Intent notificationIntent = new Intent(mContext, CancelDownloadActivity.class);
        notificationIntent.putExtra("downloadID", groupId);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(mContext,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

       if(pendingIntent!=null){
           if(!TextUtils.isEmpty(groupId)) {
               notification = mBuilder
                       .setAutoCancel(true)
                       .setOngoing(ongoing)
                       .setContentTitle(title)
                       .setGroup(groupId)

                       .setContentText(message)
                       .setTicker(title)
                       .setStyle(bigTextStyle)
                       .setContentIntent(pendingIntent)
                       .setDefaults(Notification.DEFAULT_SOUND)
                       .setSmallIcon(R.drawable.ic_done_all_black_24dp)
                       .build();

           }else{
               notification = mBuilder
                       .setAutoCancel(true)
                       .setOngoing(ongoing)
                       .setContentTitle(title)
                       .setStyle(bigTextStyle)
                       .setContentText(message)
                       .setTicker(title)
                       .setContentIntent(pendingIntent)
                       .setDefaults(Notification.DEFAULT_SOUND)
                       .setSmallIcon(R.drawable.ic_done_all_black_24dp)
                       .build();
           }
       }else{
           if(!TextUtils.isEmpty(groupId)) {
               notification = mBuilder
                       .setAutoCancel(true)
                       .setOngoing(ongoing)
                       .setContentTitle(title)
                       .setStyle(bigTextStyle)
                       .setContentText(message)
                       .setChannelId(groupId)
                       .setTicker(title)
                       .setSmallIcon(R.drawable.ic_file_download)
                       .addAction(R.drawable.ic_add_circle_violet_24dp, "Cancel",
                               pendingNotificationIntent)
                       .build()


               ;
           }else{
               notification = mBuilder
                       .setAutoCancel(true)
                       .setOngoing(ongoing)
                       .setContentTitle(title)
                       .setContentText(message)
                       .setStyle(bigTextStyle)
                       .setTicker(title)
                       .setGroup(groupId)
                       .setChannelId(groupId)
                       .setSmallIcon(R.drawable.ic_file_download)
                       .addAction(R.drawable.ic_add_circle_violet_24dp, "Cancel",
                               pendingNotificationIntent)
                       .build();
           }
       }


        notificationManagerCompat.notify(id, notification);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannels(Context context){

        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel=new NotificationChannel("download_channel","Download Status Notifications", NotificationManager.IMPORTANCE_MIN);
        channel.enableLights(true);
        channel.setShowBadge(true);
        channel.enableVibration(true);

        notificationManager.createNotificationChannel(channel);

    }

}
