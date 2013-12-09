package com.iha.wcc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iha.wcc.job.car.Car;

public class CarActivity extends FragmentActivity {

    /**
     * Static instance of itself.
     */
    public static Context context;

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
     * For the video player
     */
    private static final String TAG = "MjpegActivity";
    private MjpegView mv;
    private String URL = "http://64.122.208.241:8000/axis-cgi/mjpg/video.cgi";
    //private String URL = "http://192.168.240.1:8080/?action=stream";
    
    /**
     * Path for the picture
     */
    //private String takeSpanshot = "http://192.168.240.1:8080/?action=snapshot";
    private String takeSpanshot = "http://sergi1985.files.wordpress.com/2012/03/futurama-fry-meme-generator-why-but-whyy-aac252.jpg";
    
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
            } catch(ConnectException e){
                runOnUiThread(new Runnable(){
                    public void run(){
                        // Warn the user because the connection is wrong.
                        Toast.makeText(
                                context,
                                "Unable to connect to the car, are you sure to be connected on the car network? Typically 192.168.240.1, you are connected at "+serverIpAddress,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
                stopProcessingSocket.set(true);
            } catch (IOException e1) {
                e1.printStackTrace();
                stopProcessingSocket.set(true);
            }

            queriesQueueSocket.clear(); // we only want new values

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

    // View components.
    private ImageView cameraContent;// The entire screen which displays the video stream or the last photo from the car.
    private ImageButton pictureBtn;// Take a picture.
    private ImageButton honkBtn;// Play a sound.
    private ImageButton settingsBtn;// Go to settings.
    private ImageButton goForwardBtn;
    private ImageButton goBackwardBtn;
    private ImageButton goLeftBtn;
    private ImageButton goRightBtn;
    private ImageButton doStopBtn;
    private TextView speedText;// Displays the current speed.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Previous code
        //setContentView(R.layout.activity_car);

        //Loading first the video Custom View and after that adding the normal view

        //External video
        //String URL = "http://64.122.208.241:8000/axis-cgi/mjpg/video.cgi";
        //Arduino feed
        //String URL = "http://192.168.240.1:8080/?action=stream";

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mv = new MjpegView(this);
        setContentView(mv);

        LayoutInflater inflater = getLayoutInflater();
        getWindow().addContentView(inflater.inflate(R.layout.activity_car, null),
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));
        new DoRead().execute(URL);


        // Define a static context. Useful for the anonymous events.
        context = getApplicationContext();

        // Initialize view components.
        this.initializeComponents();

        // Bind listeners once all components are initialized.
        this.initializeListeners();

        // Initialize the car and the application.
        Bundle extras = getIntent().getExtras();

        // Initialize car network to reach. Settings will be updated when on the onStart() method.
        this.initializeCar(
                // Use the pre-defined constant as default but try to get custom config if exists to configure the arduino to reach.
                extras.containsKey("ip") ? (String)extras.get("ip") : Car.DEFAULT_NETWORK_IP,
                extras.containsKey("port") ? Integer.parseInt((String)extras.get("port")) : Car.DEFAULT_NETWORK_PORT
        );
    }

    @Override
    protected void onStart() {
        stopProcessingSocket.set(false);
        if(socketThread == null){
            socketThread = new Thread(networkRunnable);
            socketThread.start();
        }
        // Load or reload the car settings. TODO Reload only if they was changed, not every time.
        this.initializeCarSettings();

        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mv.stopPlayback();	//Necessary for the video
    }

    @Override
    protected void onStop() {
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

    /**
     * Initialize all view components.
     */
    private void initializeComponents() {
        this.cameraContent = (ImageView) findViewById(R.id.cameraContent);
        this.pictureBtn = (ImageButton) findViewById(R.id.pictureBtn);
        this.honkBtn = (ImageButton) findViewById(R.id.honkBtn);
        this.settingsBtn = (ImageButton) findViewById(R.id.settingsBtn);
        this.goForwardBtn = (ImageButton) findViewById(R.id.goForwardBtn);
        this.goBackwardBtn = (ImageButton) findViewById(R.id.goBackwardBtn);
        this.goLeftBtn = (ImageButton) findViewById(R.id.goLeftBtn);
        this.goRightBtn = (ImageButton) findViewById(R.id.goRightBtn);
        this.doStopBtn = (ImageButton) findViewById(R.id.doStopBtn);
        this.speedText = (TextView) findViewById(R.id.speedText);
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
                    stopTurn();
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
                    stopTurn();
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
        ******** Picture **********
        */
        this.pictureBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	new ImageDownloader().execute(takeSpanshot);
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
        // Get settings.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

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

        // Update arduino Car device settings.
        this.send("settings", Car.speed + "/" + prefs.getString("sound_preferences", String.valueOf(Car.DEFAULT_TONE_FREQUENCY)));// TODO: More settings.
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
        send(Car.calculateSpeed(Car.Direction.LEFT), Car.getSpeedTurnMotor()+"");// 100 for the force motor rear wheels. TODO use setting value.
    }

    /**
     * Send a request to the car to go to the right.
     */
    private void goRight(){
        send(Car.calculateSpeed(Car.Direction.RIGHT), Car.getSpeedTurnMotor()+"");// 100 for the force motor rear wheels.
    }

    /**
     * Send a request to the car to stop turn.
     */
    private void stopTurn(){
        send("stopTurn");
    }

    /**
     * Send a request to the car to go to the right.
     */
    private void doStop(){
        send(Car.calculateSpeed(Car.Direction.STOP));
    }

    /**
     * Send a request to the car to generate a a sound from the car (honk).
     */
    private void doHonk(){
        send("honk");
    }

    /**
     * Display settings.
     * TODO We don't know yet how it will works, another page? Could be better to have all the stuff on the same page but could be difficult... [Alvarro]
     */
    private void displaySettings(){
        // Stop the car before kill somebody.
        this.doStop();

        // Load the settings interface.

        Intent intentSettings = new Intent(this, SettingsActivity.class);
        //Intent intentSettings = new Intent(this, MjpegActivity.class);
        startActivity(intentSettings);
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
        // Send the message in the socket pool.
        queriesQueueSocket.offer(action + "/" + params);

        // Update the displayed speed in the view.
        this.updateViewSpeed(Car.speed);

        // Update the direction displayed on the view.
        this.updateViewDirection(Car.lastDirection);
    }

    /**
     * Update the direction displayed on the view.
     * @param direction The new direction of the car.
     * @TODO Use an image or something more beautiful.
     */
    private void updateViewDirection(Car.Direction direction) {
        speedText.setText((direction == Car.Direction.FORWARD ? "+" : (direction == Car.Direction.BACKWARD ? "-" : speedText.getText().charAt(0) + "")) + speedText.getText().toString());
    }

    /**
     * Update the displayed speed in the view.
     * @param speed The new speed used by the car.
     */
    private void updateViewSpeed(int speed) {
        speedText.setText(speed + " Km/h");
    }

    /**
     * Debug log.
     * @param message Message to display.
     */
    private void log(String message){
        Log.d(TAG_DEBUG, message);
    }
    /**
     * 
     * Video logic
     *
     */
    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
                //Error connecting to camera
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);
            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mv.showFps(true);
        }
    }
    
    /**
     * Image logic
     */
    
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    	 
        @Override
        protected Bitmap doInBackground(String... param) {
            return downloadBitmap(param[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        	Date now = new Date();
        	storeImage(result, "/" + formatter.format(now) + ".jpg");
        }
 
        private Bitmap downloadBitmap(String url) {
            // initialize the default HTTP client object
            final DefaultHttpClient client = new DefaultHttpClient();
 
            //forming a HttpGet request
            final HttpGet getRequest = new HttpGet(url);
            try {
 
                HttpResponse response = client.execute(getRequest);
 
                //check 200 OK for success
                final int statusCode = response.getStatusLine().getStatusCode();
 
                if (statusCode != HttpStatus.SC_OK) {
                    Log.w("ImageDownloader", "Error " + statusCode +
                            " while retrieving bitmap from " + url);
                    return null;
                }
 
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        // getting contents from the stream
                        inputStream = entity.getContent();
 
                        // decoding stream data back into image Bitmap that android understands
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
 
                        return bitmap;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {
                getRequest.abort();
                Log.e("ImageDownloader", "Something went wrong while" +
                        " retrieving bitmap from " + url + e.toString());
            }
            return null;
        }
    }
    
    private boolean storeImage(Bitmap imageData, String filename) {

    	String iconsStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WIFICar";
    	File sdIconStorageDir = new File(iconsStoragePath);

    	//Create storage directories, if nonexistent
    	sdIconStorageDir.mkdirs();

    	try {
    		String filePath = sdIconStorageDir.toString() + filename;
    		FileOutputStream fileOutputStream = new FileOutputStream(filePath);

    		BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

    		imageData.compress(CompressFormat.JPEG, 100, bos);

    		bos.flush();
    		bos.close();

    	} catch (FileNotFoundException e) {
    		Log.w("TAG", "Error saving image file: " + e.getMessage());
    		return false;
    	} catch (IOException e) {
    		Log.w("TAG", "Error saving image file: " + e.getMessage());
    		return false;
    	}

    	return true;
    }

}