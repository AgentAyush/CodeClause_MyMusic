package com.example.mymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

class NotificationReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra("action", -1);
        if (action != -1) {
            switch (action) {
                case 1:
                    // Pause button clicked
                    // Pause the MediaPlayer
                    ((PlaySong) context).pauseSong();
                    break;
                case 2:
                    // Play button clicked
                    // Resume the MediaPlayer
                    ((PlaySong) context).resumeSong();
                    break;
                case 3:
                    // Next button clicked
                    // Play the next song
                    ((PlaySong) context).playNextSong();
                    break;
                case 4:
                    // Previous button clicked
                    // Play the previous song
                    ((PlaySong) context).playPreviousSong();
                    break;
            }
        }
    }
}
