package org.octabyte.zeem.Publish;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.octabyte.zeem.Camera.CameraActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.BottomNavigationSetup;
import org.octabyte.zeem.Utils.FocusView;
import org.octabyte.zeem.Utils.Utils;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/18/2017.
 */

public class SelectStatusActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    private int drawingStartLocation;

    private RelativeLayout lyRoot, rlText ,rlAudio, rlGIF, rlCamera, rlPhoto, rlTalkingPhoto;
    private BottomNavigationView bottomNavigation;

    private String postMode;
    private SharedPreferences sharedPreferences;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] audioPermission = new String[]{Manifest.permission.RECORD_AUDIO};
    private boolean audioPermissionGranted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_status);
        initVariable();

        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            lyRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    lyRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });
        }

        postMode = getIntent().getStringExtra("ARG_NAV_SENDER_MODE_TYPE");

        BottomNavigationSetup.init(bottomNavigation, SelectStatusActivity.this, 3, postMode);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showUserInfoFirstTime();
            }
        }, 1000);

        if(ActivityCompat.checkSelfPermission(this, audioPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            audioPermissionGranted = true;
        }

    }

    private void initVariable(){

        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        rlText = findViewById(R.id.rlText);
        rlAudio = findViewById(R.id.rlAudio);
        rlGIF = findViewById(R.id.rlGIF);
        rlCamera = findViewById(R.id.rlCamera);
        rlPhoto = findViewById(R.id.rlPhoto);
        rlTalkingPhoto = findViewById(R.id.rlTalkingPhoto);

        lyRoot = findViewById(R.id.root);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        rlText.setOnClickListener(this);
        rlAudio.setOnClickListener(this);
        rlGIF.setOnClickListener(this);
        rlCamera.setOnClickListener(this);
        rlPhoto.setOnClickListener(this);
        rlTalkingPhoto.setOnClickListener(this);
    }

    private void showUserInfoFirstTime(){
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        boolean firstTime = sharedPreferences.getBoolean("StatusFirstTime", true);

        if (firstTime) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(R.id.ivTalkingPhoto, this))
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(R.string.info_select_status_talking_title)
                    .setContentText(R.string.info_select_status_talking_detail)
                    .replaceEndButton(R.layout.show_case_hide_button)
                    .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            infoAudioFirstTime();
                        }
                    })
                    .build();
        }

    }
    private void infoAudioFirstTime(){
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.ivAudio, this))
                .withHoloShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.info_select_status_audio_title)
                .setContentText(R.string.info_select_status_audio_detail)
                .replaceEndButton(R.layout.show_case_hide_button)
                .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("StatusFirstTime", false);
                        editor.apply();
                    }
                })
                .build();
    }


    private void publishStatus(View v, String statusType){
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        PublishActivity.startStatusPublishFromLocation(startingLocation, this, statusType, postMode);
        overridePendingTransition(0, 0);
    }
    private void publishMediaStatus(View v, String statusType){
        publishMediaStatus(v, statusType, false);
    }
    private void publishMediaStatus(View v, String statusType, Boolean openDirectGallery){
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        CameraActivity.startCameraFromLocation(startingLocation, this, true, statusType, postMode, openDirectGallery);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rlCamera:
                // start video from camera
                publishMediaStatus(v, "IMAGE");
                break;
            case R.id.rlPhoto:
                // start camera for photo
                publishMediaStatus(v, "IMAGE", true);
                break;
            case R.id.rlText:
                publishStatus(v, "CARD");
                break;
            case R.id.rlAudio:
                if (audioPermissionGranted) {
                    publishStatus(v, "AUDIO");
                }else{
                    checkRuntimePermission();
                }
                break;
            case R.id.rlGIF:
                publishMediaStatus(v, "GIF");
                break;
            case R.id.rlTalkingPhoto:
                publishMediaStatus(v, "TALKING_PHOTO");
                break;
        }
    }

    private void checkRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, audioPermission[0]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,audioPermission[0])){
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_audio_title);
                builder.setMessage(R.string.permission_audio_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SelectStatusActivity.this,audioPermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (sharedPreferences.getBoolean("audio_permission",false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_audio_title);
                builder.setMessage(R.string.permission_audio_msg);
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
                ActivityCompat.requestPermissions(this,audioPermission,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("audio_permission",true);
            editor.apply();
        } else {
            audioPermissionGranted = true;
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

                audioPermissionGranted = true;

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,audioPermission[0])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_audio_title);
                builder.setMessage(R.string.permission_audio_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SelectStatusActivity.this,audioPermission,PERMISSION_CALLBACK_CONSTANT);
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
                Toast.makeText(getBaseContext(),"Permission is required!",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(this, audioPermission[0]) == PackageManager.PERMISSION_GRANTED) {

                //Got Permission
                audioPermissionGranted = true;

            }
        }
    }


    private void startIntroAnimation() {
        rlAudio.setTranslationX(-Utils.getScreenWidth(this));
        rlAudio.setTranslationX(+Utils.getScreenWidth(this));
        rlAudio.animate()
                .translationX(0)
                .setDuration(300)
                .setStartDelay(50)
                .start();
        rlCamera.setTranslationX(-Utils.getScreenWidth(this));
        rlCamera.setTranslationX(+Utils.getScreenWidth(this));
        rlCamera.animate()
                .translationX(0)
                .setDuration(300)
                .setStartDelay(100)
                .start();
        rlGIF.setTranslationX(-Utils.getScreenWidth(this));
        rlGIF.setTranslationX(+Utils.getScreenWidth(this));
        rlGIF.animate()
                .translationX(0)
                .setDuration(300)
                .setStartDelay(150)
                .start();
        rlText.setTranslationX(-Utils.getScreenWidth(this));
        rlText.setTranslationX(+Utils.getScreenWidth(this));
        rlText.animate()
                .translationX(0)
                .setDuration(300)
                .setStartDelay(200)
                .start();
        rlPhoto.setTranslationX(-Utils.getScreenWidth(this));
        rlPhoto.setTranslationX(+Utils.getScreenWidth(this));
        rlPhoto.animate()
                .translationX(0)
                .setDuration(300)
                .setStartDelay(250)
                .start();
        rlTalkingPhoto.setTranslationX(-Utils.getScreenWidth(this));
        rlTalkingPhoto.setTranslationX(+Utils.getScreenWidth(this));
        rlTalkingPhoto.animate()
                .translationX(0)
                .setDuration(300)
                .setStartDelay(300)
                .start();
    }

    @Override
    public void onBackPressed() {
        lyRoot.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        SelectStatusActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.getMenu().getItem(3).setChecked(true);
    }

}
