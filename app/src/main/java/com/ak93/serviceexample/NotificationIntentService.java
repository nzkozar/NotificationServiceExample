package com.ak93.serviceexample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Anže Kožar on 30.8.2016.
 */
public class NotificationIntentService extends IntentService {


    // Sets an ID for the notification
    int mNotificationId = 1;
    private static final String TAG = "NotificationIService";
    private SharedPreferences mPref;

    public NotificationIntentService(){
        super(TAG);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Service started!");
        super.onStartCommand(intent, flags, startId);
        // We want this service to continue running until it is explicitly
        // stopped, in case that it is restarted, the intent should be redelivered.
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Handle restart of service
        int status = 10;
        if(intent == null){
            status = readStatus();
            Log.i(TAG,"Service restarted, continuing at: "+status+"s");
        }
        // Do work here
        Log.i(TAG,"Handling intent!");
        try {
            for(int i = status; i>=0;i--){
                saveStatus(i);
                createNotification("ETA: "+i+"s",(100-i*10));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        removeNotification(mNotificationId);
        Log.i(TAG,"Intent handled.");
    }

    @Override
    public void onCreate() {
        mPref = getBaseContext().getSharedPreferences(getString(R.string.notification_service_prefs), Context.MODE_PRIVATE);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"Service destroyed!");
        super.onDestroy();
    }

    private void createNotification(String message,int progress){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Notification Service")
                        .setContentText(message)
                        .setProgress(100,progress,false);

        Intent resultIntent = new Intent(this, MainActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notification );
    }

    private void removeNotification(int id){
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
    }

    private void saveStatus(int progress){
        SharedPreferences.Editor mPrefEditor = mPref.edit();
        mPrefEditor.putInt("progress",progress);
        mPrefEditor.commit();
    }

    private int readStatus(){
        return mPref.getInt("progress",0);
    }
}
