package com.iha.wcc.job.carCommunication;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.widget.Toast;
import com.iha.wcc.exception.MessageException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class CarHttpRequest {
    public static Context context;
	public static String host;
	public static int port = 5555;
	
	static{
		new CarHttpRequest();
	}

	public static void initialize(Context context, String host){
        CarHttpRequest.context = context;
		CarHttpRequest.host = host;
	}
	
	public static void initialize(Context context, String host, int port){
        CarHttpRequest.context = context;
		CarHttpRequest.host = host;
		CarHttpRequest.port = port;
	}
	
	public static void execute(String uri){
		new CarHttpRequest.Send().execute(uri);
	}
	
	private static class Send  extends AsyncTask<String, String, String>{
        private String errorToDisplayOnPostExecute = null;

	    @Override
	    protected String doInBackground(String... uri){
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = "";
	        try {
                String URL = "http://"+host+/*":"+port+*/"/arduino/"+uri[0];
	        	HttpGet URI = new HttpGet(URL);
	        	Log.i("Request sent", URL);
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
            } catch(HttpHostConnectException e){
                Log.e("HttpHostConnectException", e.getMessage());
                this.errorToDisplayOnPostExecute =  e.getMessage() + "\nPlease check you are connected to the Arduino hotspot.";
	        } catch (ClientProtocolException e) {
                Log.e("ClientProtocolException", e.getMessage());
                this.errorToDisplayOnPostExecute = e.getMessage();
	        } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                this.errorToDisplayOnPostExecute = e.getMessage();
	        }
	        return responseString;
	    }
	
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);

            // Display error if exists.
            if(this.errorToDisplayOnPostExecute != null){
                Toast.makeText(context, this.errorToDisplayOnPostExecute, Toast.LENGTH_LONG).show();
            }
	        Log.i("Response", result);
	    }
	}
}