# SampleGallery
How to use it?
 
gradle 

```

compile 'com.sunil.sunilgallery:sunilgallery:1.0.0'

```
<strong>What does it look like ?</strong>

Step 1. main layout
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    tools:context="com.sunil.samplegallery.MainActivity">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <ImageView
                android:id="@+id/ivPreview"
                android:layout_width="100dp"
                android:layout_height="100dp"
                android:layout_centerHorizontal="true"
                android:src="@mipmap/ic_launcher"
                android:visibility="visible" />

            <Button
                android:id="@+id/takePicture"
                android:layout_width="match_parent"
                android:layout_height="30dp"
                android:layout_below="@+id/ivPreview"
                android:layout_marginTop="@dimen/activity_horizontal_margin"
                android:background="@color/colorPrimary"
                android:gravity="center"
                android:text="@string/take_picture"
                android:textColor="@android:color/white"
                android:textSize="@dimen/activity_horizontal_margin" />

            <RelativeLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_below="@+id/takePicture"
                android:layout_marginTop="@dimen/activity_horizontal_margin">

                <ImageView
                    android:id="@+id/video_Preview"
                    android:layout_width="100dp"
                    android:layout_height="100dp"
                    android:layout_marginRight="@dimen/activity_horizontal_margin"
                    android:src="@mipmap/ic_launcher"
                    android:visibility="visible" />

                <VideoView
                    android:id="@+id/video_view"
                    android:layout_width="150dp"
                    android:layout_height="150dp"
                    android:layout_alignParentRight="true"
                    android:layout_toRightOf="@+id/video_Preview" />

                <Button
                    android:id="@+id/takeVideo"
                    android:layout_width="match_parent"
                    android:layout_height="30dp"
                    android:layout_below="@+id/video_view"
                    android:layout_marginTop="@dimen/activity_horizontal_margin"
                    android:background="@color/colorPrimary"
                    android:gravity="center"
                    android:text="@string/take_video"
                    android:textColor="@android:color/white"
                    android:textSize="@dimen/activity_horizontal_margin" />

            </RelativeLayout>
        </RelativeLayout>
    </ScrollView>
</RelativeLayout>

  
```


Step 2. Your java must like that. You donot need to give permission. Library already provide permission.
```java
package com.sunil.samplegallery;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.sunil.sunilgallery.MediaManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    MediaManager mediaManager;
    private ImageView video_Preview;
    private VideoView videoView;
    private Button btnTakeVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.ivPreview);
        mediaManager= new MediaManager(MainActivity.this);
        Button button = (Button)findViewById(R.id.takePicture) ;
        btnTakeVideo = (Button)findViewById(R.id.takeVideo) ;
        videoView = (VideoView)findViewById(R.id.video_view) ;
        video_Preview = (ImageView)findViewById(R.id.video_Preview);
        button.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        MediaManager.goTakePicture(MainActivity.this);

                    }
                });
        btnTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaManager.goTakeVideo(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MediaManager.RECODE_GET_TAKEPICTURE_IMAGE:
                    String imagePath = MediaManager.mCurrentPhotoPath;      // get image path
                    Uri selectedImage = MediaManager.imageUri;             // get image uri
                    File currentFile = MediaManager.mCurrentFile;          // get file
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
                        mImageView.setImageBitmap(bitmap);
                        showToast(imagePath);
                    } catch (Exception e) {
                        showToast("Failed to load");
                        Log.e("Camera", e.toString());
                    }
                    break;

                case MediaManager.RECODE_GET_TAKEPICTURE_MOVIE:
                    Uri mVideoUri = data.getData();
                    String video_path = MediaManager.getPath(this, mVideoUri);                 // get video path
                    Bitmap bitmapVideo = MediaManager.getThumbnailFromURI(this, mVideoUri);   // get video bitmap
                    video_Preview.setImageBitmap(bitmapVideo);
                    File thumbFile = MediaManager.createThumbFile(bitmapVideo, this);  // get video thumb file
                    File videoFile = new File(mVideoUri.getPath());                    // get Video file
                    playVideo(video_path);
                    break;
            }
        }
    }

    private void playVideo(String videoPath){
        MediaController mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(videoView);
        Uri video = Uri.parse(videoPath);
        videoView.setMediaController(mediacontroller);
        videoView.setVideoURI(video);

        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {

                videoView.start();
            }
        });
    }
    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mediaManager.onRequestPermissionsResult(MainActivity.this, requestCode, grantResults);
    }
}

```
LINK:

MyBlog tutorial:

http://sunil-android.blogspot.in/

Copyright 2014 Sunil Gupta

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

