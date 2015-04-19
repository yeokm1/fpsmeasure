package com.yeokm1.fpsmeasure;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

/**
 * Created by yeokm1 on 17/4/2015.
 */
public class FgService extends Service {

    private static final int NOTIFICATION_ID = 1234;

    @Override
    public void onCreate(){
        super.onCreate();
        runAsForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runAsForeground(){
        Intent notificationIntent = new Intent(this, MainActivity.class);

        Notification notification=new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FPS Measure")
                .setContentText("is running")
                .build();

        startForeground(NOTIFICATION_ID, notification);

    }
}
