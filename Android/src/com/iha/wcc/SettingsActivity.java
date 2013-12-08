package com.iha.wcc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity {

	//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
	}

	/*@Override
	protected void onStop() {
		// When we exit copy this into the real variables
		//pref.getInteger("speedAccelerationForward", "");
		
		super.onStop();
	}
	*/
	
	//This kind of fixes the problem of the video not continuing after going back
	//But it would send the view to the MainActivity not to CarActivity
		/*
		@Override
		public void onBackPressed() { 
		    
		    Intent intentSettings = new Intent(this, MainActivity.class);
	        startActivity(intentSettings);
	        finish();
		}
		*/	

} 
