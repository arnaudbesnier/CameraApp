package com.camera.app;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraApp";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mPreviewHeight;
    private int mPreviewWidth;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Screen ratio
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Float screenRation = (float) display.getHeight() / (float) display.getWidth();
        Log.d(TAG, "Screen Size: "  + display.getHeight() + "x" +  display.getWidth());
        Log.d(TAG, "Screen Ratio: " + screenRation);

        // Picture ratio
        Camera.Parameters parameters;
        parameters = mCamera.getParameters();
        Camera.Size pictureSize = parameters.getPictureSize();
        Float pictureRatio = (float) pictureSize.width / (float) pictureSize.height;
        Log.d(TAG, "Picture Size: "  + pictureSize.width + "x" +  pictureSize.height);
        Log.d(TAG, "Picture Ratio: " + pictureRatio);

        mPreviewWidth = display.getWidth();
        mPreviewHeight = (int) (pictureRatio * mPreviewWidth);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private static Camera.Size getOptimalPictureSize(List<Camera.Size> sizes) {
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        long resolution = 0;

        for (Camera.Size size : sizes) {
            if (size.width * size.height > resolution) {
                optimalSize = size;
                resolution = size.width * size.height;
            }
        }
        return optimalSize;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            (this).layout(0, 0, mPreviewWidth, mPreviewHeight);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters;
            parameters = mCamera.getParameters();
            parameters.set("orientation", "portrait");
            parameters.setRotation(90);

            List<Camera.Size> pictSizes = parameters.getSupportedPictureSizes();
            Camera.Size pictureSize = getOptimalPictureSize(pictSizes);
            if (pictureSize != null) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
            }
            mCamera.setParameters(parameters);

            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
