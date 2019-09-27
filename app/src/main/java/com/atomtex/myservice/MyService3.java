package com.atomtex.myservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyService3 extends Service {

    //это сервис без BroadCastReceiver, выполняет каждую команду в отдельном потоке,
    //изначально сервис завершал работу после завершения последней задачи,
    // если при этом последняя задача завершалась раньше, сервис не завершался
    // я пофиксил это, теперь сервис завершается после завершения работы последнего остановившегося
    // потока (см.
    //      if (counter == 1) stopSelf();  //если последний оставшийся поток -- он завершится и остановит сервис
    //      else Log.e(LOG_TAG, "попытка убить сервис");
    //      counter--;)
    //Ещё здесь добавлен Notification и Foreground


    private static final int NOTIFICATION_ID = 1;
    final String LOG_TAG = "myLogs";
    ExecutorService es;
    Object someRes;
    int counter;



    public void onCreate() {
        super.onCreate();
        Log.e(LOG_TAG, "MyService onCreate");
        es = Executors.newFixedThreadPool(3);
        someRes = new Object();
        counter = 0;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e(LOG_TAG, "MyService onDestroy");
        someRes = null;
    }


    //Вообще для вызова Foreground здесь только одна строчка (последняя),
    // весь метод в основном занимается созданием Notification
    private void initStartForeground() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        NotificationCompat.Builder mNotificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel("ID", "Notification", NotificationManager.IMPORTANCE_HIGH);
            mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
        } else {
            //noinspection deprecation
            mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        }

        //Build the a notification
        mNotificationBuilder.setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher_round))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setWhen(new Date().getTime())
                .setUsesChronometer(true)
                .setStyle(new NotificationCompat.BigTextStyle())
//                .setContentTitle(String.format("R.string.notification_title", "R.string.status_start"))
                .setContentTitle("Сервис работает...");
//                .setContentIntent(resultPendingIntent); //не знаю, зачем он здесь
        Notification notification = mNotificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        initStartForeground();

        Log.e(LOG_TAG, "MyService onStartCommand");
        int time = intent.getIntExtra("time", 1);
        Log.e(LOG_TAG, "onStartCommand: " + time);
        MyService3.MyRun mr = new MyService3.MyRun(time, startId);
        es.execute(mr);
        counter++;
        Log.e(LOG_TAG, "---------COUNTER = " + counter);
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    class MyRun implements Runnable {

        int time;
        int startId;

        MyRun(int time, int startId) {
            this.time = time;
            this.startId = startId;
            Log.e(LOG_TAG, "MyRun#" + startId + " create");
        }

        public void run() {
            Log.e(LOG_TAG, "MyRun#" + startId + " start, time = " + time);
            for (int i = 1; i <= time; i++) {
                Log.d(LOG_TAG, "Thread#" + startId + ", i = " + i);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //TimeUnit.SECONDS.sleep(time);
            try {
                Log.e(LOG_TAG, "MyRun#" + startId + " someRes = " + someRes.getClass());
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "MyRun#" + startId + " error, null pointer");
            }
            Log.e(LOG_TAG, "-----------startID = " + startId + ", counter = " + counter);
            if (counter == 1) stopSelf();  //если последний оставшийся поток -- он завершится и остановит сервис
            else Log.e(LOG_TAG, "попытка убить сервис");
            counter--;

        }

        void stop() {
            Log.e(LOG_TAG, "MyRun#" + startId + " end, stopSelf(" + startId + ")");
            stopSelf(startId);
        }
    }
}
