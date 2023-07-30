package com.example.mymusic;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class AudioFileScanner {
    private Context context;

    public AudioFileScanner(Context context) {
        this.context = context;
    }

    public List<String> scanAudioFiles() {
        List<String> audioFiles = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String fileTitle = fileName.substring(0, fileName.lastIndexOf('.'));
                audioFiles.add(fileTitle);
            }

            cursor.close();
        }

        return audioFiles;
    }

}
