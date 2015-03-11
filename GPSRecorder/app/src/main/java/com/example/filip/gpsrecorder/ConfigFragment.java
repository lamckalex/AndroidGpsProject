package com.example.filip.gpsrecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Filip on 2015-03-09.
 */
public class ConfigFragment extends DialogFragment{

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    EditText ipTxt;
    EditText portTxt;
    EditText minTimeTxt;
    EditText minDistanceTxt;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View layout = inflater.inflate(R.layout.server_config, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(layout)
                // Add action buttons
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        sharedpreferences = layout.getContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                        ipTxt = (EditText) layout.findViewById(R.id.ip);
                        portTxt = (EditText) layout.findViewById(R.id.port);
                        minTimeTxt = (EditText) layout.findViewById(R.id.minTimeTxt);
                        minDistanceTxt = (EditText) layout.findViewById(R.id.minDistanceTxt);

                        String ip, port, time, dist;
                        int portNum = 7000;
                        int minTime = 1000;
                        int minDist = 5;

                        ip = ipTxt.getText().toString();
                        port = portTxt.getText().toString();
                        time = minTimeTxt.getText().toString();
                        dist = minDistanceTxt.getText().toString();

                        try {
                            portNum = Integer.parseInt(port);
                            minTime = Integer.parseInt(time);
                            minDist = Integer.parseInt(dist);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        editor = sharedpreferences.edit();

                        editor.putString("IP_ADDR", ip);
                        editor.putInt("PORT", portNum);
                        editor.putInt("MIN_TIME", minTime);
                        editor.putInt("MIN_DISTANCE", minDist);

                        editor.commit();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ConfigFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
