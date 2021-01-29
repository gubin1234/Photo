package com.example.photo.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.example.photo.receiver.ClockReceiver;
import com.example.photo.utils.AudioPlayer;
import com.example.photo.utils.InfoPrefs;
import com.example.photo.utils.MyPetApplication;

public class ClockService extends Service {
    private static final String TAG = "ClockService";
    ClockReceiver receiver;

    public ClockService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate executed");
        receiver = new ClockReceiver();
        registerReceiver(receiver,new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy executed");
        super.onDestroy();
        if(receiver != null) {
            unregisterReceiver(receiver);
            InfoPrefs.init("pet_info");
            InfoPrefs.setIntData("pet_isopen",0);
            AudioPlayer.getInstance(MyPetApplication.getContext()).stop();
        }
    }
}
