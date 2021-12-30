package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompatSideChannelService;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private ContentResolver contentResolver;
    private ListView playList;
    private MediaCursorAdapter mediaCursorAdapter  = null;
    private Cursor cursor;

    private BottomNavigationView navigationView;
    private TextView bottomTitle;
    private TextView bottomArtist;
    private ImageView thumbnail;
    private ImageView playMusic;
    private MediaPlayer mediaPlayer = null;
    private boolean playStatus = true;
    private ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Cursor cursor = mediaCursorAdapter.getCursor();
            if(cursor!=null&&cursor.moveToPosition(i)){
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                String title  = cursor.getString(titleIndex);
                String artist = cursor.getString(artistIndex);
                Long album = cursor.getLong(albumIndex);
                String data = cursor.getString(dataIndex);

                Uri dataUri = Uri.parse(data);

                if (mediaCursorAdapter!=null){
                    try {
                        mediaCursorAdapter.reset();
                        mediaCursorAdapter.setDataSorce(MainActivity.this,dataUri);
                        mediaCursorAdapter.prepare();
                        mediaCursorAdapter.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                navigationView.setVisibility(View.VISIBLE);

                if(bottomTitle!=null){
                    bottomTitle.setText(title);
                }

                if(bottomArtist!=null){
                    bottomArtist.setText(artist);
                }

                Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,album);

                Cursor albumCursor = contentResolver.query(albumUri,null,null,null,null);

                if(albumCursor!=null&&albumCursor.getCount()>0){
                    albumCursor.moveToFirst();
                    int albumArtistIndex = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                    String albumArtist = albumCursor.getString(albumArtistIndex);
                    Glide.with(MainActivity.this).load(albumArtist).into(thumbnail);
                    albumCursor.close();
                }

            }
        }
    };
    private ProgressBar pbProgress;

    private final String SELECTION = MediaStore.Audio.Media.IS_MUSIC+" =? "+" and "+
            MediaStore.Audio.Media.MIME_TYPE+" like?";

    private final String[] SELECTION_ARGS = {
            Integer.toString(1),
            "audio/mpeg"
    };

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE={
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentResolver = getContentResolver();
        mediaCursorAdapter = new MediaCursorAdapter(MainActivity.this);
        playList= findViewById(R.id.playList);
        playList.setAdapter(mediaCursorAdapter);
        navigationView = findViewById(R.id.navigation);
        LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_toolbar,navigationView,true);

        playMusic = navigationView.findViewById(R.id.play);
        bottomTitle = navigationView.findViewById(R.id.bottomTitle);
        bottomArtist = navigationView.findViewById(R.id.bottomArtist);
        thumbnail = navigationView.findViewById(R.id.thumbnail);
        pbProgress=navigationView.findViewById(R.id.progress);



        if (playMusic!=null){
            playMusic.setOnClickListener((View.OnClickListener) MainActivity.this);
        }

        navigationView.setVisibility(View.GONE);

        playList.setOnItemClickListener(itemClickListener);

        if(mediaPlayer ==null){
            mediaPlayer = new MediaPlayer();
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){ }
            else{
                requestPermissions(PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        }
        else{
            initPlayList();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initPlayList();
                }
                break;
            default:
                break;
        }
    }

    private void initPlayList() {
        cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                SELECTION,
                SELECTION_ARGS,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
        if(cursor!=null){
        mediaCursorAdapter.swapCursor(cursor);
        mediaCursorAdapter.notifyDataSetChanged();}
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mediaPlayer ==null){
            mediaPlayer = new MediaPlayer();
        }
    }

    @Override
    protected void onStop() {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("TGA","onStop invoked!");
        }
        super.onStop();

    }



    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.play:
                playStatus = !playStatus;
                if(playStatus ==true){
                    if(mediaPlayer!=null){
                        mediaPlayer.start();
                    }
                    playMusic.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                }
                else{
                    if(mediaPlayer!=null){
                        mediaPlayer.pause();
                    }
                    playMusic.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                }
                break;
        }
    }
}