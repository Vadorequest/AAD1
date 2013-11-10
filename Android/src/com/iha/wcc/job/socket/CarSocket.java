package com.iha.wcc.job.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class CarSocket {
	public static CarSocket instance;
	public static String host;
	public static int port = 5555;
	
	public static Send send;
	
	private Socket socket;
	private DataOutputStream dataOutputStream;
	
	static{
		new CarSocket();
	}
	
	public CarSocket(){
		instance = this;
		send = new Send();
	}

	public static CarSocket initialize(String host){
		CarSocket.host = host;
		
		return instance;
	}
	
	public static CarSocket initialize(String host, int port){
		CarSocket.host = host;
		CarSocket.port = port;
		
		return instance;
	}
	
	public static void execute(String... params){
		send.execute(params);
		Log.i("CarSocket::execute", params[0]);
	}
	
	private class Send extends AsyncTask<String, Void, Void>{

		protected Void doInBackground(String... params) {
			Log.i("Send::doInBackground", params[0]);
			
			try {
				Log.i("Opening a socket to", host+":"+port);
				socket = new Socket(host, port);
				Log.i("Socket state:", "Open.");
				
				// Can't be called in the MAIN thread: http://stackoverflow.com/questions/6343166/android-os-networkonmainthreadexception
				dataOutputStream = new DataOutputStream(socket.getOutputStream());
				
			} catch (UnknownHostException e) {
//				e.printStackTrace();
				Log.e("Socket state:", "Unable to start a socket connection." + e.getMessage());
			} catch (IOException e) {
//				e.printStackTrace();
				Log.e("Socket state:", "Unable to start a socket connection." + e.getMessage());
			}
			return null;			
		}
		
	}
}
