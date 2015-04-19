package com.yeokm1.fpsmeasure;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by yeokm1 on 17/4/2015.
 */
public class FgService extends Service {


    private static final String TAG = "FgService";
    private static final int NOTIFICATION_ID = 1234;

    private WindowManager windowManager;
    private TextView fpsView;

    public static final String INTENT_FPS_DATA = "fps";

    @Override
    public void onCreate(){
        super.onCreate();


        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        fpsView = new TextView(this);
        fpsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        fpsView.setTextColor(Color.YELLOW);

        setFPSViewText("NA");


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.END;
        params.x = 0;
        params.y = 100;

        windowManager.addView(fpsView, params);

        runAsForeground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fpsView != null) {
            windowManager.removeView(fpsView);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle bundle = intent.getExtras();

        if(bundle == null) {
            setFPSViewText("NA");
        } else {
            int fps = bundle.getInt(INTENT_FPS_DATA);

            Log.i(TAG, "FPS: " + fps);

            if(fps != CommandHandler.NO_FPS_CALCULATED) {
                setFPSViewText(Integer.toString(fps));
            }
        }

        return START_STICKY;
    }

    private void setFPSViewText(String text){
        if(fpsView != null){
            fpsView.setText(text);
        }
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
