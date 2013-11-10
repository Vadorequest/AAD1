package com.iha.wcc.job.carCommunication;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class CarHttpRequest {
	public static String host;
	public static int port = 5555;
	
	static{
		new CarHttpRequest();
	}

	public static void initialize(String host){
		CarHttpRequest.host = host;
	}
	
	public static void initialize(String host, int port){
		CarHttpRequest.host = host;
		CarHttpRequest.port = port;
	}
	
	public static void execute(String uri){
		new CarHttpRequest.Send().execute(uri);
	}
	
	private static class Send  extends AsyncTask<String, String, String>{
	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	        	HttpGet URI = new HttpGet("http://"+host+":"+port+"/arduino/"+uri[0]);
	        	Log.i("Request sent", URI.getURI().getPath());
	            response = httpclient.execute(URI);
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                Log.i("Response", responseString);
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	        	e.printStackTrace();
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
	        return responseString;
	    }
	
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        Log.i("Response", result);
	    }
	}
}