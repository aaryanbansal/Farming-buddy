package com.techpacs.data_monitoring;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
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

public class ChannelActivity extends AppCompatActivity {
    ArrayList<FeedsModel> feedsModel;
    ArrayList<FieldsModel> fieldsModel;

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        initializations();
        find_id();
        fun();
        lv_fields.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ChannelActivity.this, FieldActivity.class);
                Gson gson = new Gson();
                intent.putExtra("channelsModel", gson.toJson(channelsModel));
                intent.putExtra("fieldId", channelsModel.getChannelModel().getFields().get(i).getField_id());
                intent.putExtra("fieldName", channelsModel.getChannelModel().getFields().get(i).getField_name());
                startActivity(intent);
            }
        });
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound);


    }

    TextView tv_channel_name;
    ListView lv_fields;

    private void find_id() {
        tv_channel_name = findViewById(R.id.tv_channel_name);
        lv_fields = findViewById(R.id.lv_fields);
        tv_channel_name.setText(name);
    }

    private void initializations() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        read_key = bundle.getString("read_key", "");
        write_key = bundle.getString("write_key", "");
        server_name = bundle.getString("server_name", "");
        id = bundle.getString("id", "");
        name = bundle.getString("name", "");
    }

    private void triggerVibrationAndBeep() {
        // Check for the VIBRATE permission in the manifest file before starting vibration
        vibrator.vibrate(500); // Vibrate for 500 milliseconds
        mediaPlayer.start(); // Play the beep sound
    }

    String id, read_key, write_key, server_name, name;
    FieldsAdapter fieldsAdapter;
    ChannelsModel channelsModel;

    String fieldId;
    String val;

    private void getdata(){
        FeedsModel FDM = channelsModel.getFeedsModel().get(0);
        val=FDM.getField("field1");
      //  Toast.makeText(getApplicationContext(), val , Toast.LENGTH_LONG).show();

        if (val.equals("0")){
            Toast.makeText(getApplicationContext(), "Fire Occured" , Toast.LENGTH_LONG).show();
            triggerVibrationAndBeep();
        }
    }


    private void fun() {
        final WebServices mService = RetrofitClient.getClient("https://" + server_name).create(WebServices.class);
        mService.get_channels_data(id, read_key, 10).enqueue(new Callback<ChannelsModel>() {
            @Override
            public void onResponse(Call<ChannelsModel> call, Response<ChannelsModel> response) {
                assert response.body() != null;
                channelsModel = response.body();

                fieldsAdapter = new FieldsAdapter(
                        response.body().getChannelModel().getFields(),
                       response.body().getFeedsModel());

                lv_fields.setAdapter(fieldsAdapter);
        }

            @Override
            public void onFailure(@NonNull Call<ChannelsModel> call, @NonNull Throwable t) {
                Log.i("---------------------", t.toString());

            }
        });
    }


    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;


    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                fun();
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
