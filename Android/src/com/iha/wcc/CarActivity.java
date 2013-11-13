package com.iha.wcc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CarActivity extends Activity {
	/**
	 * Static instance of itself.
	 */
	public static Context context;

    /**
     * Tag used to debug.
     */
    private final static String TAG = ">==< ArduinoYun >==<";

    /**
     * Information about the server to reach.
     */
    private static String serverIpAddress;
    private static int serverPort = 5555;

    /**
     * Array of strings that contains all messages to send to the server using sockets.
     */
    private ArrayBlockingQueue<String> mQueue = new ArrayBlockingQueue<String>(255);
    private AtomicBoolean mStop = new AtomicBoolean(false);

    /**
     * Contains the values to write in the socket stream.
     */
    private OutputStream mOutputStream = null;

    /**
     * Socket connected to the Arduino.
     */
    private Socket mSocket = null;

    /**
     * Thread which manage socket streams.
     */
    private static Thread sNetworkThread = null;

    /**
     * Runnable running in another thread, responsible to the communication with the car.
     */
    private final Runnable mNetworkRunnable = new Runnable() {

        @Override
        public void run() {
            log("starting network thread");

            try {
                mSocket = new Socket(serverIpAddress, serverPort);
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
	
	// View components.
	private ImageView cameraContent;
	private ImageButton pictureBtn;
	private ImageButton honkBtn;
	private ImageButton settingsBtn;
	private ImageButton goForwardBtn;
	private ImageButton goBackwardBtn;
	private ImageButton goLeftBtn;
	private ImageButton goRightBtn;
	private ImageButton doStopBtn;

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

		// On touch, stop the car.
		this.doStopBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
                doStop();
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.car, menu);
		return true;
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
	}
	
	/**
	 * Send a request to the car to go forward.
	 */
	private void goForward(){
        Car.calculSpeed(Car.Direction.FORWARD);
        send("forward");
	}
	
	/**
	 * Send a request to the car to go backward.
	 */
	private void goBackward(){
        Car.calculSpeed(Car.Direction.BACKWARD);
        send("backward");
	}
	
	/**
	 * Send a request to the car to go to the left.
	 */
	private void goLeft(){
        Car.calculSpeed(Car.Direction.LEFT);
        send("left");
	}
	
	/**
	 * Send a request to the car to go to the right.
	 */
	private void goRight(){
        Car.calculSpeed(Car.Direction.RIGHT);
        send("right");
	}

	/**
	 * Send a request to the car to go to the right.
	 */
	private void doStop(){
        Car.calculSpeed(Car.Direction.STOP);
        send("stop");
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
		Toast.makeText(context, "I don't know really how did that in only one screen guys! We should discuss about :)", Toast.LENGTH_SHORT).show();
	}

    /**
     * Send a message using the socket connection to the Arduino.
     * @param action
     */
    private void send(String action){
        mQueue.offer(action + "/" + Car.speed);
    }

    /**
     * Send a message using the socket connection to the Arduino.
     * Send params instead of the speed.
     * @param action
     * @param params
     */
    private void send(String action, String params){
        mQueue.offer(action + "/" + params);
    }

    /**
     * Debug log.
     * @param s Message to display.
     */
    public void log(String s){
        Log.d(TAG, s);
    }

    /**
     * Class that represents the current controlled car.
     * Contains all values about it.
     */
    private static class Car {

        /*
         ******************************************* CONSTANTS *****************************************
         */

        /**
         * Each time forward is called the speed is incremented.
         */
        public final static int SPEED_INC_FORWARD = 10;

        /**
         * Each time backward is called the speed is incremented.
         */
        public final static int SPEED_INC_BACKWARD = 5;

        /**
         * Speed is decremented when we turn going forward.
         */
        public final static int SPEED_DEC_TURN_FORWARD = 10;

        /**
         * Speed is decremented when we turn going backward.
         */
        public final static int SPEED_DEC_TURN_BACKWARD = 20;

        /**
         * Maximal speed available for forward direction.
         */
        public final static int MAX_SPEED_FORWARD = 250;

        /**
         * Minimal speed available for forward direction.
         */
        public final static int MIN_SPEED_FORWARD = 5;

        /**
         * Maximal speed available for backward direction.
         */
        public final static int MAX_SPEED_BACKWARD = 125;

        /**
         * Minimal speed available for backward direction.
         */
        public final static int MIN_SPEED_BACKWARD = 5;

        /*
         ******************************************* VARIABLES *****************************************
         */

        /**
         * Speed of the car.
         */
        public static int speed = 0;

        /**
         * Last direction used by the car. Stopped by default.
         */
        public static Direction lastDirection = Direction.STOP;

        /**
         * List of available directions.
         */
        public static enum Direction {
            FORWARD,
            BACKWARD,
            LEFT,
            RIGHT,
            STOP
        };

        /*
         ******************************************* METHODS *****************************************
         */

        /**
         * Calcul the new speed.
         * @param direction The direction of the car.
         */
        public static int calculSpeed(Direction direction) {
            // If we ask to stop, just stop.
            if(direction == Direction.STOP){
                speed = 0;
                return speed;
            }

            // Else it's a little more funny. (Wrote at 2 a.m)
            if(direction == lastDirection){
                // We keep the same direction.
                switch (direction){
                    case FORWARD :
                        _accelerateForward();
                        break;
                    case BACKWARD :
                        _accelerateBackward();
                        break;
                    case LEFT :
                    case RIGHT :
                        // Speed still the same.
                        break;
                }
            }else{
                // Depending on the last direction used.
                switch (lastDirection){
                    case FORWARD :
                        if(direction == Direction.BACKWARD){
                            // If we was going forward and now going backward OR if we stop.
                            speed = 0;
                        } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                            // If we turn going forward.
                            _turnForward();
                        }
                        break;
                    case BACKWARD :
                        if(direction == Direction.FORWARD){
                            // If we was going backward and now going forward OR if we stop.
                            speed = 0;
                        } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                            // If we turn going forward.
                            _turnBackward();
                        }
                        break;
                    case LEFT :
                    case RIGHT :
                    case STOP :
                        // Increase the speed depending of the direction if we are going forward or backward.
                        switch (direction){
                            case FORWARD :
                                _accelerateForward();
                                break;
                            case BACKWARD :
                                _accelerateBackward();
                                break;
                        }
                        break;
                }
            }

            // Update the last direction used.
            lastDirection = direction;

            // Return the new speed to use.
            return speed;
        }

        /**
         * Increase the speed going forward.
         */
        private static void _accelerateForward(){
            if(speed + SPEED_INC_FORWARD < MAX_SPEED_FORWARD){
                speed += SPEED_INC_FORWARD;
            }else{
                speed = MAX_SPEED_FORWARD;
            }
        }

        /**
         * Increase the speed going backward.
         */
        private static void _accelerateBackward() {
            if(speed + SPEED_INC_BACKWARD < MAX_SPEED_BACKWARD){
                speed += SPEED_INC_BACKWARD;
            }else{
                speed = MAX_SPEED_BACKWARD;
            }
        }

        /**
         * Update the speed when turning forward.
         */
        private static void _turnForward() {
            if(speed - SPEED_DEC_TURN_FORWARD > MIN_SPEED_FORWARD){
                speed -= SPEED_DEC_TURN_FORWARD;
            }else{
                speed = MIN_SPEED_FORWARD;
            }
        }

        /**
         * Update the speed when turning backward.
         */
        private static void _turnBackward() {
            if(speed - SPEED_DEC_TURN_BACKWARD > MIN_SPEED_BACKWARD){
                speed -= SPEED_DEC_TURN_BACKWARD;
            }else{
                speed = MIN_SPEED_BACKWARD;
            }
        }
    }
}
