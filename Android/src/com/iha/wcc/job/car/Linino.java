package com.iha.wcc.job.car;

import android.content.SharedPreferences;

/**
 * Linino is the embedded Linux OS on the Arduino YUN embedded on the car.
 */
public class Linino {

    /**
     * Linino default IP address for its hotspot.
     */
    public final static String DEFAULT_NETWORK_IP = "192.168.240.1";

    /**
     * Linino default port number for its hotspot.
     */
    public final static int DEFAULT_NETWORK_PORT = 5555;

    /**
     * Linino default user for SSH.
     */
    public final static String DEFAULT_SSH_USER = "root";

    /**
     * Linino default user password for SSH.
     */
    public final static String DEFAULT_SSH_PASSWORD = "20132013";

    public static String getNetworkIp(SharedPreferences prefs){
        return prefs.getString("ipNetwork", DEFAULT_NETWORK_IP);
    }

    public static int getNetworkPort(SharedPreferences prefs){
        return Integer.parseInt(prefs.getString("portNetwork", String.valueOf(DEFAULT_NETWORK_PORT)));
    }

    public static String getUserSsh(SharedPreferences prefs){
        return prefs.getString("userSsh", DEFAULT_SSH_USER);
    }

    public static String getPasswordSsh(SharedPreferences prefs){
        return prefs.getString("passwordSsh", DEFAULT_SSH_PASSWORD);
    }
}
