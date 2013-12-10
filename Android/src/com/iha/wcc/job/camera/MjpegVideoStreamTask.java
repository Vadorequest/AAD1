package com.iha.wcc.job.camera;

import java.io.IOException;
import java.net.URI;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * In charge of start the streaming from an URL.
 * Load the stream to an URL and set it to a MjpegView.
 */
public class MjpegVideoStreamTask extends AsyncTask<String, Void, MjpegInputStream> {
    public String TAG = "DoRead";

    /**
     * The view to update with the loaded stream.
     */
    private MjpegView camera;

    /**
     * Constructor to define a custom view.
     * @param camera View that will be updated once the HTTP request will be done.
     */
    public MjpegVideoStreamTask(MjpegView camera){
        this.camera = camera;
    }

    /**
     * Call the target using HTTP request and return the content to the onPostExecute method.
     * @param url Url to reach.
     * @return MjpegInputStream if success.
     */
    @Override
    protected MjpegInputStream doInBackground(String... url) {
        HttpResponse res;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        Log.d(TAG, "1. Sending http request");
        try {
            res = httpclient.execute(new HttpGet(URI.create(url[0])));
            Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
            if(res.getStatusLine().getStatusCode()==401){
                //You must turn off camera User Access Control before this will work
                return null;
            }
            return new MjpegInputStream(res.getEntity().getContent());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.d(TAG, "Request failed-ClientProtocolException", e);
            //Error connecting to camera
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Request failed-IOException", e);
            //Error connecting to camera
        }

        return null;
    }

    /**
     * Executed once the doInBackground method is done.
     * Update the camera with the new stream.
     * @param result Content of the webpage called.
     */
    @Override
    protected void onPostExecute(MjpegInputStream result) {
        if(result != null){
            camera.setSource(result);
            camera.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            camera.showFps(true);
        }else{
            Log.d(TAG, "The URL called doesn't have stream content.");
        }
    }
}
