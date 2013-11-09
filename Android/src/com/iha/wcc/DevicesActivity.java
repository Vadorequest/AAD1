package com.iha.wcc;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.iha.wcc.DeviceFragment.OnFragmentInteractionListener;

public class DevicesActivity extends FragmentActivity implements OnFragmentInteractionListener{

	private static Context context;
	
	private WifiManager wifiManager;
	
	// View components.
	private ToggleButton wifiBtn;
	private ImageButton refreshList;
	private DeviceFragment devices;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices);
		
		// Define a static context. Useful for the anonymous events.
		context = getApplicationContext();
		
		// Initialize services instances.
		this.initializeServices();
		
		// Initialize view components.
		this.initializeComponents();

		// Initialize WIFI and display alert if not enabled.
		this.initializeWifi();		

		// Bind listeners once all components are initialized.
		this.initializeListeners();
	}
	
	/**
	 * Initialize all services.
	 */
	private void initializeServices() {
		// WIFI service manager.
		this.wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * Initialize all view components.
	 */
	private void initializeComponents() {
		this.wifiBtn = (ToggleButton) findViewById(R.id.wifiBtn);
		this.refreshList = (ImageButton) findViewById(R.id.refreshList);
		this.devices = (DeviceFragment) getSupportFragmentManager().findFragmentById(R.id.devices);
	}
	
	/**
	 * Display a Toast if WIFI disabled.
	 * Set the initial value of the wifi toggle button.
	 */
	private void initializeWifi() {
		boolean wifiEnable = wifiManager.isWifiEnabled();
		if(!wifiEnable){
			// Display a message if WIFI is disabled.
			Toast.makeText(context, getNotificationWifi(wifiEnable), Toast.LENGTH_LONG).show();
		}
		
		// Set the value of the ToggleButton.
		this.wifiBtn.setChecked(wifiEnable);
	}

	/**
	 * Bind all button listeners. (called during the initialization)
	 */
	private void initializeListeners(){
		// On value change, enable or disable the WIFI.
		this.wifiBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	        @Override
	        public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
	        	wifiManager.setWifiEnabled(isChecked);
	        	Toast.makeText(context, getNotificationWifi(isChecked), Toast.LENGTH_LONG).show();
	        }
	    });
		
		// On click refresh the view with the current available devices.
		this.refreshList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				devices.refreshList();
				Toast.makeText(context, "Devices list refreshed.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.devices, menu);
		return true;
	}

	@Override
	public void onFragmentInteraction(String id) {
		// Load the Car activity.
		Intent intent = new Intent(DevicesActivity.this, CarActivity.class); 
		intent.putExtra("id", id);
		
        startActivity(intent);
        
        // Display a message to the user.
		Toast.makeText(this, "Connection processing with the device #"+id, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Return a string depending of the WIFI status.
	 * @param enabled
	 * @return String - Message to display.
	 */
	private static String getNotificationWifi(boolean enabled){
		return enabled ? "WIFI starting, please refresh once you will be connected." : "Please, enable the WIFI to get a list of available devices!";
	}

}
