package com.yeokm1.fpsmeasure;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yeokm1 on 17/4/2015.
 */
public class CommandHandler {


    private static final int UPDATE_RATE = 1000;
    public static final float TIME_INTERVAL_NANO_SECONDS = 1000000000;
    public static final int BUFF_LEN = 1000;


    public static final int MAX_FPS = 60;
    public static final int NO_FPS_CALCULATED = -1;
    protected static final String FPS_COMMAND = "dumpsys SurfaceFlinger --latency SurfaceView\n";

    private static final String TAG = "CommandHandler";

    private Process process;
    private DataOutputStream stdin;
    private InputStream stdout;

    private ScheduledExecutorService scheduler;

    private boolean processOfGettingFPS = false;


    public void startFPSMeasure(){
        try {


            Process process = Runtime.getRuntime().exec("su");
            stdin = new DataOutputStream(process.getOutputStream());
            stdout = process.getInputStream();


            scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {

                            if(processOfGettingFPS){
                                processOfGettingFPS = true;
                            }
                            int fps = getFPS();

                            processOfGettingFPS = false;

                            Log.i(TAG, "FPS: " + fps);

                        }
                    }, 0, UPDATE_RATE, TimeUnit.MILLISECONDS);

        } catch (IOException e) {

        }

    }


    public void stopFPSMeasure(){
        if(process != null){
            try {
                stdin.write("exit\n".getBytes());
                stdin.flush();
                stdin.close();
                process.waitFor();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
            process.destroy();
            process = null;
        }

    }


    private int getFPS(){

        try {
            String[] output = getFPSCommandOutput();

            if (output.length == 0 || output.length == 1) {
                return NO_FPS_CALCULATED;
            }

            //First line is not used


            String lastLine = output[output.length - 1];
            String[] split = splitLine(lastLine);

            if(split.length != 3){
                return NO_FPS_CALCULATED;
            }

            String lastFrameFinishTimeStr = split[2];

            double lastFrameFinishTime = Double.parseDouble(lastFrameFinishTimeStr);
            int frameCount = 0;

            for (int i = 1; i < output.length; i++) {
                String[] splitted = splitLine(output[i]);
                String thisFrameFinishTimeStr = splitted[2];
                double thisFrameFirstTime = Double.parseDouble(thisFrameFinishTimeStr);
                if ((lastFrameFinishTime - thisFrameFirstTime) <= TIME_INTERVAL_NANO_SECONDS) {
                    frameCount++;
                }

            }

            if (frameCount > 100 || frameCount <= 3) {
                return NO_FPS_CALCULATED;
            } else {
                //Cap to Max FPS
                return (frameCount <= MAX_FPS) ? frameCount : MAX_FPS;
            }
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            return NO_FPS_CALCULATED;
        }

    }

    private String[] splitLine(String input){
        String[] result = input.split("\t");
        return result;
    }


    private String[] getFPSCommandOutput(){

        String out = getRunningCommandOutput(FPS_COMMAND);
        String[] lines = out.split("\n");
        //	Log.i(TAG, "Num Lines " + lines.length);

        return lines;
    }


    public String getRunningCommandOutput(String command){
        try {
            stdin.write((command).getBytes());


            stdin.flush();

            byte[] buffer = new byte[BUFF_LEN];
            int read;
            String out = new String();
            //read method will wait forever if there is nothing in the stream
            //so we need to read it in another way than while((read=stdout.read(buffer))>0)
            while(true){
                int bytesAvailable = stdout.available();
                if(bytesAvailable == 0){
                    throw new Exception();
                }
                read = stdout.read(buffer);
                out += new String(buffer, 0, read);
                if(read<BUFF_LEN){
                    break;
                }
            }

            return out;
        } catch (Exception ex) {
            return new String();
        }

    }

}
