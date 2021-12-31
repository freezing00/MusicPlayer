package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class MusicService extends Service {
    private static final String CHANNEL_ID = "Music channel";
    private static final String ONGOING_NOTIFICATION_ID = "1001";
    NotificationManager notificationManager;
    MediaPlayer mediaPlayer;

    public MusicService() {
    }

    @Override
    public void onDestroy() {

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer =null;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer =new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String data = intent.getStringExtra(MainActivity.DATA_URI);
        String title = intent.getStringExtra(MainActivity.TITLE);
        String artist = intent.getStringExtra(MainActivity.ARTIST);
        Uri dataUri = Uri.parse(data);

        if(mediaPlayer!=null){
            try{
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(getApplicationContext(),dataUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"Music Channel",NotificationManager.IMPORTANCE_HIGH);
            if(notificationManager!= null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Intent notificationIntent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,0);
        NotificationCompat.Builder builder;

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
        }
        else{
            builder = new NotificationCompat.Builder(getApplicationContext());
        }

        Notification notification;
        notification = builder.setContentTitle(title).setContentText(artist).setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent).build();
        startForeground(Integer.parseInt(ONGOING_NOTIFICATION_ID),notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
