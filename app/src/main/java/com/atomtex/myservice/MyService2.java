package com.atomtex.myservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class MyService2 extends Service {

    /**
     * There can only be one instance of a Service at a time
     * No matter how you create it, there can only ever be one instance at a time,
     * even when external applications/processes interact with it.
     */

    final String LOG_TAG = "myLogs";
    int startId;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(LOG_TAG, "-----------onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(LOG_TAG, "-------------onStartCommand, ID = " + startId);
        //someTask_bad();
        someTask(startId);
        this.startId = startId;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf(1);
        Log.e(LOG_TAG, "------------onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(LOG_TAG, "--------------onBind");
        return null;
    }

    //выполнение сервися в потоке активити -- так делать нельзя, это только для проверки
    void someTask_bad() {
        for (int i = 1; i <= 5; i++) {
            Log.d(LOG_TAG, "i = " + i);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //каждый раз при клике создает новый поток, паралельно уже работающему
    void someTask(final int num) {
        new Thread(new Runnable() {
            public void run() {
                for (int i = 1; i <= 15; i++) {
                    Log.d(LOG_TAG, "Thread#" + num + ", i = " + i);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stopSelf(num);
                Log.e(LOG_TAG, "stop Thread #" + num);
            }
        }).start();
        startId -= 1;
    }
}
