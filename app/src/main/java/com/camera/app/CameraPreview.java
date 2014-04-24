package com.camera.app;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraApp";

    private SurfaceHolder mHolder;
    private Camera mCamera;

    private static List<Camera.Size> mSupportedPreviewSizes;
    private static Camera.Size mOptimalPreviewSize;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        final double MAX_DOWNSIZE = 1.5;
        double targetRatio = (double)h / w;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            double downsize = (double) size.width / w;
            if (downsize > MAX_DOWNSIZE) {
                //if the preview is a lot larger than our display surface ignore it
                //reason - on some phones there is not enough heap available to show the larger preview sizes
                continue;
            }
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                double downsize = (double) size.width / w;
                if (downsize > MAX_DOWNSIZE) {
                    continue;
                }
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        //everything else failed, just take the closest match
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private static Camera.Size getOptimalPictureSize(List<Camera.Size> sizes)
    {
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mOptimalPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters;
            parameters = mCamera.getParameters();
            parameters.set("orientation", "portrait");
            parameters.setRotation(90);
            parameters.setPreviewSize(mOptimalPreviewSize.width, mOptimalPreviewSize.height);

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
