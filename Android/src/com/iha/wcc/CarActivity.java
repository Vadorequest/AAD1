package com.iha.wcc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
     * Information about the server to reach.
     */
    private static String serverIpAddress;
    private static int serverPort = 5555;

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
		setContentView(R.layout.activity_car);		
		
		// Define a static context. Useful for the anonymous events.
		context = getApplicationContext();
		
		// Initialize view components.
		this.initializeComponents();
		
		// Bind listeners once all components are initialized.
		this.initializeListeners();
		
		// Initialize the car and the application.
		Bundle extras = getIntent().getExtras();
		this.initializeCar((String)extras.get("name"), (String)extras.get("ip"));
	}

    @Override
    protected void onStart() {
        stopProcessingSocket.set(false);
        if(socketThread == null){
            socketThread = new Thread(networkRunnable);
            socketThread.start();
        }
        super.onStart();
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
		// On click, take a picture.
		this.pictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doPhoto();
			}
		});
		
		// On click, sound a honk.
		this.honkBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doHonk();
			}
		});
		
		// On click, go to the settings page.
		this.settingsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displaySettings();
			}
		});

        // Use onClick event is better for debug but worse for real use.
        // On click, go forward.
		this.goForwardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goForward();
            }
        });

		// On click, go backward.
		this.goBackwardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackward();
            }
        });

		// On click, go to the left.
		this.goLeftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goLeft();
            }
        });

		// On click, go to the right.
		this.goRightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goRight();
            }
        });

        // On click, stop the car.
        this.doStopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop();
            }
        });

        // Use onTouch event send to much queries, bad for debug but should be the final way to do it.
		// On touch, go forward.
		this.goForwardBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goForward();
				return false;
			}
		});
		
		// On touch, go backward.
		this.goBackwardBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goBackward();
				return false;
			}
		});
		
		// On touch, go to the left.
		this.goLeftBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goLeft();
				return false;
			}
		});
		
		// On touch, go to the right.
		this.goRightBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goRight();
				return false;
			}
		});
	}
	
	// The next methods communicate with the car.
	
	/**
	 * Initialize the car, load the local phone settings and send them to the car.
	 * Save in session useful information about the car.
	 * @param name Network SSID.
	 * @param ip IP address of the car.
	 */
	private void initializeCar(String name, String ip){
		// Initialize the CarSocket and CarHttpRequest classes with available information about the host to connect.
        serverIpAddress = ip;

        // TODO Send the settings to the car.
        //send("settings", "SETTINGS...");
        //Car.setSettings(5, 2, 10, 4, 0, 0, 5, 255, 2, 150);
        //Car.setSettings(50, 20, 100, 40, 0, 0, 50, 2550, 20, 1500);// Fake settings to test the limits.
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
        send(Car.calculateSpeed(Car.Direction.LEFT));
	}
	
	/**
	 * Send a request to the car to go to the right.
	 */
	private void goRight(){
        send(Car.calculateSpeed(Car.Direction.RIGHT));
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
        send("photo");
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
		Toast.makeText(context, "I don't know really how did that in only one screen guys! We should discuss about :)", Toast.LENGTH_SHORT).show();
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

        log("Speed: " + Car.speed + " | Direction: " + Car.lastDirection);
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
}
