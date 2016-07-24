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
