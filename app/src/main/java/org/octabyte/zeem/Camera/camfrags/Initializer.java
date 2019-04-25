package org.octabyte.zeem.Camera.camfrags;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.octabyte.zeem.Camera.CameraDrawing;
import org.octabyte.zeem.Camera.CameraHandler;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Camera.camutil.CameraListener;
import org.octabyte.zeem.Camera.camutil.CameraUtils;
import org.octabyte.zeem.Utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static org.octabyte.zeem.Camera.CameraActivity.camCurrentMedia;
import static org.octabyte.zeem.Camera.CameraHandler.camMediaFile;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmp;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmpWidth;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmpHeight;

import static org.octabyte.zeem.Camera.camutil.CameraListener.activeBtn;
import static org.octabyte.zeem.Camera.camutil.CameraListener.btnFlow;
import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 6/19/2017.
 */

public class Initializer extends Fragment implements View.OnClickListener {

    private Context context;

    private ImageView btnCamSwitch;
    private ImageView btnCapturePicture;
    private ImageView btnCaptureVideo;
    private ImageView btnCaptureGif;
    private ImageView btnCamFlash;
    private ImageView pictureView;
    private ImageView btnImageGallery;
    private ImageView btnVideoStop;
    private ImageView btnGifStop;
    private ImageView ivExternalCamera;

    private FrameLayout cameraPreview;
    private CameraHandler cameraHandler;
    //CameraDrawing cameraDrawing;

    private Boolean camFlash = false;
    private int pictureRotation = 90;

    private RelativeLayout cameraLayout;
    private RelativeLayout holdCameraButtons;

    private int pStatus = 0;
    private Handler handler = new Handler();
    private ProgressBar mProgress;
    private int progressDuration;

    private final String TAG = "com.azeem.Debug";
    private final int RESULT_LOAD_IMAGE = 137;
    private final int RESULT_LOAD_VIDEO = 111;

    private ProgressDialog progressDialog;

    private Boolean isGifVideo = false;

    private Boolean isStatusMode;
    private String statusType = null;
    private int originalPictureViewWidth, originalPictureViewHeight;

    public Initializer() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("CameraLife", "onCreate");
        isStatusMode = getArguments().getBoolean("ARG_IS_STATUS_MODE");
        statusType = getArguments().getString("ARG_STATUS_TYPE");

    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_cam_initializer, container, false);

        context = view.getContext();
        Log.i("CameraLife", "onCreateView");

        Log.i(TAG, "onCreateView");
        btnCapturePicture = view.findViewById(R.id.btnCapturePicture);
        btnCaptureVideo = view.findViewById(R.id.btnCaptureVideo);
        btnVideoStop = view.findViewById(R.id.btnVideoStop);
            btnVideoStop.setOnClickListener(this);
        btnGifStop = view.findViewById(R.id.btnGifStop);
            btnGifStop.setOnClickListener(this);
        btnCaptureGif = view.findViewById(R.id.btnCaptureGif);
        btnCamSwitch = view.findViewById(R.id.btnCamSwitch);
        btnCamFlash = view.findViewById(R.id.btnCamFlash);
        btnImageGallery = view.findViewById(R.id.btnImageGallery);
            btnImageGallery.setOnClickListener(this);
        ivExternalCamera = view.findViewById(R.id.ivExternalCamera);
        ivExternalCamera.setOnClickListener(this);


        mProgress = view.findViewById(R.id.cameraProgressbar);

        if(btnCapturePicture != null) {
            Drawable background = btnCapturePicture.getBackground();
            if (background instanceof ShapeDrawable) {
                // cast to 'ShapeDrawable'
                ShapeDrawable shapeDrawable = (ShapeDrawable) background;
                shapeDrawable.getPaint().setColor(Color.BLUE);
            } else if (background instanceof GradientDrawable) {
                // cast to 'GradientDrawable'
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                gradientDrawable.setColor(Color.BLUE);
            } else if (background instanceof ColorDrawable) {
                // alpha value may need to be set again after this call
                ColorDrawable colorDrawable = (ColorDrawable) background;
                colorDrawable.setColor(Color.BLUE);
            }
        }

        if (statusType != null){
            CameraListener cameraListener = new CameraListener(context, btnCapturePicture, btnCaptureVideo, btnCaptureGif);
            if (statusType.equals("VIDEO")){
                btnImageGallery.setImageResource(R.drawable.ic_video_gallery);
                cameraListener.activateVideoBtn();
                btnFlow = "middle";
                activeBtn = "video";
            }else if (statusType.equals("GIF")){
                btnImageGallery.setVisibility(View.GONE);
                cameraListener.activateDirectGifBtn();
                btnFlow = "left";
                activeBtn = "gif";
                //camCurrentMedia = "GIF";
            }else if (statusType.equals("TALKING_PHOTO")){
                btnCaptureVideo.setVisibility(View.GONE);
                btnCaptureGif.setVisibility(View.GONE);
            }
        }

        holdCameraButtons = view.findViewById(R.id.holdCameraButtons);
        holdCameraButtons.setOnTouchListener(new CameraListener(context, btnCapturePicture, btnCaptureVideo, btnCaptureGif));

        btnCamSwitch.setOnClickListener(this);
        btnCamFlash.setOnClickListener(this);

        btnCapturePicture.setOnTouchListener(new CameraListener(context, btnCapturePicture, btnCaptureVideo, btnCaptureGif){
            @Override
            public void clicked() {
                try {
                    if(pictureView != null)
                        cameraHandler.mCamera.takePicture(shutterCallback, null, pictureCallback);
                    else
                        Toast.makeText(context,"Wait! setup camera", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnCaptureVideo.setOnTouchListener(new CameraListener(context, btnCapturePicture, btnCaptureVideo, btnCaptureGif){
            @Override
            public void clicked() {
                    progressDuration = 300;
                    cameraHandler.startRecording();
                    btnCaptureVideo.setVisibility(View.INVISIBLE);
                    btnVideoStop.setVisibility(View.VISIBLE);
                startProgressBar();
            }
        });
        btnCaptureGif.setOnTouchListener(new CameraListener(context, btnCapturePicture, btnCaptureVideo, btnCaptureGif){
            @Override
            public void clicked() {
                mProgress.setProgressDrawable(getResources().getDrawable(R.drawable.circular_blue));
                isGifVideo = true;
                progressDuration = 50;
                cameraHandler.startGifRecording();
                btnCaptureGif.setVisibility(View.INVISIBLE);
                btnGifStop.setVisibility(View.VISIBLE);
                startProgressBar();
            }
        });


        cameraHandler = new CameraHandler(context);
        cameraPreview = view.findViewById(R.id.cameraPreview);

        /*if(isStatusMode) {
            cameraPreview.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams mParams;
                    mParams = (RelativeLayout.LayoutParams) cameraPreview.getLayoutParams();
                    mParams.setMargins(0, Utils.dpToPx(70), 0, 0);
                    mParams.height = cameraPreview.getWidth();
                    cameraPreview.setLayoutParams(mParams);
                    cameraPreview.postInvalidate();
                }
            });
        }*/


        cameraPreview.addView(cameraHandler,0);

        return view;
    }

    private void showCameraInfoFirstTime(){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        boolean firstTime = sharedPreferences.getBoolean("ExternalCameraFirstTime", true);

        if (firstTime) {
            new ShowcaseView.Builder(getActivity())
                    .setTarget(new ViewTarget(R.id.ivExternalCamera, getActivity()))
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(getString(R.string.info_external_camrea_title))
                    .setContentText(getString(R.string.info_external_camera_msg))
                    .replaceEndButton(R.layout.show_case_hide_button)
                    .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("ExternalCameraFirstTime", false);
                            editor.apply();
                        }
                    })
                    .build();
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("CameraLife", "onActivityCreated");
        Activity mActivity = getActivity();
        pictureView = mActivity.findViewById(R.id.pictureView);

        originalPictureViewWidth = pictureView.getWidth();
        originalPictureViewHeight = pictureView.getHeight();

        /*if(isStatusMode) {
            pictureView.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams mParams;
                    mParams = (RelativeLayout.LayoutParams) pictureView.getLayoutParams();
                    mParams.height = pictureView.getWidth();
                    pictureView.setLayoutParams(mParams);
                    pictureView.postInvalidate();
                }
            });
        }*/
        cameraLayout = mActivity.findViewById(R.id.cameraLayout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showCameraInfoFirstTime();
            }
        }, 500);
    }

    private void capturePicture(Bitmap bitmap){
        Log.i(TAG,"Picture bitmap width:"+bitmap.getWidth() +" height: "+bitmap.getHeight());
        //cameraDrawing.setLayoutParams(new ViewGroup.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
        progressDialog.dismiss();

        if (isStatusMode){
            File mFolder = new File(context.getExternalFilesDir(null), "temp");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            Long timeStamp = System.currentTimeMillis();
            String s = timeStamp + ".jpg";

            File f = new File(mFolder.getAbsolutePath(), s);

            FileOutputStream out = null;
            String filename = f.getAbsolutePath();
            try {
                out = new FileOutputStream(filename);

                // write the compressed bitmap at the destination specified by filename.
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Uri imageUri = Uri.fromFile(f);

            CropImage.activity(imageUri)
                    .setScaleType(CropImageView.ScaleType.CENTER_CROP)
                    .setAspectRatio(1, 1)
                    .setInitialCropWindowPaddingRatio(0)
                    .start(getActivity().getApplicationContext(), this);

        }else{
            pictureView.setImageBitmap(bitmap);
            pictureView.setVisibility(View.VISIBLE);
            gotoCameraEditing();
        }

    }

    private void stopRecording(){
        pStatus = 101;
        cameraHandler.stopRecording();
        if(isGifVideo){
            camCurrentMedia = "GIF";
        }else{
            camCurrentMedia = "VIDEO";
        }
        gotoCamVideo();
    }
    private void gotoCamVideo(){
        Fragment cameraVideo = new Video();
        Bundle args = new Bundle();
        args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
        cameraVideo.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.cameraLayout, cameraVideo,"cameraVideo");
        fragmentTransaction.commit();
    }

    private void switchCamera(){
        cameraHandler.switchCamera();
    }

    private void cameraFlash(){
        camFlash = !camFlash;
        if(camFlash)
            btnCamFlash.setImageResource(R.drawable.flash_on);
        else
            btnCamFlash.setImageResource(R.drawable.flash_off);

        cameraHandler.onOffFlash();
    }

    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "int .... pictureWidth: "+pictureView.getWidth() +" pictureHeight: "+pictureView.getHeight());

                if(cameraHandler.mIsFrontCamera)
                    capturePicture(CameraUtils.bitmapFromRawBytes(data, originalPictureViewWidth, originalPictureViewHeight, true) );
                else
                    capturePicture(CameraUtils.bitmapFromRawBytes(data, originalPictureViewWidth, originalPictureViewHeight, false) );

        }
    };
    private final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            progressDialog = ProgressDialog.show(context,"","Processing...", true);
        }
    };


    private void hideButtons(Boolean isVideo){
        btnImageGallery.setVisibility(View.INVISIBLE);
        btnCapturePicture.setVisibility(View.INVISIBLE);
        if(isVideo)
            btnCaptureGif.setVisibility(View.INVISIBLE);
        else
            btnCaptureVideo.setVisibility(View.INVISIBLE);
    }

    private void startProgressBar(){
        Resources res = getResources();
        //Drawable drawable = res.getDrawable(R.drawable.circular);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setProgress(0);   // Main Progress
        mProgress.setSecondaryProgress(100); // Secondary Progress
        mProgress.setMax(100); // Maximum Progress
        //mProgress.setProgressDrawable(drawable);

        new Thread( new Runnable() {

            @Override
            public void run() {
                while (pStatus < 100) {
                    pStatus += 1;

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.setProgress(pStatus);

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(progressDuration); //thread will take approx 3 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (pStatus == 100) {
                    stopRecording();
                }
            }
        }).start();
    }


    private void loadImageGallery(){
        /*Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);*/
        getExternalCamera();
    }

    private void loadCropImageFromGallery(){

        CropImage.activity()
                .setAspectRatio(1,1)
                .setInitialCropWindowPaddingRatio(0)
                .start(getActivity().getApplicationContext(), this);
    }

    private void getExternalCamera(){
        if (isStatusMode) {
            CropImage.activity()
                    .setCropMenuCropButtonTitle("Done")
                    .setAspectRatio(1,1)
                    .setInitialCropWindowPaddingRatio(0)
                    .start(getActivity().getApplicationContext(), this);
        } else {
            CropImage.activity()
                    .setCropMenuCropButtonTitle("Done")
                    .setInitialCropWindowPaddingRatio(0)
                    .start(getActivity().getApplicationContext(), this);
        }
    }

    private void pickVideoFromGallery(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == RESULT_LOAD_IMAGE ) {
                try {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = context.getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImage.getPath(), options);
                    originalBmp = ThumbnailUtils.extractThumbnail(bitmap, pictureView.getWidth(), pictureView.getHeight(), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    originalBmpWidth = originalBmp.getWidth();
                    originalBmpHeight = originalBmp.getHeight();

                    Bitmap bitmap2 = BitmapFactory.decodeFile(picturePath);
                    pictureView.setImageBitmap(bitmap2);
                    pictureView.setVisibility(View.VISIBLE);
                    gotoCameraEditing();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "Unable to get Media file", Toast.LENGTH_SHORT).show();
                }
            }else if(requestCode == RESULT_LOAD_VIDEO ){
                try {
                    Uri selectedVideo = data.getData();
                    String[] filePathColumn = { MediaStore.Video.Media.DATA };
                    Cursor cursor = context.getContentResolver().query(selectedVideo,filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String videoPath = cursor.getString(columnIndex);
                    cursor.close();
                    File videoFile = new File(videoPath);
                    long fileSize = (videoFile.length()/1024) / 1024;
                    if (fileSize < 25){
                        camCurrentMedia = "VIDEO";
                        camMediaFile = videoFile;
                        gotoCamVideo();
                    }else {
                        Toast.makeText(context, "File size must be less than 25 Mb", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "Unable to get Media file", Toast.LENGTH_SHORT).show();
                }

            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                Bitmap bitmap = BitmapFactory.decodeFile(resultUri.getPath(), options);
                originalBmp = ThumbnailUtils.extractThumbnail(bitmap, pictureView.getWidth(), pictureView.getHeight(), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                originalBmpWidth = originalBmp.getWidth();
                originalBmpHeight = originalBmp.getHeight();

                Bitmap bitmap2 = BitmapFactory.decodeFile(resultUri.getPath());
                pictureView.setImageBitmap(bitmap2);
                pictureView.setVisibility(View.VISIBLE);
                gotoCameraEditing();
            }

        }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(getActivity().getApplicationContext(), "Unable to crop your photo", Toast.LENGTH_SHORT).show();
        }// end if-else
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCamSwitch:
                switchCamera();
                break;
            case R.id.btnCamFlash:
                cameraFlash();
                break;
            case R.id.btnVideoStop:
                stopRecording();
                break;
            case R.id.btnGifStop:
                stopRecording();
                break;
            case R.id.btnImageGallery:

                if (statusType != null) {
                    switch (statusType) {
                        case "IMAGE":
                            //loadImageGallery();
                            loadCropImageFromGallery();
                            break;
                        case "VIDEO":
                            pickVideoFromGallery();
                            break;
                        default:
                            Toast.makeText(context, "You can't pick from gallery", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }else{
                    if (activeBtn.equals("video")){
                        pickVideoFromGallery();
                    }else{
                        loadImageGallery();
                    }
                }

                break;
            case R.id.ivExternalCamera:
                getExternalCamera();
                break;
        }
    }
    private void gotoCameraEditing(){
        try {
            Fragment cameraEditing = new Editing();
            Bundle args = new Bundle();
            args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
            cameraEditing.setArguments(args);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.cameraLayout, cameraEditing,"cameraEditing");
            //fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } catch (IllegalStateException e) {
            e.printStackTrace();

            Toast.makeText(getActivity().getApplicationContext(), "Illegal State Exception, Try again!", Toast.LENGTH_SHORT).show();
        }
    }
}
