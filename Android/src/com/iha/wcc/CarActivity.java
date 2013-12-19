package com.iha.wcc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


import android.widget.ImageView;
import com.iha.wcc.job.camera.MjpegVideoStreamTask;
import com.iha.wcc.job.camera.MjpegView;
import com.iha.wcc.job.camera.TakePictureTask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iha.wcc.job.car.Camera;
import com.iha.wcc.job.car.Linino;
import com.iha.wcc.job.ssh.SshTask;
import com.iha.wcc.job.car.Car;

public class CarActivity extends FragmentActivity {

    /**
     * Static instance of itself.
     */
    public static Context context;

    /**
     * Don't re-run start video stream when one is done, it's useless.
     */
    public static boolean videoStreamStarted;

    /**
     * Tag used to debug.
     */
    private final static String TAG_DEBUG = ">==< ArduinoYun >==<";

    /**
     * Information about the arduino to reach.
     * Use default values from the Car class if they are not provided.
     */
    private static String serverIpAddress;
    private static int serverPort;

    /**
     * Array of strings that contains all messages to send to the server using sockets.
     */
    private ArrayBlockingQueue<String> queriesQueueSocket = new ArrayBlockingQueue<String>(255);

    /**
     * Atomic boolean shared and accessible between different threads, used to be sure the connection is available.
     */
    private AtomicBoolean stopProcessingSocket = new AtomicBoolean(false);

    /**
     * Contains the values to write in the socket stream.
     */
    private OutputStream outputStreamSocket = null;

    /**
     * Socket connected to the Arduino.
     */
    private Socket socket = null;

    /**
     * Thread which manage socket streams.
     */
    private static Thread socketThread = null;

    /**
     * Runnable running in another thread, responsible to the communication with the car.
     */
    private final Runnable networkRunnable = new Runnable() {
        @Override
        public void run() {
            log("starting network thread");

            try {
                socket = new Socket(serverIpAddress, serverPort);
                outputStreamSocket = socket.getOutputStream();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                stopProcessingSocket.set(true);
                log("UnknownHostException");
            } catch(final ConnectException e){// Final because we need it in the runnable.
                runOnUiThread(new Runnable(){
                    public void run(){
                        // Warn the user because the connection is wrong.
                        Toast.makeText(context, "Unable to connect to the car, are you sure to be connected on the car network?" + e.getMessage(), Toast.LENGTH_LONG).show();
                        Toast.makeText(context, "Typically "+Linino.DEFAULT_NETWORK_IP+":"+Linino.DEFAULT_NETWORK_PORT+", you are connected at "+serverIpAddress+":"+serverPort, Toast.LENGTH_LONG).show();
                    }
                });
                stopProcessingSocket.set(true);
            } catch (IOException e1) {
                e1.printStackTrace();
                stopProcessingSocket.set(true);
            }

            queriesQueueSocket.clear(); // we only want new values

            // Initialize once the thread is running.
            initializeCarSettings();

            try {
                while(!stopProcessingSocket.get()){
                    String val = queriesQueueSocket.take();
                    if(val != "-1"){
                        log("Sending value "+val);
                        outputStreamSocket.write((val + "\n").getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally{
                try {
                    stopProcessingSocket.set(true);
                    if(outputStreamSocket != null) outputStreamSocket.close();
                    if(socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            log("returning from network thread");
            socketThread = null;
        }
    };

    /**
     * Shared preferences.
     */
    private SharedPreferences prefs;

    // View components.
    private MjpegView cameraContent;// Contains the Mjpeg view which contains all other components and the video stream.
    private ImageButton pictureBtn;// Take a picture.
    private ImageButton honkBtn;// Play a sound.
    private ImageButton settingsBtn;// Go to settings.
    private ImageButton goForwardBtn;
    private ImageButton goBackwardBtn;
    private ImageButton goLeftBtn;
    private ImageButton goRightBtn;
    private ImageButton doStopBtn;
    private ImageButton doStopTurnBtn;
    private TextView speedText;// Displays the current speed.
    private ImageView sensView;// Displays the current sens.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable title, set full screen mode.
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        this.refreshStreamingView();

        // Define a static context. Useful for the anonymous events.
        context = getApplicationContext();

        // Get settings.
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize view components.
        this.initializeComponents();

        // Bind listeners once all components are initialized.
        this.initializeListeners();

        // Initialize the car and the application.
        Bundle extras = getIntent().getExtras();

        // Initialize car network to reach. Settings will be updated when on the onStart() method.
        this.initializeCar(
                Linino.getNetworkIp(prefs),
                Linino.getNetworkPort(prefs)
        );

        // Start the camera stream from the camera using SSH.
        if(!videoStreamStarted){
            new SshTask(this.cameraContent, context, serverIpAddress, Linino.getUserSsh(prefs), Linino.getPasswordSsh(prefs), Camera.getCommand(prefs)).execute();
        }
    }

    @Override
    protected void onStart() {
        stopProcessingSocket.set(false);
        if(socketThread == null){
            socketThread = new Thread(networkRunnable);
            socketThread.start();
        }

        // Start to stream the video.
        this.startStreaming();

        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(cameraContent != null){
            cameraContent.stopPlayback();//Necessary for the video
        }
    }

    @Override
    protected void onStop() {
        // Next time the view will be called it will automatically refresh all the components.
        cameraContent = null;

        // Stop sockets.
        stopProcessingSocket.set(true);
        queriesQueueSocket.clear();
        queriesQueueSocket.offer("-1");
        if(socketThread != null) socketThread.interrupt();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Start the video stream next time the application is ran.
        videoStreamStarted = false;
        this.finish();
    }

    /**
     * Initialize all view components.
     */
    private void initializeComponents() {
        this.pictureBtn = (ImageButton) findViewById(R.id.pictureBtn);
        this.honkBtn = (ImageButton) findViewById(R.id.honkBtn);
        this.settingsBtn = (ImageButton) findViewById(R.id.settingsBtn);
        this.goForwardBtn = (ImageButton) findViewById(R.id.goForwardBtn);
        this.goBackwardBtn = (ImageButton) findViewById(R.id.goBackwardBtn);
        this.goLeftBtn = (ImageButton) findViewById(R.id.goLeftBtn);
        this.goRightBtn = (ImageButton) findViewById(R.id.goRightBtn);
        this.doStopBtn = (ImageButton) findViewById(R.id.doStopBtn);
        this.doStopTurnBtn = (ImageButton) findViewById(R.id.doStopTurnBtn);
        this.speedText = (TextView) findViewById(R.id.speedText);
        this.sensView = (ImageView) findViewById(R.id.sensView);
    }

    /**
     * Bind all button listeners. (called during the initialization)
     */
    private void initializeListeners(){
        /*
        ******** Forward **********
        */
        this.goForwardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goForward();
            }
        });

        this.goForwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                goForward();
                return false;
            }
        });

        this.goForwardBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                goForward();
                return false;
            }
        });

        /*
        ******** Backward **********
        */
        this.goBackwardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackward();
            }
        });

        this.goBackwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                goBackward();
                return false;
            }
        });

        this.goBackwardBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                goBackward();
                return false;
            }
        });

        /*
        ******** Left **********
        */
        this.goLeftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    goLeft();
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    doStopTurn();
                }

                return false;
            }
        });

        /*
        ******** Right **********
        */
        this.goRightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    goRight();
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    doStopTurn();
                }
                return false;
            }
        });

        /*
        ******** Stop **********
        */
        this.doStopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop();
            }
        });
        /*
        ******** Stop turn **********
        */
        this.doStopTurnBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doStopTurn();
            }
        });

        /*
        ******** Picture **********
        */
        this.pictureBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doPhoto();
            }
        });

        /*
        ******** Honk **********
        */
        this.honkBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    // Touch is active, do honk.
                    doHonk();
                }

                return false;
            }
        });

        this.honkBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                doHonk();
                return false;
            }
        });

        /*
        ******** Settings **********
        */
        this.settingsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySettings();
            }
        });
    }

    // The next methods communicate with the car.

    /**
     * Initialize the car such as the arduino settings (ip/port) to reach.
     * @param ip IP address of the car.
     * @param port Port used to communicate with the Arduino.
     */
    private void initializeCar(String ip, int port){
        // Initialize the CarSocket and CarHttpRequest classes with available information about the host to connect.
        serverIpAddress = ip;
        serverPort = port;
    }

    /**
     * Get the car settings from the local phone settings and send them to the car.
     */
    private void initializeCarSettings(){
        // Update android Car class settings.
        Car.setSettings(
                Integer.parseInt(prefs.getString("speedAccelerationForward", String.valueOf(Car.getSpeedAccelerationForward()))),
                Integer.parseInt(prefs.getString("speedAccelerationBackward", String.valueOf(Car.getSpeedAccelerationBackward()))),
                Integer.parseInt(prefs.getString("speedDecelerationForward", String.valueOf(Car.getSpeedDecelerationForward()))),
                Integer.parseInt(prefs.getString("speedDecelerationBackward", String.valueOf(Car.getSpeedDecelerationBackward()))),
                Integer.parseInt(prefs.getString("speedDecTurnForward", String.valueOf(Car.getSpeedDecTurnForward()))),
                Integer.parseInt(prefs.getString("speedDecTurnBackward", String.valueOf(Car.getSpeedDecTurnBackward()))),
                Integer.parseInt(prefs.getString("minSpeedForward", String.valueOf(Car.getMinSpeedForward()))),
                Integer.parseInt(prefs.getString("maxSpeedForward", String.valueOf(Car.getMaxSpeedForward()))),
                Integer.parseInt(prefs.getString("minSpeedBackward", String.valueOf(Car.getMinSpeedBackward()))),
                Integer.parseInt(prefs.getString("maxSpeedBackward", String.valueOf(Car.getMaxSpeedBackward()))),
                Integer.parseInt(prefs.getString("speedTurnMotor", String.valueOf(Car.getSpeedTurnMotor()))));

        // Update arduino Car device settings without updating the view.
        this.send("settings", Car.speed + "/" + prefs.getString("soundPreferences", String.valueOf(Car.DEFAULT_TONE_FREQUENCY)), false);
        Log.d("ArduinoYun", prefs.getString("soundPreferences", String.valueOf(Car.DEFAULT_TONE_FREQUENCY)));
    }

    /**
     * Start the video streaming.
     */
    private void startStreaming() {
        if(this.cameraContent == null){
            refreshStreamingView();

            // Initialize view components.
            this.initializeComponents();

            // Bind listeners once all components are initialized.
            this.initializeListeners();
        }
        new MjpegVideoStreamTask(context, this.cameraContent).execute(Camera.DEFAULT_CAMERA_STREAMING_URL);
    }

    /**
     * Refresh the entire view with the special Mjpeg view to stream the video.
     */
    private void refreshStreamingView() {
        // Use a Mjpeg view instead of a "normal" view to stream the video.
        this.setContentView(cameraContent = new MjpegView(this));

        LayoutInflater inflater = this.getLayoutInflater();
        this.getWindow().addContentView(
                // Use the old layout to display the controls.
                inflater.inflate(R.layout.activity_car, null),
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
    }

    /**
     * Send a request to the car to go forward.
     */
    private void goForward(){
        send(Car.calculateSpeed(Car.Direction.FORWARD));
    }

    /**
     * Send a request to the car to go backward.
     */
    private void goBackward(){
        send(Car.calculateSpeed(Car.Direction.BACKWARD));
    }

    /**
     * Send a request to the car to go to the left.
     */
    private void goLeft(){
        send(Car.calculateSpeed(Car.Direction.LEFT), Car.getSpeedTurnMotor()+"");
    }

    /**
     * Send a request to the car to go to the right.
     */
    private void goRight(){
        send(Car.calculateSpeed(Car.Direction.RIGHT), Car.getSpeedTurnMotor()+"");
    }

    /**
     * Send a request to the car to stop turn.
     */
    private void doStopTurn(){
        send("stopTurn");
    }

    /**
     * Send a request to the car to go to the right.
     */
    private void doStop(){
        send(Car.calculateSpeed(Car.Direction.STOP));
    }

    /**
     * Send a request to the car to take a photo to store on the SD card.
     */
    private void doPhoto(){
        new TakePictureTask(context).execute(Camera.DEFAULT_CAMERA_PICTURE_URL);
    }

    /**
     * Send a request to the car to generate a a sound from the car (honk).
     */
    private void doHonk(){
        send("honk");
    }

    /**
     * Display settings activity.
     * Stop the car to avoid accident.
     */
    private void displaySettings(){
        // Stop the car before kill somebody.
        this.doStop();

        // Load the settings interface.
        Intent intentSettings = new Intent(this, SettingsActivity.class);
        startActivity(intentSettings);
        this.finish();
    }

    /**
     * Send a message using the socket connection to the Arduino.
     * @param action Action to execute.
     */
    private void send(String action){
        this.send(action, String.valueOf(Car.speed));
    }

    /**
     * Send a message using the socket connection to the Arduino.
     * Send params instead of the speed.
     * @param action Action to execute.
     * @param params Params to the action.
     */
    private void send(String action, String params){
        this.send(action, params, true);
    }

    /**
     * Send a message using the socket connection to the Arduino.
     * Send params instead of the speed.
     * Don't update the view if asked. Useful for call to method who don't need to update the view.
     * @param action Action to execute.
     * @param params Params to the action.
     * @param updateView
     */
    private void send(String action, String params, boolean updateView){
        // Send the message in the socket pool.
        queriesQueueSocket.offer(action + "/" + params);

        if(updateView){
            // Update the displayed speed in the view.
            this.updateViewSpeed(Car.speed);

            // Update the direction displayed on the view.
            this.updateViewSens(Car.lastSens);
        }
    }

    /**
     * Update the sens displayed on the view.
     * @param sens The new sens of the car.
     */
    private void updateViewSens(Car.Direction sens) {
        if(sens == Car.Direction.FORWARD || sens == Car.Direction.STOP){
            this.sensView.setImageResource(R.drawable.ic_going_forward);
        }else{
            this.sensView.setImageResource(R.drawable.ic_going_backward);
        }
    }

    /**
     * Update the displayed speed in the view.
     * @param speed The new speed used by the car.
     */
    private void updateViewSpeed(int speed) {
        this.speedText.setText(speed + " Km/h");
    }

    /**
     * Debug log.
     * @param message Message to display.
     */
    private void log(String message){
        Log.d(TAG_DEBUG, message);
    }
}