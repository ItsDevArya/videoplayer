package com.devarya.VDplayer.adapter;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.devarya.VDplayer.R;
import com.devarya.VDplayer.VideoModel;
import com.devarya.VDplayer.activity.VideoFolder;
import com.devarya.VDplayer.activity.VideoPlayer;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyHolder> {


        public static ArrayList<VideoModel> videoFolder = new ArrayList<>();
        private final Context context;
        VideoFolder videoFolderActivity;

    public VideoAdapter(ArrayList<VideoModel> videoFolder, Context context) {
        this.videoFolder = videoFolder;
        this.context = context;
        videoFolderActivity = (VideoFolder) context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.files_view,parent,false);
        return new MyHolder(view,videoFolderActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {

        Glide.with(context).load(videoFolder.get(position).getPath()).into(holder.thumbnail);
        holder.title.setText(videoFolder.get(position).getTitle());
        holder.duration.setText(videoFolder.get(position).getDuration());
        holder.resolution.setText(videoFolder.get(position).getResolution());
        holder.size.setText(videoFolder.get(position).getSize());
        holder.menu.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(context,R.style.BottomSheetDialogTheme);
            View bottomSheetView= LayoutInflater.from(context).inflate(R.layout.file_menu,null);


            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

            bottomSheetView.findViewById(R.id.menu_down).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
            });
            bottomSheetView.findViewById(R.id.menu_share).setOnClickListener(v2 -> {
                Toast.makeText(context,"Share",Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
                shareFiles(position);
            });
            bottomSheetView.findViewById(R.id.rename).setOnClickListener(v3 -> {
                renameFiles(position,v);
            });

            bottomSheetView.findViewById(R.id.menu_delete).setOnClickListener(v4 -> {
                bottomSheetDialog.dismiss();
                 deleteFiles(position,v);
            });

            bottomSheetView.findViewById(R.id.menu_properties).setOnClickListener(v5 -> {
                bottomSheetDialog.dismiss();
                showProperties(position);
            });
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayer.class);
                intent.putExtra("p",position);
                context.startActivity(intent);
            }
        });



        if (videoFolderActivity.is_selectable){
            //when true
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.menu.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }else {
            //when false
            holder.checkBox.setVisibility(View.GONE);
            holder.menu.setVisibility(View.VISIBLE);
        }

    }

    private void shareFiles(int p){

        Uri uri=  Uri.parse(videoFolder.get(p).getPath());
        Intent intent= new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        context.startActivity(Intent.createChooser(intent,"Share"));
        Toast.makeText(context, "loading..", Toast.LENGTH_SHORT).show();


    }

    private void deleteFiles(int p, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete")
                .setMessage(videoFolder.get(p).getTitle())
                .setNegativeButton("cancel", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    //toto leave as it is

                    }

        }).setPositiveButton("ok", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

//                Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show();
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(videoFolder.get(p).getId()));
                File file = new File(videoFolder.get(p).getPath());
                boolean deleted = file.delete();

                if (deleted) {
                    context.getApplicationContext().getContentResolver().delete(contentUri, null, null);
                    videoFolder.remove(p);
                    notifyItemRemoved(p);
                    notifyItemRangeChanged(p, videoFolder.size());
                    Snackbar.make(view, "File Deleted Success", Snackbar.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(view, "File Deleted Fail", Snackbar.LENGTH_SHORT).show();

                }
            }

        }).show();

    }

    private void renameFiles(int position, View view ){
        final Dialog dialog= new Dialog(context);
        dialog.setContentView(R.layout.rename_layout);
        EditText edit_text = dialog.findViewById(R.id.rename_edit_text);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        Button rename_btn = dialog.findViewById(R.id.rename_button);
        final File renameFile= new File(videoFolder.get(position).getPath());
        String nameText= renameFile.getName();
        nameText = nameText.substring(0,nameText.lastIndexOf("."));
        edit_text.setText(nameText);
        edit_text.clearFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        cancel.setOnClickListener(v ->{
                dialog.dismiss();
        });
         rename_btn.setOnClickListener(v1->{
            String onlyPath = renameFile.getParentFile().getAbsolutePath();
            String ext= renameFile.getAbsolutePath();
            ext = ext.substring(ext.lastIndexOf("."));
            String newPath = onlyPath + "/" + edit_text.getText() + ext;
            File newFile = new File (newPath);
            boolean rename = renameFile.renameTo(newFile);
            if (rename){
                context.getApplicationContext().getContentResolver().delete(MediaStore.Files.getContentUri("external"),
                        MediaStore.MediaColumns.DATA+ "=?",new String[]{renameFile.getAbsolutePath()});
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(newFile));
                    context.getApplicationContext().sendBroadcast(intent);
                    Snackbar.make(view, "File Rename Success", Snackbar.LENGTH_SHORT).show();
                    dialog.show();
            } else {
                Snackbar.make(view, "File Rename Failed", Snackbar.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
         dialog.show();
    }

    private void showProperties(int p){
    Dialog dialog= new Dialog(context);
    dialog.setContentView(R.layout.file_properties);
    String name= videoFolder.get(p).getTitle();
    String path= videoFolder.get(p).getPath();
    String size= videoFolder.get(p).getSize();
    String duration= videoFolder.get(p).getDuration();
    String resolution= videoFolder.get(p).getResolution();

    TextView pro_title= dialog.findViewById(R.id.pro_title);
    TextView st= dialog.findViewById(R.id.pro_storage);
    TextView pro_size= dialog.findViewById(R.id.pro_size);
    TextView pro_duration= dialog.findViewById(R.id.pro_duration);
    TextView pro_resolution= dialog.findViewById(R.id.pro_resolution);

    pro_title.setText(name);
    st.setText(path);
    pro_size.setText(size);
    pro_duration.setText(duration);
    pro_resolution.setText(resolution+"p");

    dialog.show();

    }



    @Override
    public int getItemCount() {
        return videoFolder.size();
    }

    public void updateSearchList(ArrayList<VideoModel> searchList) {

        videoFolder= new ArrayList<>();
        videoFolder.addAll(searchList);
        notifyDataSetChanged();
    }


    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView thumbnail ,menu;
        TextView title, duration, resolution, size;
        CheckBox checkBox;
        VideoFolder videoFolderActivity;



        public MyHolder(@NonNull View itemView, VideoFolder videoFolderActivity) {
            super(itemView);

            thumbnail= itemView.findViewById(R.id.thumbnail);
            title= itemView.findViewById(R.id.video_title);
           size= itemView.findViewById(R.id.video_size);
           duration = itemView.findViewById(R.id.video_duration);
            resolution= itemView.findViewById(R.id.video_quality);
            menu= itemView.findViewById(R.id.video_menu);
            checkBox = itemView.findViewById(R.id.video_folder_checkbox);
            this.videoFolderActivity = videoFolderActivity;

            checkBox.setOnClickListener(this);

            itemView.setOnLongClickListener(videoFolderActivity);
        }

        @Override
        public void onClick(View v) {

            videoFolderActivity.prepareSelection(v, getAdapterPosition());
        }
    }
}
