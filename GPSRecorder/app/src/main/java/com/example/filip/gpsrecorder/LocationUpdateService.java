package com.example.filip.gpsrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Filip on 2015-03-06.
 */
public class LocationUpdateService extends Service {

    public static boolean isRunning = false;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private SharedPreferences sharedpreferences;
    private Socket clientSocket;

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;

    private String deviceIP = null;

    private String macAddress = null;

    @Override
    public void onCreate() {


        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);

        thread.setDaemon(true);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "GPS Service started", Toast.LENGTH_SHORT).show();
        isRunning = true;

        sharedpreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

        new RequestConnection().execute();

        getDeviceIdentity();

        startLocationDiscovery();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        //Log.d("", "" + intent.getStringExtra("PARAM_1"));
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + 5*1000;
            int count = 0;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        //Log.d("", "running service" + count++);
                        //wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     *
     */
    @Override
    public void onDestroy() {

        Toast.makeText(this, "Broadcasting stopped", Toast.LENGTH_SHORT).show();
        isRunning = false;
        mServiceLooper.quit();
        locationManager.removeUpdates(locationListener);
        try {
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /*************************************************************************************
     * Function: getDeviceIdentity()
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	getDeviceIdentity()
     *
     * PARAMETERS:
     *          void
     *
     * RETURNS:	void
     *
     * NOTES:
     * gets and sets the ip and MAC addresses of the device to be sent along with location
     * info to the server.
     *************************************************************************************/
    private void getDeviceIdentity() {

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();

        int ipAddress = wifiInfo.getIpAddress();

        deviceIP = android.text.format.Formatter.formatIpAddress(ipAddress);

        macAddress = wifiInfo.getMacAddress();

        Log.d("DEVICE MAC ADDRESSS", macAddress);
        Log.d("DEVICE IP ADDRESS: ", deviceIP);
    }

    private void startLocationDiscovery() {

        int minTimeUpdate;
        int minDistUpdate;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        String useProvider;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            useProvider = LocationManager.NETWORK_PROVIDER;
        }
        else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                 locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            useProvider = LocationManager.GPS_PROVIDER;
        }
        else {

            useProvider = LocationManager.PASSIVE_PROVIDER;
        }

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.d("", "" + location.toString());

                String packet = buildPacket(location);

                new WriteSocket().execute(packet);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        Log.d("USING PROVIDER", useProvider);
        minTimeUpdate = sharedpreferences.getInt("MIN_TIME", 1000);
        minDistUpdate = sharedpreferences.getInt("MIN_DISTANCE", 5);
        Log.d("MIN TIME", ""+minTimeUpdate);
        Log.d("MIN DISTANCE", ""+minDistUpdate);
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(useProvider, minTimeUpdate, minDistUpdate, locationListener);
    }

    private String buildPacket(Location l) {

        if (l == null)
            return null;

        String s;

        Date d = new Date(l.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String sDate = sdf.format(d);

        s = l.getLongitude() + ", " + l.getLatitude() + ", " + deviceIP + ", " + sDate + ", " + macAddress + ", " + android.os.Build.MODEL;

        return s;
    }


    private class RequestConnection extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {

            String ip = sharedpreferences.getString("IP_ADDR", "lamckalex.ddns.net");
            int port = sharedpreferences.getInt("PORT", 7000);

            Log.d("server ip", ip);
            Log.d("server port", "" + port);

            try {
                clientSocket = new Socket(ip, port);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }

    private class WriteSocket extends AsyncTask<Object, Void, String> {


        @Override
        protected String doInBackground(Object... params) {

            OutputStream os;

            String send = (String)params[0];

            try {

                if (clientSocket != null) {

                    clientSocket.setSendBufferSize(1024);
                    os = clientSocket.getOutputStream();

                    Log.d("Sending ", send);

                    os.write(send.getBytes());
                    os.flush();
                }


            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }

    }



}
