package com.iha.wcc.job.car;

import android.content.SharedPreferences;

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
}
