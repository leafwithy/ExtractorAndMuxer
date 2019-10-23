package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.myapplication.Constraint.filePath;
import static com.example.myapplication.Constraint.filePath2;
import static com.example.myapplication.Constraint.path;

/**
 * Created by weizheng.huang on 2019-09-26.
 */
public class MainActivity extends Activity {

    private VideoView systemVideoView;

    private MediaController mediaController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

         Log.v("tag",path);
        systemVideoView = findViewById(R.id.videoView);
        mediaController = new MediaController(this);
        mediaController.show();
        systemVideoView.setMediaController(mediaController);
        if (verifyPermissino(this)) {
           Log.v("tag", "写入sdcard");
            saveToSDCard(filePath2, getResources().openRawResource(R.raw.guanghuisuiyue_huren));
            saveToSDCard(filePath,getResources().openRawResource(R.raw.shape_of_my_heart));
        }
        try {
          //  ExtractorMuxer.divideMedia(filePath2,path);
            MuxerMedia.divideMedia(filePath,filePath2,path);

        } catch (IOException e) {
            e.printStackTrace();
        }

        systemVideoView.setVideoURI(Uri.fromFile(new File(path)));


    }



    private void saveToSDCard(String filename, InputStream is) {


        File file = new File(filename);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] bytes = new byte[1024*1024];
        int size = -1;
        while (true) {
           try{
               if(((size=is.read(bytes))>0)){
                   os.write(bytes,0,size);
               }else{
                   break;
               }
           }catch (IOException e){
               e.printStackTrace();
           }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean verifyPermissino(Activity activity){
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,1);
        }
        return permission==PackageManager.PERMISSION_GRANTED;
    }
}
