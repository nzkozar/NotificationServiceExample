package com.ak93.serviceexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Anže Kožar on 30.8.2016.
 */
public class NotificationService extends Service {

    // Sets an ID for the notification
    int mNotificationId = 2;
    private static final String TAG = "NotificationService";

    private ServiceHandler mServiceHandler;
    private SharedPreferences mPref;


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            //Handle restart of service
            Intent intent = (Intent)msg.obj;
            int status = 0;
            if(intent == null){
                status = readStatus();
                Log.i(TAG,"Service restarted, continuing at: "+(status*10)+"%");
            }
            // Do work here
            Log.i(TAG,"Handling intent!");
            try {
                for(int i = status; i<=10;i++){
                    saveStatus(i);
                    createNotification(TAG,"Downloading: "+(i*10)+"%",(i*10));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            createNotification(TAG,"Download complete",100);
            saveStatus(0);
            Log.i(TAG,"Intent handled.");
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.what);
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate");
        mPref = getBaseContext().getSharedPreferences(getString(R.string.notification_service_prefs), Context.MODE_PRIVATE);
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        mServiceHandler.obtainMessage(startId,intent).sendToTarget();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification(String title, String message,int progress){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
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
