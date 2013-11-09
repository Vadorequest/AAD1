package com.iha.wcc;

import com.iha.wcc.DeviceFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DevicesActivity extends FragmentActivity implements OnFragmentInteractionListener{

	private static Context context;
	
	private WifiManager wifiManager;
	
	// View elements.
	private ToggleButton wifiBtn;
	private Button refreshList;
	private DeviceFragment devices;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices);
		
		// Define a static context.
		context = getApplicationContext();
		
		// Initialize services instances.
		this.wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
		// Initialize view objects.
		this.wifiBtn = (ToggleButton) findViewById(R.id.wifiBtn);
		this.refreshList = (Button) findViewById(R.id.refreshList);
		this.devices = (DeviceFragment) getSupportFragmentManager().findFragmentById(R.id.devices);
				
		boolean wifiEnable = wifiManager.isWifiEnabled();
		if(!wifiEnable){
			// Display a message if WIFI is disabled.
			Toast.makeText(context, getNotificationWifi(wifiEnable), Toast.LENGTH_LONG).show();
		}
		
		// Set the value of the ToggleButton.
		this.wifiBtn.setChecked(wifiEnable);		

		// Bind listeners.
		this.bindListeners();
	}
	
	/**
	 * Bind all button listeners. (called during the initialization)
	 */
	private void bindListeners(){
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
		Log.i("Item clicked:", id);
		Toast.makeText(this, "Connection processing with the device #"+id, Toast.LENGTH_LONG).show();
	}
	
	private static String getNotificationWifi(boolean enabled){
		return enabled ? "WIFI starting, please refresh once you will be connected." : "Please, enable the WIFI to get a list of available devices!";
	}

}
