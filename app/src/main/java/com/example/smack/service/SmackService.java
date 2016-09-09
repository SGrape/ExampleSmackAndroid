package com.example.smack.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
//import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

/**
 * Created by dmolloy on 9/7/16.
 */
public class SmackService extends Service {


    public static final String NEW_MESSAGE = "com.example.smack.newmessage";
    public static final String SEND_MESSAGE = "com.example.smack.sendmessage";
    public static final String NEW_ROSTER = "com.example.smack.newroster";

    public static final String BUNDLE_FROM_JID = "b_from";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_ROSTER = "b_body";
    public static final String BUNDLE_TO = "b_to";

    public static SmackConnection.ConnectionState sConnectionState;

    public static SmackConnection.ConnectionState getState() {
        if(sConnectionState == null){
            return SmackConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    private boolean mActive;
    private Thread mThread;
    private Handler mTHandler;
    private SmackConnection mConnection;

    public SmackService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
       // Log.d("SmackService","start");
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    private void start() {
        if (!mActive) {
            mActive = true;

            // Create ConnectionThread Loop
            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mTHandler = new Handler();
                        //Log.d("Service","initConnection");
                        initConnection();
                        Looper.loop();
                    }

                });
                mThread.start();
            }

        }
    }

    private void stop() {
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mConnection != null){
                    mConnection.disconnect();
                }
            }
        });
    }

    private void initConnection() {
        if(mConnection == null){
           // Log.d("service","starting connection");
            mConnection = new SmackConnection(this);
        }
        try {
            //Log.d("service","calling connect");
            mConnection.connect();
        } catch (IOException | SmackException | XMPPException e) {
            e.printStackTrace();
        }
    }
}

