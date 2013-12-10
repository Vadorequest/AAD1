package com.iha.wcc.job.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Take a picture from the camera and store it.
 */
public class TakePictureTask extends AsyncTask<String, Void, Bitmap> {
    /**
     * Context of the current activity.
     */
    private Context context;

    /**
     * Default filename extension.
     */
    private String FILENAME_EXT = "jpg";

    /**
     * Constructor with setter for the activity context to use.
     * @param context
     */
    public TakePictureTask(Context context){
        this.context = context;
    }

    /**
     * Download the picture from a website.
     * @param param URL to reach.
     * @return The picture.
     */
    @Override
    protected Bitmap doInBackground(String... param) {
        return downloadBitmap(param[0]);
    }

    /**
     * Create the name of the file to save and save it.
     * Display a message to the user.
     */
    @Override
    protected void onPostExecute(Bitmap result) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date now = new Date();
        String filename = formatter.format(now) + "." + this.FILENAME_EXT;

        // Store the picture.
        if(this.storeImage(result, "/" + filename)){
            Toast.makeText(this.context, "Photo saved! ("+filename+")", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this.context, "The photo couldn't be saved!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Download the picture from the URL.
     * @param url Url where download the picture.
     * @return The picture.
     */
    private Bitmap downloadBitmap(String url) {
        // initialize the default HTTP client object
        final DefaultHttpClient client = new DefaultHttpClient();

        //forming a HttpGet request
        final HttpGet getRequest = new HttpGet(url);
        try {

            HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    // getting contents from the stream
                    inputStream = entity.getContent();

                    // decoding stream data back into image Bitmap that android understands
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    return bitmap;

                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            getRequest.abort();
            Log.e("ImageDownloader", "Something went wrong while" +
                    " retrieving bitmap from " + url + e.toString());
        }
        return null;
    }

    /**
     * Save the downloaded picture in the Android phone.
     * @param imageData The image to save.
     * @param filename The name of the image to save.
     * @return Return true if the image was correctly saved.
     */
    private boolean storeImage(Bitmap imageData, String filename) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        String default_path = "/WIFICar";
        String iconsStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + prefs.getString("photo_storage", default_path);
        File sdIconStorageDir = new File(iconsStoragePath);

        //Create storage directories, if nonexistent
        sdIconStorageDir.mkdirs();

        try {
            String filePath = sdIconStorageDir.toString() + filename;
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            imageData.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;

        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }

        return true;
    }
}
