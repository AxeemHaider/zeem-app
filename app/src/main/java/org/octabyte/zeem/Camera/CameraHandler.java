package org.octabyte.zeem.Camera;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.octabyte.zeem.Camera.helper.GeneralUtils;
import org.octabyte.zeem.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.octabyte.zeem.Camera.CameraActivity.MEDIA_TYPE_VIDEO;

/**
 * Created by Azeem on 6/19/2017.
 */

public class CameraHandler extends SurfaceView implements SurfaceHolder.Callback {

    // initializing Variables
    private static final String TAG = "com.azeem.Debug";
    private Context mContext;
    private SurfaceHolder mSurfaceHolder;

    // Preview Sizes and orientation
    private Display mDisplay;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private static final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

    // Camera variables
    public Camera mCamera;
    private static final int NO_CAMERA = -1;
    public boolean mIsFrontCamera = false;
    private boolean mIsFlashOn = false;
    private int mFrontCameraId, mBackCameraId;
    public static File camMediaFile;

    // Video Variables
    private MediaRecorder mMediaRecorder;
    private int videoOrientation;

    public CameraHandler(Context context) {
        super(context);
        mContext = context;
        setup();
    }

    // Setup camera
    private void setup(){
        //Log.i(TAG, "setup");
        if(hasCamera()) {

            mSurfaceHolder = getHolder();
            mSurfaceHolder.addCallback(this);

            mBackCameraId = findCameraId(false);

            if(hasFrontCamera()){
                mFrontCameraId = findCameraId(true);
            }else{
                mFrontCameraId = NO_CAMERA; // Front Face Camera Not found on device
            }

            // System Service to get window display
            final WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            mDisplay = windowManager.getDefaultDisplay();

            // Retrieving camera info
            // mCameraInfo = ####### Need to check this #######

        } // Else Camera not found on device
    }

    private void setCamera(){
        //Log.i(TAG, "setCamera");
        if(hasCamera()){
            try {
                mCamera = Camera.open(mIsFrontCamera ? mFrontCameraId : mBackCameraId);
                //mCamera = Camera.open(mFrontCameraId);

                videoOrientation = mIsFrontCamera ? 270 : 90;

                // Set orientation of camera
                int degrees = 0;
                switch (mDisplay.getRotation()) {
                    case Surface.ROTATION_0: degrees = 0; break;
                    case Surface.ROTATION_90: degrees = 90; break;
                    case Surface.ROTATION_180: degrees = 180; break;
                    case Surface.ROTATION_270: degrees = 270; break;
                }
                //Log.i(TAG,"degrees = "+degrees);
                int result;
                if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = (mCameraInfo.orientation + degrees) % 360;
                    result = (360 - result) % 360;  // compensate the mirror
                } else {  // back-facing
                    result = (mCameraInfo.orientation - degrees + 360) % 360;
                }
                Log.i(TAG,"video Orientation = "+videoOrientation);
                Log.i(TAG,"Orientation = "+result);
                mCamera.setDisplayOrientation(result);
                //setDisplayOrientation(mCamera, result);

                // Set Auto focus mode for camera
                Camera.Parameters params = mCamera.getParameters();
                List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    //Log.i(TAG,"Setting auotfocus");
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.setParameters(params);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                openNativeCameraMessage(R.string.native_camera_msg_fail_to_connect_camera);
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            focusOnTouch(event);
        }
        return true;
    }

    private void focusOnTouch(MotionEvent event){

        if (mCamera != null) {
            mCamera.cancelAutoFocus();

            Rect focusRect = calculateFocusArea(event.getX(), event.getY());

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getFocusMode().equals(
                    Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> mylist = new ArrayList<>();
                mylist.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(mylist);
            }

            try {
                mCamera.cancelAutoFocus();
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (camera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            Camera.Parameters parameters = camera.getParameters();
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                            if (parameters.getMaxNumFocusAreas() > 0) {
                                parameters.setFocusAreas(null);
                            }
                            camera.setParameters(parameters);
                            camera.startPreview();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static  final int FOCUS_AREA_SIZE= 300;

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / this.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / this.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    private void start(){
        //Log.i(TAG, "start");
        if(mCamera != null){
            try {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                // Setting the optimal preview for best result
                if(mPreviewSize != null) {
                    //Log.i(TAG, "Activate Optimal Preview");
                    try {
                        Camera.Parameters parameters = mCamera.getParameters();
                        if (parameters != null) {
                            Log.i(TAG,"preview .. width: "+mPreviewSize.width+ " height:" + mPreviewSize.height);
                            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                            mCamera.setParameters(parameters);
                            mCamera.startPreview();
                        }
                    } catch (RuntimeException e) {

                        Camera.Parameters parameters = mCamera.getParameters();
                        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
                        Camera.Size previewSize = previewSizes.get(0);
                        parameters.setPreviewSize(previewSize.width, previewSize.height);
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();

                        openNativeCameraMessage(R.string.native_camera_msg_when_optimal_preview);

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e){
                openNativeCameraMessage(R.string.native_camera_msg_when_preview_not_show);
            }
        }
    }

    private void openNativeCameraMessage(int messageId){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Use External camera");
            builder.setMessage(messageId);
            builder.setPositiveButton("External Camera", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    getContext().startActivity(intent);

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to setup camera! Select pic from gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void stop(){
        //Log.i(TAG, "stop");
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void switchCamera(){
        //Log.i(TAG, "switchCamera");
        if(mFrontCameraId != NO_CAMERA){
            mIsFrontCamera = !mIsFrontCamera;
            stop();
            setCamera();
            start();
        }
    }

    public void onOffFlash(){
        try {
            mIsFlashOn = ! mIsFlashOn;
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(mIsFlashOn ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
        } catch (RuntimeException e) {
            Toast.makeText(getContext(), "Flash light not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.i(TAG, "surfaceCreated");
        setCamera();
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Log.i(TAG, "surfaceChanged : width = "+width +" height = "+height);
        if (mSupportedPreviewSizes != null) {
            //Log.i(TAG, "mSupportedPreviewSizes");

            // Get Optimal Preview Size
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);

            // Setting the optimal preview for best result
            if(mPreviewSize != null) {
                //Log.i(TAG, "Activate Optimal Preview");
                try {
                    Camera.Parameters parameters = mCamera.getParameters();
                    Log.i(TAG,"preview .. width: "+mPreviewSize.width+ " height:" + mPreviewSize.height);
                    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                } catch (RuntimeException e) {

                    Camera.Parameters parameters = mCamera.getParameters();
                    List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
                    Camera.Size previewSize = previewSizes.get(0);
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();

                    Toast.makeText(getContext(), "Unable to get optimal size", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Log.i(TAG, "surfaceDestroyed");
        stop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //Log.i(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        //Log.i(TAG, "============Supported Preview Sizes==============");
        for (Camera.Size size : sizes) {
            //Log.i(TAG, "width = "+size.width+" height = "+size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }
        //Log.i(TAG, "============Supported Preview Sizes==============");

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        //Log.i(TAG, "width = "+ optimalSize.width +" height = "+ optimalSize.height);
        return optimalSize;
    }

    // Some Camera Features That doesn't need to be changes
    private boolean hasCamera() {
        final PackageManager packageManager = mContext.getPackageManager();
        return packageManager != null
                && packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private boolean hasFrontCamera() {
        final PackageManager packageManager = mContext.getPackageManager();
        return packageManager != null
                && packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    private int findCameraId(boolean front) {
        final int cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if ((front && mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    || (!front && mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)) {
                return i;
            }
        }
        return NO_CAMERA;
    }

    // For Gif Recording
    private boolean prepareGifRecorder(){

        camMediaFile = GeneralUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        //mCamera = getCameraInstance();
        //mCamera = Camera.open();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        /*CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_720P);
        mMediaRecorder.setCaptureRate(profile.videoFrameRate/6.0f);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mMediaRecorder.setProfile(profile);*/
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(String.valueOf(camMediaFile));
        mMediaRecorder.setMaxDuration(5000);
        mMediaRecorder.setCaptureRate(profile.videoFrameRate/6.0f);
        mMediaRecorder.setVideoFrameRate(10);

        mMediaRecorder.setOrientationHint(videoOrientation);
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(this.getHolder().getSurface());


        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    // For Gif Recording
    private boolean secondGifRecorder(){

        camMediaFile = GeneralUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        //mCamera = getCameraInstance();
        //mCamera = Camera.open();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        /*CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_720P);
        mMediaRecorder.setCaptureRate(profile.videoFrameRate/6.0f);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mMediaRecorder.setProfile(profile);*/
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(String.valueOf(camMediaFile));
        mMediaRecorder.setMaxDuration(5000);
        mMediaRecorder.setCaptureRate(20);
        mMediaRecorder.setVideoFrameRate(20);

        mMediaRecorder.setOrientationHint(videoOrientation);
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(this.getHolder().getSurface());


        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    private boolean fallBackGifRecorder(){

        camMediaFile = GeneralUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        //mCamera = getCameraInstance();
        //mCamera = Camera.open();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        /*CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_720P);
        mMediaRecorder.setCaptureRate(profile.videoFrameRate/6.0f);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mMediaRecorder.setProfile(profile);*/
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(String.valueOf(camMediaFile));
        mMediaRecorder.setMaxDuration(5000);
        mMediaRecorder.setCaptureRate(profile.videoFrameRate/3.0f);
        mMediaRecorder.setVideoFrameRate(5);

        mMediaRecorder.setOrientationHint(videoOrientation);
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(this.getHolder().getSurface());


        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    private void startFallBackGifRecorder(){
        releaseMediaRecorder();
        if (fallBackGifRecorder()){
            try {
                mMediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Toast.makeText(mContext,"Your camera is not supporting GIF", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void startSecondGifRecorder(){
        releaseMediaRecorder();
        if (secondGifRecorder()){
            try {
                mMediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                startFallBackGifRecorder();
            }
        }
    }

    public void startGifRecording(){
        if (prepareGifRecorder()) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            try {
                mMediaRecorder.start();
            } catch (RuntimeException e) {
                e.printStackTrace();
                startSecondGifRecorder();
            }

        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            // inform user
        }
    }

    // For video Recording
    private boolean prepareVideoRecorder(){
        camMediaFile = GeneralUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        //mCamera = getCameraInstance();
        //mCamera = Camera.open();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setMaxFileSize(23000000);
        mMediaRecorder.setMaxDuration(30000);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(String.valueOf(camMediaFile));
        mMediaRecorder.setOrientationHint(videoOrientation);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(this.getHolder().getSurface());


        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    public void startRecording(){
        if (prepareVideoRecorder()) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            mMediaRecorder.start();

        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            // inform user
        }
    }

    public void stopRecording(){
            // stop recording and release camera
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
        }

    }

    public Camera.Size getmPreviewSize() {
        return mPreviewSize;
    }
}