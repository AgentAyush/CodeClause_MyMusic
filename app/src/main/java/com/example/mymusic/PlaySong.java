package com.example.mymusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PlaySong extends AppCompatActivity {
    TextView textView;
    ImageView play, previous, next, backward5sec, forward5sec, repeat, shuffle,thumbnail;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    Thread updateSeek;
    Button btnToggleDark;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "mymusic_notification_channel";
    private NotificationManagerCompat notificationManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                "MyMusic Notification",
                NotificationManager.IMPORTANCE_HIGH
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)

    private void showNotification(String songName, boolean isPlaying) {
        // Cancel the previous notification
        notificationManager.cancel(NOTIFICATION_ID);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_note)
                .setContentTitle("MyMusic")
                .setContentText(songName)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        // Add play/pause button to the notification
        if (isPlaying) {
            builder.addAction(R.drawable.pause, "Pause", getPendingIntentAction(1));
        } else {
            builder.addAction(R.drawable.play, "Play", getPendingIntentAction(2));
        }

        // Add next button to the notification
        builder.addAction(R.drawable.next, "Next", getPendingIntentAction(3));

        // Add previous button to the notification
        builder.addAction(R.drawable.previous, "Previous", getPendingIntentAction(4));

        // Set the flag for the PendingIntent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        // Create the PendingIntent with the flags
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, PlaySong.class), flags);

        // Set the PendingIntent to the notification builder
        builder.setContentIntent(pendingIntent);

        // Set the notification to be dismissable with a swipe
        builder.setAutoCancel(true);

        // Show the notification
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private PendingIntent getPendingIntentAction(int action) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("action", action);

        PendingIntent pendingIntent;
        switch (action) {
            case 1:
                pendingIntent = PendingIntent.getBroadcast(this, action, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                break;
            case 2:
                pendingIntent = PendingIntent.getActivity(this, action, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                break;
            default:
                pendingIntent = PendingIntent.getActivity(this, action, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                break;
        }
        return pendingIntent;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        updateSeek.interrupt();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        backward5sec = findViewById(R.id.backward5sec);
        forward5sec = findViewById(R.id.forward5sec);
        repeat = findViewById(R.id.repeat);
        shuffle = findViewById(R.id.shuffle);
        thumbnail = findViewById(R.id.thumbnail);
        mediaPlayer = new MediaPlayer();
// Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        // Set the notification manager
        notificationManager = NotificationManagerCompat.from(this);

        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals("notification_action")) {
            int action = intent.getIntExtra("action", -1);
            if (action != -1) {
                switch (action) {
                    case 1:
                        // Pause button clicked
                        // Pause the MediaPlayer
                        pauseSong();
                        break;
                    case 2:
                        // Play button clicked
                        // Resume the MediaPlayer
                        resumeSong();
                        break;
                    case 3:
                        // Next button clicked
                        // Play the next song
                        playNextSong();
                        break;
                    case 4:
                        // Previous button clicked
                        // Play the previous song
                        playPreviousSong();
                        break;
                }
            }
        }
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songList");
        textContent = intent.getStringExtra("currentSong");
        textView.setText(textContent);
        textView.setSelected(true);
        position = intent.getIntExtra("position", 0);

        Uri songUri = Uri.parse(songs.get(position).toString());
        setThumbnail(songUri);
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
        showNotification(textContent, true);
        seekBar.setMax(mediaPlayer.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int currentPosition = mediaPlayer.getCurrentPosition();

                backward5sec.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (currentPosition >= 5 && currentPosition < mediaPlayer.getDuration()) {
                            mediaPlayer.seekTo(currentPosition - 5000);
                        } else {
                            mediaPlayer.seekTo(0);
                        }

                    }
                });

                forward5sec.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (currentPosition <= mediaPlayer.getDuration() - 5000) {
                            mediaPlayer.seekTo(currentPosition + 5000);
                        } else {
                            mediaPlayer.seekTo(mediaPlayer.getDuration());
                        }
                    }
                });
                repeat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mediaPlayer.seekTo(0);
                    }
                });


                if (mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();

                    if (position != songs.size() - 1) {
                        position = position + 1;
                    } else {
                        position = 0;

                    }

                    Uri uri = Uri.parse(songs.get(position).toString());
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    mediaPlayer.start();
                    play.setImageResource(R.drawable.pause);
                    seekBar.setMax(mediaPlayer.getDuration());
                    textContent = songs.get(position).getName().toString();
                    textView.setText(textContent);

                    updateSeek = new Thread() {
                        @Override
                        public void run() {
                            int currentPosition = 0;
                            try {
                                while (currentPosition < mediaPlayer.getDuration()) {
                                    currentPosition = mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(currentPosition);
//                        sleep(800);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    updateSeek.start();
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        updateSeek = new Thread() {
            @Override
            public void run() {
                int currentPosition = 0;
                try {
                    while (currentPosition < mediaPlayer.getDuration()) {
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
//                        sleep(800);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    play.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                } else {
                    play.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                }

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (position != 0) {
                    position = position - 1;
                } else {
                    position = songs.size() - 1;
                }
                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//                Uri songUri = Uri.parse(songs.get(position).toString());
                setThumbnail(uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.pause);
                seekBar.setMax(mediaPlayer.getDuration());
                textContent = songs.get(position).getName().toString();
                textView.setText(textContent);

                updateSeek = new Thread() {
                    @Override
                    public void run() {
                        int currentPosition = 0;
                        try {
                            while (currentPosition < mediaPlayer.getDuration()) {
                                currentPosition = mediaPlayer.getCurrentPosition();
                                seekBar.setProgress(currentPosition);
//                        sleep(800);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                updateSeek.start();
            }
        });
        shuffle = findViewById(R.id.shuffle);
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Shuffle the songs list
                Collections.shuffle(songs);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (position != songs.size() - 1) {
                    position = position + 1;
                } else {
                    position = 0;
                }
                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//                Uri songUri = Uri.parse(songs.get(position).toString());
                setThumbnail(uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.pause);
                seekBar.setMax(mediaPlayer.getDuration());
                textContent = songs.get(position).getName().toString();
                textView.setText(textContent);

                updateSeek = new Thread() {
                    @Override
                    public void run() {
                        int currentPosition = 0;
                        try {
                            while (currentPosition < mediaPlayer.getDuration()) {
                                currentPosition = mediaPlayer.getCurrentPosition();
                                seekBar.setProgress(currentPosition);
//                        sleep(800);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                updateSeek.start();

            }
        });


    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            showNotification(textContent, false);
        } else {
            mediaPlayer.start();
            showNotification(textContent, true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            showNotification(textContent, true); // Update the notification to show play button
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void playNextSong() {
        mediaPlayer.stop();
        mediaPlayer.release();
        position = (position + 1) % songs.size(); // Increment position circularly
        playSelectedSong();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void playPreviousSong() {
        mediaPlayer.stop();
        mediaPlayer.release();
        position = (position - 1 + songs.size()) % songs.size(); // Decrement position circularly
        playSelectedSong();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void playSelectedSong() {
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
        play.setImageResource(R.drawable.pause);
        seekBar.setMax(mediaPlayer.getDuration());
        textContent = songs.get(position).getName().toString();
        textView.setText(textContent);

        // Update the notification with the new song details
        showNotification(textContent, true);

        // Update the seekBar
        updateSeek = new Thread() {
            @Override
            public void run() {
                int currentPosition = 0;
                try {
                    while (currentPosition < mediaPlayer.getDuration()) {
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();
    }
    private void setThumbnail(Uri songUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, songUri);

        byte[] art = retriever.getEmbeddedPicture();
        if (art != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            thumbnail.setImageBitmap(bitmap);
        } else {
            // Set a default thumbnail image if no album art is available
            thumbnail.setImageResource(R.drawable.image );
        }

        retriever.release();
    }

}