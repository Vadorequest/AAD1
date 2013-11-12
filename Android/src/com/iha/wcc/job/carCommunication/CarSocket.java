package com.iha.wcc.job.carCommunication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class CarSocket {
	public static String host;
	public static int port = 5555;
	
	static{
		new CarSocket();
	}

	public static void initialize(String host){
		CarSocket.host = host;
	}
	
	public static void initialize(String host, int port){
		CarSocket.host = host;
		CarSocket.port = port;
	}
	
	public static void execute(String params){
		
		new CarSocket.Send().execute(params);
	}
	
	private static class Send extends AsyncTask<String, Void, JSONObject>{
		private Socket socket;
		private DataOutputStream dataOutputStream;
		
		protected JSONObject doInBackground(String... params) {
			long startTime = System.nanoTime();
			Log.i("Send::doInBackground", params[0]);
			
			try {
				Log.i("Opening a socket to", host+":"+port);
				socket = new Socket(host, port);
				Log.i("Socket state:", "Open.");
				
				// Can't be called in the MAIN thread: http://stackoverflow.com/questions/6343166/android-os-networkonmainthreadexception
				dataOutputStream = new DataOutputStream(socket.getOutputStream());
				
			} catch (UnknownHostException e) {
//				e.printStackTrace();
				Log.e("UnknownHostException", "Unable to start a socket connection." + e.getMessage());
			} catch (IOException e) {
//				e.printStackTrace();
				Log.e("IOException", "Unable to start a socket connection." + e.getMessage());
			}
						
			// TODO return something useful;
			JSONObject response = new JSONObject();
			try {
				response.put("elapsedTime", System.nanoTime() - startTime);
				Log.i("Response", Long.toString(response.getLong("elapsedTime")));
			} catch (JSONException e) {
				//e.printStackTrace();
				Log.e("JSONException", e.getMessage());
			}
			
			return response;			
		}
		
	}
}
