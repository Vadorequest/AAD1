package com.iha.wcc.job.ssh;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import com.iha.wcc.CarActivity;
import com.iha.wcc.job.camera.MjpegVideoStreamTask;
import com.iha.wcc.job.camera.MjpegView;
import com.iha.wcc.job.car.Camera;
import com.jcraft.jsch.*;
import java.util.Properties;

/**
 * Run a SSH command using Jsch library.
 * http://www.jcraft.com/jsch/
 */
public class SshTask extends AsyncTask<String, Integer, Boolean> {
    private static Session session;
    private static Channel channel;

    private Context context;
    private String host;
    private String user;
    private String password;
    private String command;

    private MjpegView cameraContent;

    /**
     * Constructor that load the necessary information to connect using SSG protocol.
     * @param cameraContent
     * @param context       Activity context, useful to display message.
     * @param host          Host IP to reach.
     * @param user          SSH user name.
     * @param password      SSH user password.
     * @param command       SSH command to execute.
     */
    public SshTask(MjpegView cameraContent, Context context, String host, String user, String password, String command){
        this.cameraContent = cameraContent;
        this.context = context;
        this.host = host;
        this.user = user;
        this.password = password;
        this.command = command;
    }

    /**
     * Connect to the host using SSH.
     * @param arg0 Contains nothing.
     * @return True if the connection is a success.
     */
    @Override
    protected Boolean doInBackground(String... arg0) {
        JSch jsch=new JSch();

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        Session session;
        try {
            session = jsch.getSession(user, host, 22);
            session.setConfig(config);
            session.setPassword(password);
            session.connect();

            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            SshTask.session = session;
            SshTask.channel = channel;

            return true;
        } catch (JSchException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Display a message to the user depending of the SSH command success.
     * @param success True if the connection is a success.
     */
    @Override
    protected void onPostExecute(Boolean success){
        if(success){
            Toast.makeText(this.context, "Video stream successfully started.", Toast.LENGTH_SHORT).show();
            CarActivity.videoStreamStarted = true;
        }else{
            Toast.makeText(this.context, "Unable to start the camera video stream. Are you connected to the Arduino Wi-Fi network?", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Shutdown the camera video stream.
     */
    public static void disconnect(){
        if(SshTask.session != null){
            SshTask.channel.disconnect();
            SshTask.session.disconnect();
        }
    }

}
