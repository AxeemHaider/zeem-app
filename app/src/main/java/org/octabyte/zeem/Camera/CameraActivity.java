package org.octabyte.zeem.Camera;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import org.octabyte.zeem.Camera.camfrags.Editing;
import org.octabyte.zeem.Camera.camfrags.Initializer;
import org.octabyte.zeem.Camera.camutil.FragmentCommunicator;
import org.octabyte.zeem.Camera.helper.GeneralUtils;
import org.octabyte.zeem.Camera.helper.SingleMediaScanner;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.Profile.RevealBackgroundView;
import org.octabyte.zeem.Publish.PublishActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.octabyte.zeem.Camera.CameraHandler.camMediaFile;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmp;

import static org.octabyte.zeem.Camera.camutil.CameraListener.activeBtn;
import static org.octabyte.zeem.Camera.camutil.CameraListener.btnFlow;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmpHeight;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmpWidth;
import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

public class CameraActivity extends AppCompatActivity implements FragmentCommunicator, RevealBackgroundView.OnStateChangeListener{

    public static final String TAG = "com.azeem.Debug";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private ProgressDialog progressDialog;

    private TextView writingText;
    private EditText writingEditText;

    private CameraDrawing cameraDrawing;

    public static Boolean isCameraEditingPanel = false;
    public static Boolean isCameraActivity = true;
    public static String camCurrentMedia = null;

    private Boolean isStatusMode;
    private String statusType = null;
    private Boolean openDirectGallery = false;
    public Boolean isGalleryOpened = false;
    private ImageView pictureView;

    // Animation to start this activity
    private static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";
    private RevealBackgroundView vRevealBackground;

    private String postMode;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] permissionsRequired = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    private SharedPreferences pref;

    public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity, Boolean isStatusMode){
        startCameraFromLocation(startingLocation, startingActivity, isStatusMode, null, null, false);
    }
    public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity,
                                               Boolean isStatusMode, String statusType,
                                               String statusModePublicPrivate, Boolean openDirectGallery) {
        //Log.i("StoryActivity", String.valueOf(storiesCount));
        Intent intent = new Intent(startingActivity, CameraActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra("ARG_IS_STATUS_MODE", isStatusMode);
        intent.putExtra("ARG_STATUS_TYPE", statusType);
        intent.putExtra("USER_STATUS_PRIVATE_PUBLIC_TYPE", statusModePublicPrivate);
        intent.putExtra("ARG_OPEN_DIRECT_GALLERY", openDirectGallery);
        if (isStatusMode) {
            startingActivity.startActivity(intent);
        }else{
            startingActivity.startActivityForResult(intent, 777);
        }
    }
    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }
    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            //vRevealBackground.setVisibility(View.GONE);
            if(openDirectGallery){
                loadCropImageFromGallery();
            }else {
                checkRuntimePermission();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        Long mUserId = pref.getLong("userId", 123L);

        isStatusMode = getIntent().getBooleanExtra("ARG_IS_STATUS_MODE", false);
        statusType = getIntent().getStringExtra("ARG_STATUS_TYPE");
        postMode = getIntent().getStringExtra("USER_STATUS_PRIVATE_PUBLIC_TYPE");

        try {
            openDirectGallery = getIntent().getBooleanExtra("ARG_OPEN_DIRECT_GALLERY", false);
        } catch (Exception e) {
            e.printStackTrace();
        }


        vRevealBackground = findViewById(R.id.vRevealBackground);
        setupRevealBackground(savedInstanceState);

        // Reset the Camera Listener Static variables
        activeBtn = "picture";
        btnFlow = "right";

        writingText = findViewById(R.id.writingText);
        writingEditText = findViewById(R.id.writingEditText);
        pictureView = findViewById(R.id.pictureView);
        cameraDrawing = findViewById(R.id.cameraDrawing);

        if(isStatusMode) {
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
        }

    }

    private void initCamera(){

        Fragment camInitializer = new Initializer();
        Bundle args = new Bundle();
        args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
        args.putBoolean("ARG_OPEN_DIRECT_GALLERY", openDirectGallery);
        args.putString("ARG_STATUS_TYPE", statusType);
        camInitializer.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.cameraLayout, camInitializer, "camInitializer");
        fragmentTransaction.commit();

    }

    private void loadCropImageFromGallery(){
        isGalleryOpened = true;

        CropImage.activity()
                .setAspectRatio(1,1)
                .setInitialCropWindowPaddingRatio(0)
                .start(this);
    }

    private void checkRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])){
                //Show Information about why you need the permission
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_camera_title);
                builder.setMessage(R.string.permission_camera_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CameraActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (pref.getBoolean("camera_permission",false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_camera_title);
                builder.setMessage(R.string.permission_camera_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("camera_permission",true);
            editor.apply();
        } else {
            initCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if(allgranted){

                initCamera();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])){

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_camera_title);
                builder.setMessage(R.string.permission_camera_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CameraActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Permissions are required!",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PERMISSION_SETTING) {
                if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {

                    //Got Permission
                    initCamera();

                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                isGalleryOpened = false;

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
            isGalleryOpened = false;
            Toast.makeText(CameraActivity.this, "Unable to crop your photo", Toast.LENGTH_SHORT).show();
        }// end if-else
    }

    @Override
    public void doAction(String action) {
        FragmentManager fragmentManager = getFragmentManager();
        Editing cameraEditing = (Editing) fragmentManager.findFragmentByTag("cameraEditing");

        if(action.equals("hideLayout")){
            cameraEditing.hideLayout();
        }else if(action.equals("showLayout")){
            cameraEditing.showLayout();
        }

    }

    /*
     * SaveImage function:
     * Process all data like drawing Emoji and captions on Canvas
     * combine canvas and image
     * save it in mobile device
     */

    public void saveImage(View view){
        view.setVisibility(View.INVISIBLE);

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                progressDialog = ProgressDialog.show(CameraActivity.this,"","Processing...", true);
                // First of all draw text on canvas
                if (!writingText.getText().toString().isEmpty()) {
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) writingText.getLayoutParams();
                    Rect bounds = new Rect();
                    Paint textPaint = writingText.getPaint();
                    textPaint.getTextBounds(writingText.getText().toString(),0,writingText.getText().toString().length(),bounds);
                    int textHeight = bounds.height();
                    int textMargin = lParams.topMargin + textHeight;
                    float textSize = writingText.getTextSize();
                    int textPadding = writingText.getPaddingTop();
                    ColorDrawable cd = (ColorDrawable) writingText.getBackground();
                    int bgColor = cd.getColor();
                    cameraDrawing.drawText(writingText.getText().toString(), textMargin, textSize, textPadding,bgColor, writingText.getCurrentTextColor());
                }

                // Now save the image
                cameraDrawing.setDrawingCacheEnabled(true);
                Bitmap drawingCache = cameraDrawing.getDrawingCache();
                ImageView pictureView = findViewById(R.id.pictureView);
                Bitmap pictureBitmap = ((BitmapDrawable)pictureView.getDrawable()).getBitmap();
                Bitmap cameraBitmap = Bitmap.createBitmap(pictureBitmap.getWidth(), pictureBitmap.getHeight(), pictureBitmap.getConfig());
                Bitmap drawingBitmap = Bitmap.createScaledBitmap(drawingCache, pictureBitmap.getWidth(), pictureBitmap.getHeight(), false);
                Log.i(TAG,"activity .. width: "+pictureBitmap.getWidth()+" height: "+pictureBitmap.getHeight());
                //Bitmap drawingBitmap = CameraUtils.resizeBitmap(drawingCache, pictureBitmap.getWidth(), pictureBitmap.getHeight());
                //Bitmap drawingBitmap = Bitmap.createScaledBitmap(drawingCache, pictureBitmap.getWidth(), pictureBitmap.getHeight(), false);
                Log.i(TAG,"drawingBitmap .. width: "+drawingBitmap.getWidth()+" height: "+drawingBitmap.getHeight());
                //Bitmap drawingBitmap = Bitmap.createScaledBitmap(drawingCache, pictureBitmap.getWidth(), pictureBitmap.getHeight(), false);

                Canvas canvas = new Canvas(cameraBitmap);
                canvas.drawBitmap(pictureBitmap, 0f, 0f, null);
                canvas.drawBitmap(drawingCache, 0f, 0f, null);
                pictureView.setImageBitmap(cameraBitmap);

                File pictureFile = GeneralUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }

                try {
                    new SingleMediaScanner(CameraActivity.this, pictureFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Need to clear thing set all things as default
                isCameraEditingPanel = false;
                isCameraActivity = true;
                originalBmp = null;
                //finish();
                //startActivity(getIntent());

                progressDialog.dismiss();

                String mediaType = "IMAGE";

                if (camCurrentMedia != null)
                    mediaType = camCurrentMedia;

                //Toast.makeText(CameraActivity.this, String.valueOf(Uri.fromFile(pictureFile)), Toast.LENGTH_LONG).show();
                if(isStatusMode){
                    if (statusType.equals("TALKING_PHOTO"))
                        mediaType = "TALKING_PHOTO";

                    PublishActivity.openWithMediaUri(CameraActivity.this, Uri.fromFile(pictureFile), mediaType, postMode);
                }else {
                    createNewStory(pictureFile.getPath(), mediaType);
                }

            }
        };
        handler.post(r);

    }

    public void saveMedia(View v){

        String mediaType = "VIDEO";

        if (camCurrentMedia != null) {
            if (camCurrentMedia.equals("GIF"))
                mediaType = "GIF";
        }

        if(isStatusMode){
            PublishActivity.openWithMediaUri(CameraActivity.this, Uri.fromFile(camMediaFile), mediaType, postMode);
        }else {
            createNewStory(camMediaFile.getPath(), mediaType);
        }
    }


    private void createNewStory(String localFilePath, String mediaType){

        Toast.makeText(this, "Story Created", Toast.LENGTH_SHORT).show();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("ARG_CREATE_NEW_STORY", true);
        returnIntent.putExtra("ARG_STORY_TYPE", mediaType);
        returnIntent.putExtra("ARG_STORY_MEDIA_PATH", localFilePath);
        setResult(CameraActivity.RESULT_OK, returnIntent);
        finish();

    }

    private void gotoCameraEditing(){
        Fragment cameraEditing = new Editing();
        Bundle args = new Bundle();
        args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
        cameraEditing.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.cameraLayout, cameraEditing,"cameraEditing");
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (openDirectGallery && isGalleryOpened){
                isGalleryOpened = false;
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(isCameraEditingPanel){
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("Discard Photo ?");
            newDialog.setMessage("If you go back now, you will lose your photo and drawing.");
            newDialog.setPositiveButton("Discard", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    isCameraEditingPanel = false;
                    isCameraActivity = true;
                    originalBmp = null;
                    finish();
                    startActivity(getIntent());
                }
            });
            newDialog.setNegativeButton("Keep", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        }else if(!isCameraActivity){
            writingEditText.setVisibility(View.GONE);
            gotoCameraEditing();
            isCameraEditingPanel = true;
        }else if(camCurrentMedia != null){
            camCurrentMedia = null;
            finish();
            startActivity(getIntent());
        }else{
            super.onBackPressed();
        }
    }


}
