package com.yeokm1.fpsmeasure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    private CommandHandler commandHandler;

    private Intent fgServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        commandHandler = new CommandHandler();


        fgServiceIntent = new Intent(this, FgService.class);

    }


    public void startFPS(View v){
        startService(fgServiceIntent);
        commandHandler.startFPSMeasure();
    }

    public void stopFPS(View v){
        commandHandler.stopFPSMeasure();
        stopService(fgServiceIntent);
    }



}
