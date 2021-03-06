package com.yeokm1.fpsmeasure;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yeokm1 on 17/4/2015.
 */
public class CommandHandler {


    private static final int UPDATE_RATE = 500;
    public static final float TIME_INTERVAL_NANO_SECONDS = 1000000000;
    public static final int BUFF_LEN = 1000;


    public static final int MAX_FPS = 60;
    public static final int NO_FPS_CALCULATED = -1;
    private static final String FPS_COMMAND = "dumpsys SurfaceFlinger --latency SurfaceView\n";

    private static final String FILE_CPU_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed";
    private static final String FILE_GPU_NEXUS5_FREQ = "/sys/class/kgsl/kgsl-3d0/gpuclk";


    private static final String TAG = "CommandHandler";

    private Process process;
    private DataOutputStream stdin;
    private InputStream stdout;

    private ScheduledExecutorService scheduler;

    private boolean processOfGettingFPS = false;

    private Intent fgServiceIntent;

    private Context context;

    private boolean measuring = false;

    public CommandHandler(Context context){
        this.context = context;
        fgServiceIntent = new Intent(context.getApplicationContext(), FgService.class);
    }

    public void startFPSMeasure(){
        measuring = true;
        context.startService(fgServiceIntent);

        try {


            Process process = Runtime.getRuntime().exec("su");
            stdin = new DataOutputStream(process.getOutputStream());
            stdout = process.getInputStream();


            scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {

                            if(!measuring){
                               return;
                            }

                            if(processOfGettingFPS){
                                return;
                            }

                            try {
                                processOfGettingFPS = true;
                                int fps = getFPS();
                                long cpu = getCPUFreq();
                                long gpu = getGPUFreq();

                                fgServiceIntent.putExtra(FgService.INTENT_FPS_DATA, fps);
                                fgServiceIntent.putExtra(FgService.INTENT_CPU_DATA, cpu);
                                fgServiceIntent.putExtra(FgService.INTENT_GPU_DATA, gpu);

                                context.startService(fgServiceIntent);

                                Log.i(TAG, "FPS: " + fps);
                            }catch (Exception e){
                                Log.e(TAG, "Scheduler " + e.getMessage());
                            } finally{
                                processOfGettingFPS = false;
                            }




                        }
                    }, 0, UPDATE_RATE, TimeUnit.MILLISECONDS);

        } catch (IOException e) {

        }

    }


    public void stopFPSMeasure(){
        measuring = false;

        if(scheduler != null){
            scheduler.shutdownNow();
            scheduler = null;
        }

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

        context.stopService(fgServiceIntent);

    }

    private long getCPUFreq(){
        String cpuStr =  getStringFromFile(FILE_CPU_FREQ);
        long value = Long.valueOf(cpuStr);
        return value;
    }

    private long getGPUFreq(){
        String gpuStr = getStringFromFile(FILE_GPU_NEXUS5_FREQ);
        long value = Long.valueOf(gpuStr);
        return value;
    }


    private int getFPS(){

        try {
            String[] output = getFPSCommandOutput();

            if (output.length == 0 || output.length == 1) {
                return NO_FPS_CALCULATED;
            }

            //First line is not used

            //Ignore the last line as it can be inaccurate
            String lastLine = output[output.length - 2];
            String[] split = splitLine(lastLine);

            if(split.length != 3){
                return NO_FPS_CALCULATED;
            }

            String lastFrameFinishTimeStr = split[2];

            double lastFrameFinishTime = Double.parseDouble(lastFrameFinishTimeStr);
            int frameCount = 0;

            //Ignore the last line as it can be inaccurate
            for (int i = 1; i < (output.length - 1); i++) {
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
            Log.e(TAG, "getFPS " + e.getMessage());
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


    public String getStringFromFile(String filePath){
        try {
            RandomAccessFile reader = new RandomAccessFile(filePath, "r");
            String output = reader.readLine();
            reader.close();
            return output;
        } catch (IOException e) {
            return "";
        }

    }
}
