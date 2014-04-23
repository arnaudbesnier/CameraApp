package com.camera.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity {

    private static final String TAG = "CameraApp";
    public static final int MEDIA_TYPE_IMAGE = 1;

    private Camera mCamera;
    private CameraPreview mPreview;

    private boolean checkCameraHardware(Context context) {
         if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
             return true;
         } else {
             return false;
         }
    }

    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();
        }
        catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return camera;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkCameraHardware(this)) {
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera);

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            preview.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.autoFocus(mAutoFocusCallback);
                    }
                }
            );

            Button captureButton = (Button) findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Take picture", Toast.LENGTH_SHORT).show();
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
            );

            Button restartButton = (Button) findViewById(R.id.button_restart);
            restartButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Restart the preview", Toast.LENGTH_SHORT).show();
                        mCamera.startPreview();
                    }
                }
            );
        }
    }

    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback(){
        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {}
    };

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Toast.makeText(getApplicationContext(), "File saved", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
}
