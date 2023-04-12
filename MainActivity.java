package com.techpacs.data_monitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.techpacs.data_monitoring.adapters.ChannelsAdapter;
import com.techpacs.data_monitoring.models.ProfileModel;
import com.techpacs.data_monitoring.retorfitClientAndService.RetrofitClient;
import com.techpacs.data_monitoring.retorfitClientAndService.WebServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String api_key;

    EditText etAPIKey;
    EditText etServerName;
    Button btnSubmit;

    ChannelsAdapter channelsAdapter;
    ListView lv_channels;
    String serverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        find_ID();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                my_fun();
            }
        });
        lv_channels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProfileModel profileModel = channelsAdapter.getModel(i);

                String read_key = "", write_key = "";
                if (profileModel.getApi_keys().get(0).getWrite_flag()) {
                    write_key = profileModel.getApi_keys().get(0).getApi_key();
                } else {
                    read_key = profileModel.getApi_keys().get(0).getApi_key();
                }
                if (profileModel.getApi_keys().get(1).getWrite_flag()) {
                    write_key = profileModel.getApi_keys().get(1).getApi_key();
                } else {
                    read_key = profileModel.getApi_keys().get(1).getApi_key();
                }
                Bundle bundle = new Bundle();
                bundle.putString("read_key", read_key);
                bundle.putString("write_key", write_key);
                bundle.putString("server_name", serverName);
                bundle.putString("id", profileModel.getId());
                bundle.putString("name", profileModel.getName());
                Intent intent = new Intent(MainActivity.this, ChannelActivity.class).putExtra("bundle", bundle);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


            }
        });
    }


    private void my_fun() {
        serverName = etServerName.getText().toString();
        api_key = etAPIKey.getText().toString();
        final WebServices mService = RetrofitClient.getClient("https://" + serverName).create(WebServices.class);
        mService.get_profile_data(api_key).enqueue(new Callback<List<ProfileModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProfileModel>> call, @NonNull Response<List<ProfileModel>> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    channelsAdapter = new ChannelsAdapter((ArrayList<ProfileModel>) response.body());
                    lv_channels.setAdapter(channelsAdapter);
                }else {
                    Toast.makeText(MainActivity.this, "Invalid User API Key", Toast.LENGTH_SHORT).show();
                }
                
            }

            @Override
            public void onFailure(@NonNull Call<List<ProfileModel>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Invalid User API Key", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void find_ID() {
        etAPIKey = findViewById(R.id.etAPIKey);
        etServerName = findViewById(R.id.etServerName);
        btnSubmit = findViewById(R.id.btn_submit);
        lv_channels = findViewById(R.id.lv_channels);

    }
}