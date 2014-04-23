package com.camera.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;


public class MainActivity extends Activity {

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
            // Create an instance of Camera
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }
}
