package com.iha.wcc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Ambroise on 12/11/13.
 */
public class CarSocketActivity extends Activity {
    private final static String TAG = ">==< ArduinoYun >==<";
    private final static String ARDUINO_IP_ADDRESS = "192.168.240.1";
    private final static int PORT = 5555;

    private SeekBar mSeekBar;

    private ArrayBlockingQueue<String> mQueue = new ArrayBlockingQueue<String>(100);
    private AtomicBoolean mStop = new AtomicBoolean(false);

    private OutputStream mOutputStream = null;

    private Socket mSocket = null;

    private static Thread sNetworkThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_socket);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mQueue.offer(String.valueOf(progress));
            }
        });

    }

    @Override
    protected void onStart() {
        mStop.set(false);
        if(sNetworkThread == null){
            sNetworkThread = new Thread(mNetworkRunnable);
            sNetworkThread.start();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        mStop.set(true);
        mQueue.clear();
        mQueue.offer("-1");
        if(sNetworkThread != null) sNetworkThread.interrupt();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private final Runnable mNetworkRunnable = new Runnable() {

        @Override
        public void run() {
            log("starting network thread");

            try {
                mSocket = new Socket(ARDUINO_IP_ADDRESS, PORT);
                mOutputStream = mSocket.getOutputStream();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                mStop.set(true);
            } catch (IOException e1) {
                e1.printStackTrace();
                mStop.set(true);
            }

            mQueue.clear(); // we only want new values

            try {
                while(!mStop.get()){
                    String val = mQueue.take();
                    if(val != "-1"){
                        log("sending value "+val);
                        mOutputStream.write((val+"\n").getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally{
                try {
                    mStop.set(true);
                    if(mOutputStream != null) mOutputStream.close();
                    if(mSocket != null) mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            log("returning from network thread");
            sNetworkThread = null;
        }
    };

    public void log(String s){
        Log.d(TAG, s);
    }
}