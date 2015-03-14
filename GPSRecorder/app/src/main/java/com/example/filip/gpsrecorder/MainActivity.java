package com.example.filip.gpsrecorder;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;


/*****************************************************************************************************
 *	SOURCE FILE:	MainActivity.java	Main activity for our gps recorder mobile application
 *                                      contains all the UI elements. Allows user to start location
 *                                      allows user to select connection settings, location discovery mode
 *                                      and location update frequency. Also has a manual check-in mode.
 *
 *	PROGRAM:	GPS Recorder
 *
 *	FUNCTIONS:
 *		protected void onCreate(Bundle)
 *		private void getDeviceIdentity()
 *		public boolean onCreateOptionsMenu(Menu menu)
 *		public void onBtnClick(View v)
 *      public void startConfigFragment()
 *      public void startService()
 *      public void checkIn()
 *      private void requestLocationUpdate()
 *      private void updateBtnText(boolean serviceEnabled)
 *      public boolean onOptionsItemSelected(MenuItem item)
 *      private void reverse(byte[] array)
 *      private String buildPacket(Location l)
 *
 *  Inner Classes:
 *      private class RequestConnection
 *      private class WriteSocket
 *
 *	DATE: 		March 13, 2015
 *
 *
 *	DESIGNERS: 	Filip Gutica
 *
 *	PROGRAMMER: Filip Gutica
 *
 *	NOTES:
 *
 *********************************************************************************************************/
public class MainActivity extends Activity {

    private SharedPreferences sharedpreferences;
    private WebView webView;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Socket clientSocket;

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;

    private String deviceIP = null;
    private String macAddress = null;


    @Override
    /*************************************************************************************
     * Function: onCreate
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Autogenerated function
     *
     * PROGRAMMER:	Autogenerated function
     *              implemented by Filip Gutica
     *
     * INTERFACE:	onCreate(Bundle savedInstanceState)
     *
     * PARAMETERS:
     *          savedInstanceState      -   Any previouse save state of the application.
     *                                      if application is restarted, allows for it to
     *                                      continue from where the user left off.
     *
     * RETURNS:	void
     *
     * NOTES:
     * Main entry point of the activity. The first function taht is called when an activity
     * is instantiated.
     *************************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateBtnText(LocationUpdateService.isRunning);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        sharedpreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("IP_ADDR", "lamckalex.ddns.net");
        editor.putInt("PORT", 7000);
        editor.commit();

        webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);

        /**
         * Process the authentication request from the website
         */
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {

                handler.proceed("dcomm", "bcit");
            }
        });

        /**
         * Load our website such that the user can see the results of their actions.
         */
        webView.loadUrl("http://lamckalex.ddns.net/GPSAssign/");
    }

    @Override
    /*************************************************************************************
     * Function: onCreateOptionsMenu()
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Auto generated by Android studio
     *
     * PROGRAMMER:	Auto generated by Android studio
     *
     * INTERFACE:	onCreateOptionsMenu(Menu menu)
     *
     * PARAMETERS:
     *          menu        - Menu object to be inflated
     *
     * RETURNS:	void
     *
     * NOTES:
     * Called when the user clicks on the menu option on the top right hand corner of the
     * screen. Inflates the options menu which contains one element at the moment: Settings
     *************************************************************************************/
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    /*************************************************************************************
     * Function: onOptionsItemSelected
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Auto generated by Android studio
     *
     * PROGRAMMER:	Auto generated by Android studio
     *
     * INTERFACE:	 onOptionsItemSelected(MenuItem item)
     *
     * PARAMETERS:
     *          item        - menu item toggled by the user
     *
     * RETURNS:	void
     *
     * NOTES:
     * Called when user selects a menu item from the menu. Verifies which item was selected.
     * In this case we only have one item: settings which when clicked starts the
     * LOCATION_SOURCE_SETTINGS activity where the user can set the location discovery preferences
     * for the device.
     *************************************************************************************/
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent (Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    /*************************************************************************************
     * Function: onBtnClick
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	onBtnClick(View v)
     *
     * PARAMETERS:
     *          v       - "View" which was clicked. (Buttons are views)
     *
     * RETURNS:	void
     *
     * NOTES:
     * Button listener. Checks which button was clicked then performs the appropriate task
     *************************************************************************************/
    public void onBtnClick(View v) {

        switch (v.getId()) {

            case R.id.btnCheckIn:       //Check in button
                checkIn();
                break;

            case R.id.btnConfig:        //Config button
                startConfigFragment();
                break;

            case R.id.btnService:       //Start service button
                startService();
                break;
        }
    }

    /*************************************************************************************
     * Function: startConfigFragment
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	startConfigFragment()
     *
     * PARAMETERS:
     *          void
     *
     * RETURNS:	void
     *
     * NOTES:
     * Starts the config dialog fragment where users can set the host/ip of the server
     * they want to connect to, the port and also the frequency of location updates for the
     * automatic background location discovery service.
     *************************************************************************************/
    private void startConfigFragment() {

        FragmentManager fm = getFragmentManager();

        ConfigFragment config = new ConfigFragment();

        config.show(fm, "dialog");

    }


    /*************************************************************************************
     * Function: startService
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	void startService()
     *
     * PARAMETERS:
     *          void
     *
     * RETURNS:	void
     *
     * NOTES:
     * Starts the background service for location discovery.
     *************************************************************************************/
    private void startService() {
        Intent intent = new Intent(this, LocationUpdateService.class);
        //Bundle bundle = new Bundle();

       // intent.putExtras(bundle);

        updateBtnText(!LocationUpdateService.isRunning);

        if (LocationUpdateService.isRunning) {
            stopService(intent);
        } else {
            Log.d("", "Starting service");
            startService(intent);
        }

    }

    /*************************************************************************************
     * Function: checkIn
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	void checkIn()
     *
     * PARAMETERS:
     *          void
     *
     * RETURNS:	void
     *
     * NOTES:
     * Requests a connection with the TCP server and requests and sends one location packet
     * after which it terminates the connection.
     *************************************************************************************/
    private void checkIn() {

        getDeviceIdentity();

        new RequestConnection().execute();

        requestLocationUpdate();
    }

    /*************************************************************************************
     * Function: requestLocationUpdate
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	void requestLocationUpdate()
     *
     * PARAMETERS:
     *          void
     *
     * RETURNS:	void
     *
     * NOTES:
     * Request a single location update from the android Location service, when a location
     * update occurs the onLocationChanged callback is called where we build a packet
     * and write it to the socket.
     *************************************************************************************/
    private void requestLocationUpdate() {

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

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }


        };

        Log.d("USING PROVIDER", useProvider);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestSingleUpdate(useProvider, locationListener, null);
    }


    /*************************************************************************************
     * Function: updateBtnText
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	private void updateBtnText(boolean serviceEnabled)
     *
     * PARAMETERS:
     *          serviceEnabled      - is the service running
     *
     * RETURNS:	void
     *
     * NOTES:
     * if the service is enabled sets the button text to "stop service" if the service
     * is not enabled sets the button text to "start service"
     *************************************************************************************/
    private void updateBtnText(boolean serviceEnabled) {
        Button btn = (Button)findViewById(R.id.btnService);

        if (serviceEnabled)     btn.setText("Stop Service");
        else                    btn.setText("Start Service");
    }


    /*************************************************************************************
     * Function: buildPacket
     *
     * DATE: March 13, 2015
     *
     * DESIGNER:	Filip Gutica
     *
     * PROGRAMMER:	Filip Gutica
     *
     * INTERFACE:	buildPacket(Location l)
     *
     * PARAMETERS:
     *          l     - location object
     *
     * RETURNS:	void
     *
     * NOTES:
     * builds a packet containing a longitude, latitude, ip address, date and macAddress
     * delimited by commas.
     *************************************************************************************/
    private String buildPacket(Location l) {

        if (l == null)
            return null;

        String s;

        Date d = new Date(l.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String sDate = sdf.format(d);

        s = l.getLongitude() + ", " + l.getLatitude() + ", " + deviceIP + ", " + sDate + ", " + macAddress;

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

                    clientSocket.close();
                }


            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }

    }




}
