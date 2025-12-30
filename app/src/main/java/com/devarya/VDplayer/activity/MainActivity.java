package com.devarya.VDplayer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devarya.VDplayer.R;
import com.devarya.VDplayer.VideoModel;
import com.devarya.VDplayer.adapter.FolderAdapter;
//import com.devarya.chammo.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private ArrayList<String> folderlist = new ArrayList<>();
    private ArrayList<VideoModel> videoList = new ArrayList<>();
    FolderAdapter folderAdapter;
    RecyclerView recyclerView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

                    recyclerView= findViewById(R.id.folder_recyclerview);
                    videoList = fetchAllVideos(this);

                    if (folderlist!=null && folderlist.size()> 0 && videoList !=null) {

                        folderAdapter = new FolderAdapter(folderlist,videoList,this);
                        recyclerView.setAdapter(folderAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                                RecyclerView.VERTICAL,false ));

                    }
                        else {
                            Toast.makeText(this, "Can't find any Videos folder", Toast.LENGTH_SHORT).show();
                        }

                    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                        return insets;
                    });
                }

        //this method will fetch all videos from internal and external storage
        private ArrayList<VideoModel> fetchAllVideos(Context context) {
            ArrayList<VideoModel> videoModels= new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String orderBy = MediaStore.Video.Media.DATE_ADDED +" DESC";

          String[] projection = {
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.TITLE,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.HEIGHT,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.RESOLUTION,
                  MediaStore.Video.Media.RESOLUTION
                      };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String size = cursor.getString(3);
                String resolution = cursor.getString(4);
                String duration = cursor.getString(5);
                String disName = cursor.getString(6);
                String width_height = cursor.getString(7);

                VideoModel videoFiles = new VideoModel(id, path, title, size, resolution, duration, disName, width_height);

                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);

                if (!folderlist.contains(subString)) {
                    folderlist.add(subString);
                }
                videoModels.add(videoFiles);
            }
            cursor.close();
        }
        return videoModels;
    }
}
