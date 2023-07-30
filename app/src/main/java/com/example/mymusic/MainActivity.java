package com.example.mymusic;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
//import android.widget.SearchView;
import androidx.appcompat.widget.SearchView;


import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<File> mySongs; // Cached song list
    private static final String CACHE_FILE_NAME = "song_cache";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        // Check if songs are already cached
        if (mySongs == null) {
            if (isCacheFileExists()) {
                // Cache file exists, retrieve cached song list
                mySongs = retrieveCachedSongs();
                setListViewAdapter();
            } else {
                // Cache file doesn't exist, perform scan and cache songs
                performSongsScan();
            }
        } else {
            // Songs are already cached, use the existing list
            setListViewAdapter();
        }
    }

    private void performSongsScan() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        mySongs = fetchSongs(Environment.getExternalStorageDirectory());
                        cacheSongs(mySongs);
                        setListViewAdapter();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        // Handle permission denied case
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }


    private boolean isCacheFileExists() {
        SharedPreferences preferences = getSharedPreferences(CACHE_FILE_NAME, MODE_PRIVATE);
        return preferences.getBoolean("cached", false);
    }

    private ArrayList<File> retrieveCachedSongs() {
        ArrayList<File> cachedSongs = new ArrayList<>();
        SharedPreferences preferences = getSharedPreferences(CACHE_FILE_NAME, MODE_PRIVATE);
        int size = preferences.getInt("size", 0);
        for (int i = 0; i < size; i++) {
            String path = preferences.getString("song" + i, "");
            if (!path.isEmpty()) {
                cachedSongs.add(new File(path));
            }
        }
        return cachedSongs;
    }

    private void cacheSongs(ArrayList<File> songs) {
        SharedPreferences preferences = getSharedPreferences(CACHE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putInt("size", songs.size());
        for (int i = 0; i < songs.size(); i++) {
            editor.putString("song" + i, songs.get(i).getPath());
        }
        editor.putBoolean("cached", true);
        editor.apply();
    }

    public ArrayList<File> fetchSongs(File file) {
        ArrayList<File> songs = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File currentFile : files) {
                if (currentFile.isDirectory()) {
                    songs.addAll(fetchSongs(currentFile)); // Recursive call to fetch songs from subdirectory
                } else {
                    String name = currentFile.getName();
                    if ((name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav")) && !name.startsWith(".")) {
                        songs.add(currentFile);
                    }
                }
            }
        }
        return songs;
    }

    private void setListViewAdapter() {
        ArrayList<String> items = new ArrayList<>();
        for (File file : mySongs) {
            String name = file.getName();
            if (name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav")) {
                String itemName = name.substring(0, name.lastIndexOf('.'));
                items.add(itemName); // Add the name without extension to the items list
            }
        }

        Collections.sort(items, new Comparator<String>() {
            public int compare(String name1, String name2) {
                return name1.compareToIgnoreCase(name2);
            }
        });

        // Sort the mySongs ArrayList based on the sorted items ArrayList
        ArrayList<File> sortedSongs = new ArrayList<>();
        for (String itemName : items) {
            for (File file : mySongs) {
                String name = file.getName();
                String fileNameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
                if (fileNameWithoutExtension.equals(itemName)) {
                    sortedSongs.add(file);
                    break;
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlaySong.class);
                String currentSong = listView.getItemAtPosition(position).toString();
                intent.putExtra("songList", sortedSongs); // Use the sortedSongs ArrayList
                intent.putExtra("currentSong", currentSong);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }


    private void filterSongs(String query) {
        ArrayList<String> filteredItems = new ArrayList<>();
        ArrayList<File> filteredSongs = new ArrayList<>();

        for (File file : mySongs) {
            String name = file.getName();
            if (name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav")) {
                String itemName = name.substring(0, name.lastIndexOf('.'));
                if (itemName.toLowerCase().contains(query.toLowerCase())) {
                    filteredItems.add(name);
                    filteredSongs.add(file);
                }
            }
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
        adapter.clear();
        adapter.addAll(filteredItems);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlaySong.class);
                String currentSong = listView.getItemAtPosition(position).toString();
                intent.putExtra("songList", filteredSongs); // Use the filteredSongs ArrayList
                intent.putExtra("currentSong", currentSong);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView from the menu item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Set up the search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText); // Call a method to filter the songs based on the search query
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Handle other menu item clicks here if needed
        return super.onOptionsItemSelected(item);
    }


}
