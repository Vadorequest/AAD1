package com.iha.wcc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.iha.wcc.job.carCommunication.CarSocket;
import com.iha.wcc.job.carCommunication.CarHttpRequest;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CarActivity extends Activity {
	/*
	 * Static instance of itself.
	 */
	private static Context context;
	
	// View components.
	private ImageView cameraContent;
	private ImageButton pictureBtn;
	private ImageButton honkBtn;
	private ImageButton settingsBtn;
	private ImageButton goForwardBtn;
	private ImageButton goBackwardBtn;
	private ImageButton goLeftBtn;
	private ImageButton goRightBtn;
	
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
	}
	
	/**
	 * Bind all button listeners. (called during the initialization)
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
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
		
		// On touch, go forward.
		this.goForwardBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goForward();
				return false;
			}
		});
		
		// On touch, go backward.
		this.goBackwardBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goBackward();
				return false;
			}
		});
		
		// On touch, go to the left.
		this.goLeftBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goLeft();
				return false;
			}
		});
		
		// On touch, go to the right.
		this.goRightBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				goRight();
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
	 * @param ip IP address of the car.
	 * @param port Port used to communicate with the device.
	 */
	private void initializeCar(String name, String ip){
		// Initialize the CarSocket and CarHttpRequest classes with available information about the host to connect.
		CarSocket.initialize(ip);// Use socket.
		CarHttpRequest.initialize(ip);// Use HTTP web request, slower.	
	}
	
	/**
	 * Send a request to the car to go forward.
	 */
	private void goForward(){
		CarHttpRequest.execute("digital/13/1");
//		CarSocket.execute("forward");
	}
	
	/**
	 * Send a request to the car to go backward.
	 */
	private void goBackward(){
		CarHttpRequest.execute("digital/13/0");
//		CarSocket.execute("back");
	}
	
	/**
	 * Send a request to the car to go to the left.
	 */
	private void goLeft(){
//		CarSocket.execute("left");
	}
	
	/**
	 * Send a request to the car to go to the right.
	 */
	private void goRight(){
//		CarSocket.execute("right");
	}
	
	/**
	 * Send a request to the car to go backward.
	 */
	private void doPhoto(){
		Toast.makeText(context, "Photo taken! Stored on the internal car SD card.", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Send a request to the car to go backward.
	 */
	private void doHonk(){
		Toast.makeText(context, "Move your ass! Asshole!", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Display settings.
	 * TODO We don't know yet how it will works, another page? Could be better to have all the stuff on the same page but could be difficult...
	 */
	private void displaySettings(){
		Toast.makeText(context, "I don't know really how did that in only one screen guys! We should discuss about :)", Toast.LENGTH_SHORT).show();
	}

}
