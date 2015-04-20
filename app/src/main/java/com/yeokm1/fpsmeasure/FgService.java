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
    private TextView freqView;

    public static final String INTENT_FPS_DATA = "fps";
    public static final String INTENT_CPU_DATA = "cpu";
    public static final String INTENT_GPU_DATA = "gpu";


    @Override
    public void onCreate(){
        super.onCreate();


        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        fpsView = new TextView(this);
        fpsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        fpsView.setTextColor(Color.YELLOW);

        setFPSViewText("NA");

        freqView = new TextView(this);
        freqView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        freqView.setTextColor(Color.GREEN);

        setFreqView(0, 0);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;


        windowManager.addView(fpsView, params);


        params.y = 100;
        windowManager.addView(freqView, params);

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
            long cpu = bundle.getLong(INTENT_CPU_DATA);
            long gpu = bundle.getLong(INTENT_GPU_DATA);

            Log.i(TAG, "FPS: " + fps + ", CPU: " + cpu + ", GPU: " + gpu);

            if(fps != CommandHandler.NO_FPS_CALCULATED) {
                setFPSViewText(Integer.toString(fps));
                setFreqView(cpu, gpu);
            }


        }

        return START_STICKY;
    }

    private void setFPSViewText(String text){
        if(fpsView != null){
            fpsView.setText(text);
        }
    }

    private void setFreqView(long cpu, long gpu){
        if(freqView != null){

            cpu /= 1000;
            gpu /= 1000000;

            freqView.setText("CPU: " + cpu  + "MHz\nGPU: " + gpu + "MHz");
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
