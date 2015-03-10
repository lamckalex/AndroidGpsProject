package com.example.filip.gpsrecorder;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("IP_ADDR", "192.168.1.72");
        editor.putInt("PORT", 7000);
        editor.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onBtnClick(View v) {

        switch (v.getId())
        {
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
}
