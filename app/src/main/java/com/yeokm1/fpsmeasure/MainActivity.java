package com.yeokm1.fpsmeasure;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    private CommandHandler commandHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        commandHandler = new CommandHandler(this);

    }


    public void startFPS(View v){
        commandHandler.startFPSMeasure();
    }

    public void stopFPS(View v){
        commandHandler.stopFPSMeasure();
    }



}
