package com.iha.wcc;


import com.iha.wcc.fragment.network.INetworkFragmentInteractionListener;
import com.iha.wcc.fragment.network.NetworkFragment;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends FragmentActivity implements INetworkFragmentInteractionListener{

	/*
	 * Static instance of itself.
	 */
	public static Context context;
	
	// Services.
	private WifiManager wifiManager;
	
	// View components.
	private ToggleButton wifiBtn;
	private ImageButton refreshList;
	private NetworkFragment networks;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
		this.networks = (NetworkFragment) getSupportFragmentManager().findFragmentById(R.id.devices);
	}
	
	/**
	 * Set the initial value of the wifi toggle button.
	 */
	private void initializeWifi() {
		// Set the value of the ToggleButton.
		this.wifiBtn.setChecked(this.wifiManager.isWifiEnabled());
	}

	/**
	 * Bind all button listeners. (called during the initialization)
	 */
	private void initializeListeners(){
		// On value change, enable or disable the WIFI.
		this.wifiBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	        @Override
	        public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
	        	doToggleWifi(isChecked);
	        }
	    });
		
		// On click refresh the view with the current available devices and the network's label.
		this.refreshList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doRefreshList();
			}
		});
	}

    @Override
    protected void onResume(){
        super.onResume();
        this.doRefreshList();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onNetworkFragmentInteraction(String name, String ip) {
		// Load the Car activity.
		Intent intent = new Intent(MainActivity.this, CarActivity.class); 
		intent.putExtra("name", name);
		intent.putExtra("ip", ip);
		
        startActivity(intent);
	}
	
	/**
	 * Return a string depending of the WIFI status.
	 * @param enabled
	 * @return String - Message to display.
	 */
	public static String getNotificationWifi(boolean enabled){
		return enabled ? "WIFI starting, please refresh once you will be connected." : "Please, enable the WIFI to connect to the car!";
	}

	/**
	 * Refresh the list.
     * Display warnings only if wifi disabled.
	 */
	private void doRefreshList() {
        this.doRefreshList(!this.wifiBtn.isChecked());
	}

    /**
     * Refresh the list.
     * @param displayWarnings If true, display warnings.
     */
    private void doRefreshList(boolean displayWarnings) {
        networks.refreshList(displayWarnings);
    }

	/**
	 * Enable or disable the wifi.
	 * @param isChecked
	 */
	private void doToggleWifi(boolean isChecked) {
		wifiManager.setWifiEnabled(isChecked);
		
		// Display message.
    	Toast.makeText(MainActivity.context, MainActivity.getNotificationWifi(isChecked), Toast.LENGTH_LONG).show();
    	
    	// Auto refresh the list.
    	doRefreshList();
	}
}
