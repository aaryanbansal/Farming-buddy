package com.techpacs.data_monitoring;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.techpacs.data_monitoring.adapters.FieldsAdapter;
import com.techpacs.data_monitoring.models.ChannelsModel;
import com.techpacs.data_monitoring.models.FeedsModel;
import com.techpacs.data_monitoring.models.FieldsModel;
import com.techpacs.data_monitoring.retorfitClientAndService.RetrofitClient;
import com.techpacs.data_monitoring.retorfitClientAndService.WebServices;


import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FieldActivity extends AppCompatActivity {
    ArrayList<FeedsModel> feedsModel;
    ArrayList<FieldsModel> fieldsModel;

    private TextView tv_field_name;
    private ProgressBar progressBar;
    private TextView progressText;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    int i = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field);
        get_data();
        find_id();
        set_graph();
        set_progress_bar();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound);

    }


    private void set_progress_bar() {
        // set the id for the progressbar and progress text
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String val = feedsModel.get(feedsModel.size() - 1).getField(fieldId);
                double value = 0;
                if (val != null)
                    value = Double.parseDouble(val);
                progressText.setText(String.valueOf(value));
                i = (int) value;
                if (i <= 100) {
                    progressText.setText(String.valueOf(i));
                    progressBar.setProgress(i);
                    handler.postDelayed(this, 200);
                } else {
                    progressBar.setProgress(i);
                    progressText.setText(String.valueOf(i));
                    handler.removeCallbacks(this);
                }
            }
        }, 200);
    }

    private void find_id() {
        graphView = findViewById(R.id.idGraphView);
        tv_field_name = findViewById(R.id.tv_field_name);
        tv_field_name.setText(fieldName);
    }

    String fieldId;
    String fieldName;
    ChannelsModel channelsModel;

    private void get_data() {
        Intent intent = getIntent();
        Gson gson = new Gson();
        channelsModel = gson.fromJson(intent.getStringExtra("channelsModel"), ChannelsModel.class);
        fieldId = intent.getStringExtra("fieldId");
        fieldName = intent.getStringExtra("fieldName");
        fieldsModel = channelsModel.getChannelModel().getFields();
        feedsModel = channelsModel.getFeedsModel();

    }

    private void triggerVibrationAndBeep() {
        // Check for the VIBRATE permission in the manifest file before starting vibration
        vibrator.vibrate(500); // Vibrate for 500 milliseconds
        mediaPlayer.start(); // Play the beep sound
    }
    String val;

    private void getdata(){
        FeedsModel FDM = channelsModel.getFeedsModel().get(0);
        val=FDM.getField("field1");

        if (val.equals("0")){
            Toast.makeText(getApplicationContext(), "Fire Occured" , Toast.LENGTH_LONG).show();
            triggerVibrationAndBeep();
        }
    }

    GraphView graphView;

    private void set_graph() {
        DataPoint[] dataPoints = new DataPoint[feedsModel.size()];
        for (int j = 0; j < feedsModel.size(); j++) {
            String val = feedsModel.get(j).getField(fieldId);
            double value = 0;
            if (val != null && !val.equals("nan"))
                value = Double.parseDouble(val);
            dataPoints[j] = new DataPoint(
                    Double.parseDouble(feedsModel.get(j).getEntry_id()), value);

        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);

        graphView.setTitle(fieldName);
        graphView.setTitleColor(R.color.purple_200);
        graphView.setTitleTextSize(25);
        graphView.addSeries(series);
    }

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;


    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                getdata();
            }
        }, delay);
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }


}
