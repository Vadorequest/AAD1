package com.iha.wcc.job.car;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Camera embedded on the car, controlled by the Linino embedded Linux OS.
 *
 * @see com.iha.wcc.job.car.Linino Linino Linux OS.
 */
public class Camera {

    /*
     ******************************************* CONSTANTS - Public *****************************************
     */
    /**
     * Linino default camera streaming address.
     */
    public final static String DEFAULT_CAMERA_STREAMING_URL = "http://"+Linino.DEFAULT_NETWORK_IP+":8080/?action=stream";

    /**
     * Linino default camera picture address.
     */
    public final static String DEFAULT_CAMERA_PICTURE_URL = "http://"+Linino.DEFAULT_NETWORK_IP+":8080/?action=snapshot";

    /**
     * Default width used for the camera stream.
     */
    public final static String DEFAULT_CAMERA_WIDTH = "1900";

    /**
     * Default height used for the camera stream.
     */
    public final static String DEFAULT_CAMERA_HEIGHT = "1200";

    /**
     * Default FPS used for the camera stream.
     */
    public final static String DEFAULT_CAMERA_FPS = "30";

    /**
     * Default path used to store photos.
     */
    public final static String DEFAULT_PATH_PHOTO = "/WCC";

    /**
     * Default filename extension.
     */
    public final static String DEFAULT_FILENAME_EXT = "jpg";

    /*
     ******************************************* Methods - Public *****************************************
     */

    /**
     * Return the camera width to use, use default value as default if settings is not defined.
     * @return Width.
     */
    public static String getCameraWidth(SharedPreferences prefs){
        return prefs.getString("cameraWidth", DEFAULT_CAMERA_WIDTH);
    }

    /**
     * Return the camera height to use, use default value as default if settings is not defined.
     * @return Height.
     * */
    public static String getCameraHeight(SharedPreferences prefs){
        return prefs.getString("cameraHeight", DEFAULT_CAMERA_HEIGHT);
    }

    /**
     * Return the camera FPS to use, use default value as default if settings is not defined.
     * @return Height.
     * */
    public static String getCameraFps(SharedPreferences prefs){
        return prefs.getString("cameraFps", DEFAULT_CAMERA_HEIGHT);
    }

    /**
     * Return the command to execute to start the camera video stream using default values.
     * @return Command to execute.
     */
    public static String getCommand(){
        return "mjpg_streamer -i \"input_uvc.so -d /dev/video0 -f "+DEFAULT_CAMERA_FPS+" -r "+DEFAULT_CAMERA_WIDTH+"*"+DEFAULT_CAMERA_HEIGHT+"\" -o \"output_http.so -p 8080 -w /mnt/share\"";
    }

    /**
     * Return the command to execute to start the camera video stream using values in SharedPreferences.
     * @return Command to execute.
     */
    public static String getCommand(SharedPreferences prefs){
        return "mjpg_streamer -i \"input_uvc.so -d /dev/video0 -f "+getCameraFps(prefs)+" -r "+getCameraWidth(prefs)+"*"+getCameraHeight(prefs)+"\" -o \"output_http.so -p 8080 -w /mnt/share\"";
    }

    /**
     * Return the path where store the photo depending on the user preferences.
     * @return Path where store the photo as File object.
     */
    public static File getPathPhoto(Context context, SharedPreferences prefs){
        // Check if a the user defined his own path to store picture and check is the path is not empty.
        if(isExternalStorageWritable()){
            return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + (prefs.getString("photoStorage", DEFAULT_PATH_PHOTO).length() > 0 ? prefs.getString("photoStorage", DEFAULT_PATH_PHOTO) : DEFAULT_PATH_PHOTO));
        }else{
            return context.getFilesDir();
        }
    }

    /**
     * Return the filename for a new photo.
     * @return Filename of the picture.
     */
    public static String getFilenamePhoto(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return formatter.format(new Date()) + "." + DEFAULT_FILENAME_EXT;
    }

    /**
     * Checks if external storage is available for read and write
     * @return True if an external storage is writable.
     */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
