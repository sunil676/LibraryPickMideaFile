package com.sunil.sunilgallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunil on 18-Jul-16.
 */
public class MediaManager {
    public static final int CROP_MAX_SIZE = 410;

    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    public static final int MAIN_PICTURE_HEIGHT = 244;
    public static final int MAIN_PICTURE_WIDTH = 290;
    public static Uri mCaptureUri;
    public static final int MINIMUM_FREE_SIZE = 200; // 200 MB
    public static final int RECODE_CROP_CALLERY_IMAGE = 123;
    public static final int RECODE_CROP_TAKEPICTURE_IMAGE = 124;
    /** Request code define **/
    public static final int RECODE_GET_GALLERY_IMAGE = 119;

    public static final int RECODE_GET_GALLERY_IMAGE_PREVIEW = 125;
    public static final int RECODE_GET_GALLERY_MOVIE = 121;
    public static final int RECODE_GET_TAKEPICTURE_IMAGE = 120;
    public static final int RECODE_GET_TAKEPICTURE_MOVIE = 122;
    public static final String TEMP_PICTURE_FILENAME = "IMG_";
    /** Thread **/
    public static final int THREAD_FAIL_MEDIA = 17364;
    public static final int THREAD_LACK_MEMORY = 17365;

    Activity activity;

    public MediaManager(Activity activity) {
        this.activity = activity;
    }


    public static String pathLastCamera(Context context) {
        final String[] imageColumns = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA };
        final String imageOrderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
        Cursor imageCursor = ((Activity) context).managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
                null, null, imageOrderBy);
        imageCursor.moveToFirst();
        do {

            String fullPath = imageCursor.getString(imageCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            if (fullPath.contains("DCIM")) {
                // --last image from camera --

                return fullPath;

            }
        } while (imageCursor.moveToNext());
        return "";
    }
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public static String getMoviePath(Activity activity, Uri _uri) {
        String mPath = "";
        String[] projection = { MediaStore.Video.VideoColumns.DATA };
        Cursor cursor = activity.managedQuery(_uri, projection, null, null,
                null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA);
        cursor.moveToFirst();

        mPath = cursor.getString(column_index);
        cursor.close();
        return mPath;
    }

    public static int getVideoSize(Activity activity, Uri _uri) {
        int mSize = 0;
        String[] projection = { MediaStore.Video.VideoColumns.SIZE };
        Cursor cursor = activity.managedQuery(_uri, projection, null, null,
                null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE);
        cursor.moveToFirst();

        mSize = cursor.getInt(column_index);
        cursor.close();
        return mSize;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public static void goGetMovie(Activity activity, Handler mHandler) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            activity.startActivityForResult(intent, RECODE_GET_GALLERY_MOVIE);
        } catch (Exception e) {
        }
    }

    public static void goGetPicture(Activity activity) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            activity.startActivityForResult(intent, RECODE_GET_GALLERY_IMAGE);
        } catch (Exception e) {

        }

    }




    public static void goTakeVideo(Activity activity) {
        if(checkPermissionForExternalStorage(activity)) {
            if(checkPermissionForCamera(activity)) {
                openVideo(activity);
            }
        }

    }

    private static void openVideo(Activity activity){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        activity.startActivityForResult(intent, RECODE_GET_TAKEPICTURE_MOVIE);
    }

    public static String getPathVideoFromURI(Activity activity, Uri mVideoUri) {
        String[] projection = { MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE };
        Cursor cursor = activity.managedQuery(mVideoUri, projection, null,
                null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        int column_index_size = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
        String recordedVideoFilePath = "";
        if (cursor.moveToFirst())
            recordedVideoFilePath = cursor.getString(column_index_data);
        return recordedVideoFilePath;
    }

    public static String getPathImageFromURI(Activity activity, Uri mImageUri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(mImageUri, projection, null,
                null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

        String recordedVideoFilePath = "";
        if (cursor.moveToFirst())
            recordedVideoFilePath = cursor.getString(column_index_data);
        return recordedVideoFilePath;
    }

    public static int countFirst = 0;
    public static int countLast = 0;

    public static void goTakePicture(Activity activity) {
        if(checkPermissionForExternalStorage(activity)) {
            if(checkPermissionForCamera(activity)) {
                openCamera(activity);
            }
        }

    }

    private static void openCamera(Activity activity){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mCurrentFile = setUpPhotoFile();
             Log.d("cameraaaa", "f:" + mCurrentFile);
             mCurrentPhotoPath =mCurrentFile.getAbsolutePath();
            imageUri= Uri.fromFile(mCurrentFile);
            activity.getContentResolver().notifyChange(imageUri, null);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            mCurrentFile = null;
            Log.d("cameraaaa", "IOException" );
            mCurrentPhotoPath = null;
        }
        activity.startActivityForResult(takePictureIntent, RECODE_GET_TAKEPICTURE_IMAGE);
    }

    public static String mCurrentPhotoPath;
    public static Uri imageUri;
    public static File mCurrentFile;

    public static void galleryAddPic(Context context, String mCurrentPhotoPath) {
        try {
            final String[] columns = { MediaStore.Images.Media._ID };
            Cursor imagecursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, null);
            Log.d("camera", "imagecursor:" + imagecursor);
            countLast = imagecursor.getCount();
            Log.d("camera", "count countLast:" + countLast);
            if (countLast == countFirst) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, mCurrentPhotoPath);
                context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Log.d("camera", "insertok:");
                Thread.sleep(500);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("camera", "insert  false");
        }
    }

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private static File createImageFile() throws IOException {
        // Create an image file name
        String url ="";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
        String imageFileName = "JPEG_" + timeStamp + "_";
        File photo = new File(Environment.getExternalStorageDirectory(),  imageFileName+".jpg");
        return photo;
    }
/*

    private static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
                albumF);
        return imageF;
    }
*/

    @SuppressLint("NewApi")
    public static File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {

            storageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "Camera");

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v("Camera", "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private static File setUpPhotoFile() throws IOException {

        File f = createImageFile();

        return f;
    }

    public static void removeTempImage() {
        if (mCaptureUri != null) {
            File mFile = new File(mCaptureUri.getPath());
            if (mFile.exists()) {
                mFile.delete();
            }
            mCaptureUri = null;
        }
    }

    public static void removeTempImage(String path) {
        if (mCaptureUri != null) {
            File mFile = new File(path);
            if (mFile.exists()) {
                mFile.delete();
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkPermissionForExternalStorage(Activity context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkPermissionForCamera(Activity context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    public  void onRequestPermissionsResult(Activity context, int requestCode, int[] grantResults){
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionForCamera(context)) {
                        openCamera(context);
                    }
                } else {
                    Toast.makeText(context, "You have denied permission to access external storage. Please go to settings and enable access to use this feature", Toast.LENGTH_LONG).show();
                }
                break;
            case CAMERA_PERMISSION_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera(context);
                } else {
                    Toast.makeText(context, "You have denied permission to access camera. Please go to settings and enable access to use this feature", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public static Bitmap getThumbnailFromURI(Context context, Uri videoUri){
        Bitmap bitmap=null;
        if (videoUri.getPath().toString().contains(".mp4")) {
             bitmap = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(), MediaStore.Images.Thumbnails.MICRO_KIND);
        } else {
            long videoID = getVideoId(videoUri, context);
             bitmap = getBitmapFromId(videoID, context);
        }
        return bitmap;
    }

    private static long getVideoId(Uri videoUri, Context context){

        long video_id= 0;
        try {
            final String[] projection = { MediaStore.Video.Media.DATA};
            //final String selection = MediaStore.Video.VideoColumns.BUCKET_ID + " = ?";
            //final String[] selectionArgs = { bucketid };
            //final String orderBy = MediaStore.Video.VideoColumns.DATE_TAKEN +" ASC, " + MediaStore.Video.VideoColumns._ID+ " ASC";
            //Uri videosuri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            final Cursor cursor = context.getContentResolver().query(videoUri,
                    null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {

                if (cursor.moveToFirst()) {
                    do {
                        video_id=cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
                        int dataColumnIndexthumb = cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA);

                        String videoPath = cursor.getString(dataColumnIndex);
                        String sdcardPaththumbvideo = cursor.getString(dataColumnIndexthumb);

                        File videoFile = new File(videoPath);
                        // videoThumbFile = new File(sdcardPaththumbvideo);

                    } while (cursor.moveToNext());
                }
            }
            else{

                Toast.makeText(context, "Doesn't exist any files in this directory.", Toast.LENGTH_LONG).show();
            }

            cursor.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
        return video_id;
    }

    private static Bitmap getBitmapFromId(long id, Context context){
        ContentResolver crThumb = context.getContentResolver();
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
        return curThumb;
    }

    public static File createThumbFile(Bitmap bitmap, Context context){
        //create a file to write bitmap data
        File f = new File(context.getCacheDir(), "thumb");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
}
