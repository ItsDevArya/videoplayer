package com.devarya.VDplayer.activity;


import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devarya.VDplayer.R;
import com.devarya.VDplayer.VideoModel;
import com.devarya.VDplayer.adapter.VideoAdapter;
//import com.devarya.chammo.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.Locale;

public class VideoFolder extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnLongClickListener {


    private static final String MY_SORT_PREF = "sortOrder";
    private RecyclerView recyclerView;
    private String name;
    private ArrayList<VideoModel> videoModelArrayList = new ArrayList<>();
    private VideoAdapter videosAdapter;
    private InterstitialAd mInterstitialAd;
    public boolean is_selectable = false;
    TextView countText;
    ArrayList<VideoModel> selectionArrayList = new ArrayList<>();
    int count = 0;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_folder);

                recyclerView = findViewById(R.id.video_recyclerview);
                countText = findViewById(R.id.counter_textView);

                name = getIntent().getStringExtra("folderName");
                toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.go_back));
                int index= name.lastIndexOf("/");
                String onlyFolderName = name.substring(index + 1);
                toolbar.setTitle(onlyFolderName);
                countText.setText(onlyFolderName);
                loadVideos();
                loadBannerAds();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void loadBannerAds() {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i("TAG", "onAdLoaded");
            }


            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i("TAG", loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(VideoFolder.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
            }
        },4000);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar,menu);
        MenuItem menuItem= menu.findItem(R.id.search);
        SearchView searchView= (SearchView) menuItem.getActionView();
        ImageView ivClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        ivClose.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.white),
                PorterDuff.Mode.SRC_IN);
        searchView.setQueryHint("Search file name");
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);

    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        String input= newText.toLowerCase();
        ArrayList<VideoModel> searchList= new ArrayList<>();

        for (VideoModel model: videoModelArrayList){

            if (model.getTitle().toLowerCase().contains(input)){
                searchList.add(model);
            }
        }
    videosAdapter.updateSearchList(searchList);

        return false;
    }


    private void loadVideos() {
        videoModelArrayList= getallVideoFromFolder(this,name);
        if (name!= null && videoModelArrayList.size()>0){

            videosAdapter= new VideoAdapter(videoModelArrayList,this);

            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setAdapter(videosAdapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL,false));
        }


        else {
                Toast.makeText(this,"No Videos Found",Toast.LENGTH_SHORT).show();
            }


        }

    @SuppressLint("StringFormatMatches")
    private ArrayList<VideoModel> getallVideoFromFolder(Context context, String name) {

            SharedPreferences preferences= getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE);

            //which one u wanna set default bitch!!
            // Setting bydate as default
            String sort = preferences.getString("sorting","sortByDate");
            String order= null;

            switch (sort){

                case "sortByDate":
                    order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                    break;

                case "sortByName":
                    order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                    break;

                case "sortBySize":
                    order = MediaStore.MediaColumns.DATE_ADDED + " DESC";
                    break;
            }

            ArrayList<VideoModel> list = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        String orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.RESOLUTION

        };

        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + name + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, order);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                int size = cursor.getInt(3);
                String resolution = cursor.getString(4);
                int duration = cursor.getInt(5);
                String disName = cursor.getString(6);
                String bucket_display_name = cursor.getString(7);
                String width_height = cursor.getString(8);


                // Convert bytes to human-readable format (e.g., 1204 → 1MB)
                String human_can_read = null;
                if (size < 1024) {
                    human_can_read = String.format(context.getString(R.string.size_in_b), (double) size);
                } else if (size < Math.pow(1024, 2)) {
                    human_can_read = String.format(context.getString(R.string.size_in_kb), (double) (size / 1024));
                } else if (size < Math.pow(1024, 3)) {
                    human_can_read = String.format(context.getString(R.string.size_in_mb), (double) (size / Math.pow(1024, 2)));
                } else {
                    human_can_read = String.format(context.getString(R.string.size_in_gb), (double) (size / Math.pow(1024, 3)));
                }

// Format video duration (e.g., 1331533132 → 1:21:12)
                String duration_formatted;
                int sec = (duration / 1000) % 60;
                int min = (duration / (1000 * 60)) % 60;
                int hrs = duration / (1000 * 60 * 60);

                if (hrs == 0) {
                    duration_formatted = String.valueOf(min)
                            .concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                } else {
                    duration_formatted = String.valueOf(hrs)
                            .concat(":".concat(String.format(Locale.UK, "%02d", min)))
                            .concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                }
                VideoModel files = new VideoModel(id, path, title, human_can_read,
                        resolution, duration_formatted,
                        disName, width_height);
                    if (name.endsWith(bucket_display_name))
                        list.add(files);

                    } cursor.close();

            }

                return list;
        }


        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
            if (item.getItemId() == R.id.sort_by_date) {
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
            } else if (item.getItemId() == R.id.sort_by_name) {
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
            } else if (item.getItemId() == R.id.sort_by_size) {
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
            }

            return super.onOptionsItemSelected(item);

        }


    private void refresh(){
        if (name !=null && videoModelArrayList.size()>0){
            videoModelArrayList.clear();
            //handler will wait for 1.5sec and then run the code..
            //1000=1sec
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadVideos();
                    videosAdapter.notifyDataSetChanged();
                    Toast.makeText(VideoFolder.this, "Refreshed",
                            Toast.LENGTH_SHORT).show();
                }
            },1500);
        }else {
            Toast.makeText(VideoFolder.this, "folder is empty",
                    Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onLongClick(View v) {

        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.item_selected_menu);
        is_selectable = true;
        videosAdapter.notifyDataSetChanged();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.go_back));
        return true;
    }

    @Override
    public boolean onLongClickUseDefaultHapticFeedback(@NonNull View v) {
        return View.OnLongClickListener.super.onLongClickUseDefaultHapticFeedback(v);
    }


    public void prepareSelection(View v, int position) {


        if(  ((CheckBox) v).isChecked() ){
            selectionArrayList.add(videoModelArrayList.get(position));
            count = count + 1;
            updateCount(count);
        }else {
            selectionArrayList.remove(videoModelArrayList.get(position));
            count = count - 1;
            updateCount(count);
        }
    }

    private void updateCount(int counts) {

        if (counts == 0){
            countText.setText("0 item selected");
        }else {
            countText.setText(counts + " item selected");
        }
    }




    private void clearSelectingToolbar(){
        is_selectable = false;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.main_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.go_back));
        int index = name.lastIndexOf("/");
        String onlyFolderName = name.substring(index + 1);
        countText.setText(onlyFolderName);
        count = 0;
        selectionArrayList.clear();
    }


    @Override
    public void onBackPressed() {
        if (is_selectable){
            clearSelectingToolbar();
            videosAdapter.notifyDataSetChanged();
        }else {
            super.onBackPressed();
        }
    }

}
