package com.glofora.toolbox.service.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.glofora.toolbox.service.NotificationService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("com.glofora.notif.onDestroyed")){
            try {
                context.startService(new Intent(context, NotificationService.class));
            } catch (Throwable e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
    }
}
