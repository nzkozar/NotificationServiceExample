package com.ak93.serviceexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startIntentButton = (Button)findViewById(R.id.btn_startIntentService);
        startIntentButton.setOnClickListener(this);
        Button startButton = (Button)findViewById(R.id.btn_startService);
        startButton.setOnClickListener(this);
        Button stopButton = (Button)findViewById(R.id.btn_stopService);
        stopButton.setOnClickListener(this);
    }

    void startNotificationIntentService(){
        Intent serviceIntent = new Intent(this,NotificationIntentService.class);
        serviceIntent.putExtra("message","NotificationIntentService!");
        startService(serviceIntent);
    }

    void startNotificationService(){
        Intent serviceIntent = new Intent(this,NotificationService.class);
        serviceIntent.putExtra("message","NotificationService!");
        startService(serviceIntent);
    }

    void stopNotificationService(){
        Intent serviceIntent = new Intent(this,NotificationService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btn_startIntentService:
                startNotificationIntentService();
                break;
            case R.id.btn_startService:
                startNotificationService();
                break;
            case R.id.btn_stopService:
                stopNotificationService();
                break;
        }
    }
}
