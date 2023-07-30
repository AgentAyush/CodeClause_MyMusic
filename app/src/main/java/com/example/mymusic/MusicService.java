package com.example.mymusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;
import java.util.ArrayList;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private ArrayList<File> songs;
    private int currentSongPosition;

    public static final String ACTION_PREVIOUS = "com.example.mymusic.ACTION_PREVIOUS";
    public static final String ACTION_PLAY_PAUSE = "com.example.mymusic.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.example.mymusic.ACTION_NEXT";

    private final IBinder binder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_PREVIOUS:
                    playPreviousSong();
                    break;
                case ACTION_PLAY_PAUSE:
                    playPauseSong();
                    break;
                case ACTION_NEXT:
                    playNextSong();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void setSongs(ArrayList<File> songs) {
        this.songs = songs;
    }

    public void setCurrentSongPosition(int position) {
        currentSongPosition = position;
    }

    public void playSong() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songs.get(currentSongPosition).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playPreviousSong() {
        currentSongPosition = (currentSongPosition - 1 + songs.size()) % songs.size();
        playSong();
    }

    public void playPauseSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public void playNextSong() {
        currentSongPosition = (currentSongPosition + 1) % songs.size();
        playSong();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
