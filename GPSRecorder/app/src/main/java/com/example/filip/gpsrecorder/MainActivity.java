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


public class MainActivity extends Activity {

    private SharedPreferences sharedpreferences;
    private WebView webView;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Socket clientSocket;

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;

    private InetAddress deviceIP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        sharedpreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("IP_ADDR", "lamckalex.ddns.net");
        editor.putInt("PORT", 7000);
        editor.commit();

        webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://lamckalex.ddns.net");


        getDeviceIP();
    }

    private void getDeviceIP() {

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();

        int ipAddress = wifiInfo.getIpAddress();
        byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();

        reverse(bytes);

        try {
            deviceIP = InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Log.d("DEVICE IP ADDRESS: ", ""+ deviceIP);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onBtnClick(View v) {

        switch (v.getId()) {

            case R.id.btnCheckIn:
                checkIn();
                break;
            case R.id.btnConfig:
                startConfigFragment();
                break;
            case R.id.btnService:
                startService();
                break;
        }
    }

    public void startConfigFragment() {

        FragmentManager fm = getFragmentManager();

        ConfigFragment config = new ConfigFragment();

        config.show(fm, "dialog");

    }

    public void startService() {
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

    public void checkIn() {

        new RequestConnection().execute();

        requestLocationUpdate();

    }

    private void requestLocationUpdate() {

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
        minTimeUpdate = sharedpreferences.getInt("MIN_TIME", 1000);
        minDistUpdate = sharedpreferences.getInt("MIN_DISTANCE", 5);
        Log.d("MIN TIME", ""+minTimeUpdate);
        Log.d("MIN DISTANCE", ""+minDistUpdate);
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestSingleUpdate(useProvider, locationListener, null);
    }


    private void updateBtnText(boolean serviceEnabled) {
        Button btn = (Button)findViewById(R.id.btnService);
        if (serviceEnabled)
            btn.setText("Stop Service");
        else
            btn.setText("Start Service");
    }


    @Override
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



    /**
     *
     * @param array
     */
    private void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private String buildPacket(Location l) {

        if (l == null)
            return null;

        String s;

        Date d = new Date(l.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String sDate = sdf.format(d);

        s = l.getLongitude() + ", " + l.getLatitude() + ", " + deviceIP + ", " + sDate;

        return s;
    }
}
