package org.octabyte.zeem.Comment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.Comment;
import com.appspot.octabyte_zeem.zeem.model.CommentItem;
import com.appspot.octabyte_zeem.zeem.model.CommentResponse;
import com.appspot.octabyte_zeem.zeem.model.StoryComment;
import com.appspot.octabyte_zeem.zeem.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.octabyte.zeem.API.CommentTask;
import org.octabyte.zeem.API.SearchTask;
import org.octabyte.zeem.API.StoryTask;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.CloudStorage;
import org.octabyte.zeem.Utils.FocusView;
import org.octabyte.zeem.Utils.ScalingUtilities;
import org.octabyte.zeem.Utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;
import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/1/2017.
 */

public class CommentActivity extends AppCompatActivity implements
        CommentAdapter.OnCommentItemClickListener,
        MentionAdapter.OnMentionItemClickListener
{
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    public static final String POST_ID = "post_id";
    private static final int RESULT_PICK_IMAGE = 137;

    private Toolbar toolbar;
    private RelativeLayout contentRoot;
    private RecyclerView rvComments;
    private LinearLayout llCommentTextView;
    private EditText etCommentText;
    private ImageView ibSendTextComment;
    private ImageView ibAudioRecording;
    private ImageView ivCommentBlinkMic;
    private LinearLayout llCommentAudioView;
    private TextView tvCommentAudioTime;
    private TextView tvCommentSlideCancel;
    private RecyclerView rvMention;

    private int xDelta;
    private int audioCancelWidth;
    private Boolean isAudioCancel = false;

    private CommentAdapter commentsAdapter;

    private MentionAdapter mentionAdapter;

    private int drawingStartLocation;

    private int _x;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] audioPermission = new String[]{Manifest.permission.RECORD_AUDIO};

    private Long mPostId, mUserId, storyId;
    private Boolean isAnonymous = false;
    private List<Long> taggedUser = new ArrayList<>();
    private String userFullName, userProfilePic;
    private int userBadge, storyNum;
    private Boolean isMentionAdapterSet = false;
    private Boolean isPostComment = true; // comment may be for a post or for a story
    private String postSafeKey, storySafeKey;
    private boolean mentionStart = false;
    private String finalLocalString;
    private MediaRecorder mRecorder;
    private String mFileName;
    private List<User> tagUserList;
    private boolean tagUserAlreadyFetch = false;
    private String postMode;
    private String cursor;
    private ImageButton ibCommentProfilePic;
    private TextView toolbarTitle;
    private View vLoadingView, vNoInternetView;
    private View vNothingFoundView;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);
        userFullName = pref.getString("fullName", null);
        userProfilePic = pref.getString("profilePic", null);
        userBadge = pref.getInt("badge", 0);

        mPostId = getIntent().getLongExtra(POST_ID, 0L);
        postSafeKey = getIntent().getStringExtra("POST_SAFE_KEY");
        postMode = getIntent().getStringExtra("ARG_POST_MODE");

        if (mPostId == 0L){
            // It's a story comments
            isPostComment = false;
            storyId = getIntent().getLongExtra("STORY_ID", 0L);
            storyNum = getIntent().getIntExtra("STORY_NUM", 0);
            storySafeKey = getIntent().getStringExtra("STORY_SAFE_KEY");

        }

        initVariable();
        setupComments();
        //setupSendCommentButton();
        if(ActivityCompat.checkSelfPermission(this, audioPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            setupAudioTouch();
        }else{
            ibAudioRecording.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkRuntimePermission();
                }
            });
        }


        setupTextComment();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showUserInfoFirstTime();
            }
        },1000);

        int actionbarSize = Utils.dpToPx(50);
        toolbar.setTranslationY(-actionbarSize);
        toolbarTitle.setText(R.string.title_comment);

        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            contentRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contentRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });
        }

        // Add profile pic into send comment section
        Glide.with(this).load(userProfilePic)
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                .apply(RequestOptions.circleCropTransform()).into(ibCommentProfilePic);
    }
    private void initVariable(){
        toolbar = findViewById(R.id.toolbar);
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        contentRoot = findViewById(R.id.rlMainLayout);
        rvComments = findViewById(R.id.rvComments);
        llCommentTextView = findViewById(R.id.llCommentTextView);
        etCommentText = findViewById(R.id.etCommentText);
        ibSendTextComment = findViewById(R.id.ibCommentSendText);
        ibAudioRecording = findViewById(R.id.ibCommentAudioRecording);
        ivCommentBlinkMic = findViewById(R.id.ivCommentBlinkMic);
        llCommentAudioView = findViewById(R.id.llCommentAudioView);
        tvCommentAudioTime = findViewById(R.id.tvCommentAudioTime);
        tvCommentSlideCancel = findViewById(R.id.tvCommentSlideCancel);
        rvMention = findViewById(R.id.rvMention);
        ImageView ibCommentCamera = (ImageButton) findViewById(R.id.ibCommentCamera);
        ibCommentProfilePic = findViewById(R.id.ibCommentProfilePic);

        ibSendTextComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSendTextComment();
            }
        });

        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ibCommentProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAnonymous = !isAnonymous;

                if (isAnonymous){ // When anonymous is active
                    ibCommentProfilePic.setImageResource(R.drawable.ic_anonymous);
                }else{ // User is not anonymous mode set back to user pic
                    Glide.with(CommentActivity.this).load(userProfilePic)
                            .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                            .apply(RequestOptions.circleCropTransform()).into(ibCommentProfilePic);
                }
            }
        });

        ibCommentCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
    }

    private void showUserInfoFirstTime(){
        boolean firstTime = pref.getBoolean("CommentFirstTime", true);

        if (firstTime) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(R.id.ibCommentProfilePic, this))
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(R.string.info_comment_anonymous_title)
                    .setContentText(R.string.info_comment_anonymous_detail)
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
        ShowcaseView showcase = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.ibCommentAudioRecording, this))
                .withHoloShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.info_comment_audio_title)
                .setContentText(R.string.info_comment_audio_detail)
                .replaceEndButton(R.layout.show_case_hide_button)
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("CommentFirstTime", false);
                        editor.apply();
                    }

                })
                .build();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = Utils.dpToPx(8);
        layoutParams.bottomMargin = Utils.dpToPx(8);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        showcase.setButtonPosition(layoutParams);

    }

    private void pickImageFromGallery(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_PICK_IMAGE);
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
                        ActivityCompat.requestPermissions(CommentActivity.this,audioPermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (pref.getBoolean("audio_permission",false)) {
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

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("audio_permission",true);
            editor.apply();
        } else {
            ibAudioRecording.setOnClickListener(null);
            setupAudioTouch();
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

                ibAudioRecording.setOnClickListener(null);
                setupAudioTouch();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,audioPermission[0])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_audio_title);
                builder.setMessage(R.string.permission_audio_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CommentActivity.this,audioPermission,PERMISSION_CALLBACK_CONSTANT);
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
        if (requestCode == RESULT_PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            String scaledImage = null;
            try {
                ScalingUtilities scalingUtilities = new ScalingUtilities(this);
                scaledImage = scalingUtilities.compressImage(selectedImage.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // If scaled image is not null then sendImageComment
            if (scaledImage != null)
                sendImageComment(scaledImage);
            else
                Log.w("ScalingError", "Error: while scaling image");
        }else if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(this, audioPermission[0]) == PackageManager.PERMISSION_GRANTED) {

                //Got Permission
                ibAudioRecording.setOnClickListener(null);
                setupAudioTouch();

            }
        }
    }

    private void sendImageComment(String localImagePath){

        // upload image to Google Cloud Storage
        String fileNameGlobalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.COMMENT_IMG);

        if (isPostComment)
            sendPostComment("IMAGE", fileNameGlobalSource, localImagePath);
        else
            sendStoryComment("IMAGE", fileNameGlobalSource, localImagePath);

        // upload file to cloud storage
        if (localImagePath != null && fileNameGlobalSource != null){
            CloudStorage cloudStorage = new CloudStorage(this);
            cloudStorage.execute(localImagePath, fileNameGlobalSource);
        }
    }

    private void setupTextComment(){

        etCommentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            private Timer timer=new Timer();
            private final long DELAY = 1000; // milliseconds

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    ibAudioRecording.setVisibility(View.GONE);
                    ibSendTextComment.setVisibility(View.VISIBLE);
                }else{
                    ibAudioRecording.setVisibility(View.VISIBLE);
                    ibSendTextComment.setVisibility(View.GONE);
                    showComment();
                }

                try {
                    String localString = String.valueOf(s);

                    try {
                        localString = localString.substring(localString.length() - 1);
                    } catch (StringIndexOutOfBoundsException e) {
                        e.printStackTrace();

                        return;
                    }

                    if (localString.equals("@") || mentionStart){
                        mentionStart = true;
                        hideNothingFound();
                        hideComment();

                        localString = String.valueOf(s);
                        finalLocalString = localString.substring(localString.lastIndexOf('@') + 1);

                        if (!tagUserAlreadyFetch) {
                            showLoading();
                            timer.cancel();
                            timer = new Timer();
                            timer.schedule(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (!finalLocalString.isEmpty()) {
                                                SearchTask<List<User>> searchUser = new SearchTask<>(mUserId);
                                                searchUser.setMode(postMode);
                                                searchUser.setAnonymous(isAnonymous);

                                                if (postMode.equals("PUBLIC"))
                                                    searchUser.setFullName(finalLocalString);
                                                else
                                                    tagUserAlreadyFetch = true; // Get friend list only once if it's private post

                                                searchUser.execute("searchTagUser");
                                                searchUser.setListener(new SearchTask.Response<List<User>>() {
                                                    @Override
                                                    public void response(List<User> response) {
                                                        hideNothingFound();
                                                        hideLoading();

                                                        showMentionUser(response);
                                                    }
                                                });
                                            }
                                        }
                                    },
                                    DELAY
                            );
                        } else {

                            if (!finalLocalString.isEmpty())
                                filter(finalLocalString);

                        } // end inner if-else

                    } // end if

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onMentionClick(String username, Long userId) {

        showComment();

        mentionStart = false;

        String commentText = etCommentText.getText().toString();

        etCommentText.setText(Utils.replaceLast(commentText, "@"+finalLocalString, "@"+username+" "));
        etCommentText.setSelection( etCommentText.getText().length());

        // If user is not in the list add it into taggedUser list
        if(!taggedUser.contains(userId)){
            taggedUser.add(userId);
        }

    }

    private void showMentionUser(List<User> userList){

        if(!isMentionAdapterSet){
            isMentionAdapterSet = true;
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            rvMention.setLayoutManager(linearLayoutManager);
            rvMention.setHasFixedSize(true);

            tagUserList = userList;

            mentionAdapter = new MentionAdapter(this, userList);
            mentionAdapter.setOnMentionItemClickListener(CommentActivity.this);
            rvMention.setAdapter(mentionAdapter);
            rvMention.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }else{
            mentionAdapter.updateList(userList);
        }

    }
    private void filter(String text){
        if (tagUserList != null) {
            List<User> temp = new ArrayList<>();
            for(User fm: tagUserList){
                if(fm.getFullName().toLowerCase().contains(text))
                    temp.add(fm);
            }
            mentionAdapter.updateList(temp);
        }
    }

    private void showComment(){
        rvComments.setVisibility(View.VISIBLE);
        rvMention.setVisibility(View.GONE);
    }
    private void hideComment(){
        rvComments.setVisibility(View.GONE);
        rvMention.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupAudioTouch(){
        ibAudioRecording.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        sendAudioComment(false);
                        //Utils.scaleView(ibAudioRecording, 1.5f, 1.5f);
                        audioCancelWidth = X/4;
                        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) v.getLayoutParams();
                        _x = lParams.rightMargin;
                        xDelta = X;
                        Log.i("com.azeem.AA", String.valueOf(_x));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i("com.azeem.AA", xDelta - X +" : "+audioCancelWidth);
                        if ((xDelta - X) < audioCancelWidth) {
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
                            layoutParams.rightMargin = xDelta - X;
                            v.setLayoutParams(layoutParams);
                            if((xDelta - X) > audioCancelWidth/2)
                                tvCommentSlideCancel.setAlpha(0.5f);
                        }else{
                            isAudioCancel = true;
                        }
                        if(isAudioCancel){

                            cancelRecording();

                            tvCommentAudioTime.setVisibility(View.GONE);
                            ivCommentBlinkMic.clearAnimation();
                            ivCommentBlinkMic.setImageResource(R.drawable.ic_trash);
                            //showTextCommentView();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        tvCommentSlideCancel.setAlpha(1);
                        ivCommentBlinkMic.setImageResource(R.drawable.ic_mic_red);
                        tvCommentAudioTime.setVisibility(View.VISIBLE);
                        //Utils.scaleView(ibAudioRecording,1f,1f);
                        LinearLayout.LayoutParams vLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
                        Log.i("com.azeem.AA", String.valueOf(vLayoutParams.leftMargin));
                        vLayoutParams.rightMargin = _x;
                        v.setLayoutParams(vLayoutParams);

                        if (!isAudioCancel)
                            sendAudioComment(true);
                        else
                            showTextCommentView();

                        isAudioCancel = false;
                        break;
                }
                return true;
            }
        });
    }

    private void showTextCommentView(){
        llCommentAudioView.setVisibility(View.GONE);
        llCommentTextView.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ibAudioRecording.getLayoutParams();
        layoutParams.width = Utils.dpToPx(40);
        layoutParams.height = Utils.dpToPx(40);
        layoutParams.rightMargin = Utils.dpToPx(8);
        layoutParams.topMargin = Utils.dpToPx(8);
        layoutParams.bottomMargin = Utils.dpToPx(8);
        ibAudioRecording.setLayoutParams(layoutParams);
    }
    private void showAudioCommentView(){
        llCommentTextView.setVisibility(View.GONE);
        llCommentAudioView.setVisibility(View.VISIBLE);
        llCommentAudioView.setAnimation(AnimationUtils.loadAnimation(CommentActivity.this, R.anim.slide_in_from_right));
        Utils.blinkAnimation(ivCommentBlinkMic);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ibAudioRecording.getLayoutParams();
        layoutParams.width = Utils.dpToPx(80);
        layoutParams.height = Utils.dpToPx(80);
        layoutParams.rightMargin = Utils.dpToPx(0);
        layoutParams.topMargin = Utils.dpToPx(0);
        layoutParams.bottomMargin = Utils.dpToPx(-12);
        ibAudioRecording.setLayoutParams(layoutParams);
    }
    private void sendAudioComment(Boolean isAudioActive){
        if(isAudioActive){

            stopRecording();
            sendRecording();

            showTextCommentView();
            ibAudioRecording.setVisibility(View.VISIBLE);
            ibSendTextComment.setVisibility(View.GONE);
        }else{
            showAudioCommentView();
            startRecording();
        } // end if
    }

    private void startRecording(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFileName = Utils.getOutputAudioMedia(CommentActivity.this);
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(mFileName);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    mRecorder.prepare();
                    setupTimer();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mRecorder.start();
            }
        }, 300);
    }

    private void setupTimer(){

        CountDownTimer countDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(final long l) {
                if (mRecorder != null){
                    tvCommentAudioTime.post(new Runnable() {
                        @Override
                        public void run() {
                            tvCommentAudioTime.setText(Utils.milliSecondsToTimer(15000 - l));
                        }
                    });
                }else{
                    this.cancel();
                }
            }

            @Override
            public void onFinish() {
                sendAudioComment(true);
            }
        }.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRecorder = null;
        }
    }

    private void sendRecording() {

        if (!Utils.isNetworkAvailable(this)){
            Toast.makeText(this, R.string.error_in_network_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mFileName != null) {

            if (Utils.isMediaRecorded(mFileName)) { // Something is recorded in file

                // Send file to Google Cloud Storage
                String globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.AUDIO);


                if (isPostComment) {

                    String globalSourceWithDuration = globalSource + "_" + Utils.getMediaDuration(mFileName);
                    String localSourceWithDuration = mFileName + "_" + Utils.getMediaDuration(mFileName);

                    sendPostComment("AUDIO", globalSourceWithDuration, localSourceWithDuration);
                } else {

                    String globalSourceWithDuration = globalSource + "_" + Utils.getMediaDuration(mFileName);
                    String localSourceWithDuration = mFileName + "_" + Utils.getMediaDuration(mFileName);

                    sendStoryComment("AUDIO", globalSourceWithDuration, localSourceWithDuration);
                }

                CloudStorage cloudStorage = new CloudStorage(this);
                cloudStorage.execute(mFileName, globalSource);
                mFileName = null;
            }
        }
    }

    private void cancelRecording(){
        if (mFileName != null){

            File file = new File(mFileName);
            if (file.exists()){
                file.delete();
            }

            stopRecording();
            mFileName = null;
        }
    }

    private void setupComments() {



        if (Utils.isNetworkAvailable(this)) {

            hideNoInternet();
            showLoading();

            if (isPostComment) {
                CommentTask<CommentResponse> commentResponse = new CommentTask<>(mPostId);
                commentResponse.setUserId(mUserId);
                commentResponse.execute("getComments");
                commentResponse.setListener(new CommentTask.Response<CommentResponse>() {
                    @Override
                    public void response(CommentResponse response) {
                        hideLoading();

                        if (response.getCommentList() != null) {
                            cursor = response.getCursor();
                            setAdapter(response.getCommentList());
                        }else {
                            Log.w("APIDebugging", "null response in CommentActivity -> setupComments");
                            showNothingFound();
                        }
                    }
                });
            }else {
                StoryTask<CommentResponse> storyComment = new StoryTask<>();
                storyComment.setStoryId(storyId);
                storyComment.setStoryNum(storyNum);
                storyComment.execute("storyComment");
                storyComment.setListener(new StoryTask.Response<CommentResponse>() {
                    @Override
                    public void response(CommentResponse response) {
                        if (response.getCommentList() != null) {
                            hideLoading();
                            cursor = response.getCursor();
                            setAdapter(response.getCommentList());
                        }else {
                            Log.w("APIDebugging", "null response in CommentActivity -> setupComments - story comment");
                            showNothingFound();
                        }
                    }
                });
            }

        }else{
            showNoInternet();
        }
    }

    private void setAdapter(List<CommentItem> postCommentList){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CommentActivity.this);
        linearLayoutManager.setReverseLayout(true);
        rvComments.setLayoutManager(linearLayoutManager);
        rvComments.setHasFixedSize(true);

        List<CommentItem> commentList = postCommentList;

        commentsAdapter = new CommentAdapter(this, this, commentList, postMode);
        commentsAdapter.setOnCommentItemClickListener(CommentActivity.this);
        rvComments.setAdapter(commentsAdapter);
        rvComments.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rvComments.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    commentsAdapter.setAnimationsLocked(true);
                }
            }
        });
    }


    private void startIntroAnimation() {
        ViewCompat.setElevation(toolbar, 0);
        contentRoot.setScaleY(0.1f);
        contentRoot.setPivotY(drawingStartLocation);
        llCommentTextView.setTranslationY(200);
        ibAudioRecording.setTranslationY(200);

        contentRoot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setElevation(toolbar, Utils.dpToPx(4));
                        animateContent();
                    }
                })
                .start();
        toolbar.animate()
                .translationY(0)
                .setDuration(300);
    }

    private void animateContent() {

        //commentsAdapter.updateItems();
        llCommentTextView.animate().translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(200)
                .start();
        ibAudioRecording.animate().translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(200)
                .start();
    }

    @Override
    public void onBackPressed() {
        ViewCompat.setElevation(toolbar, 0);
        contentRoot.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        CommentActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    private void onSendTextComment(){
        if (validateComment()) {

            String commentText = etCommentText.getText().toString();

            if (isPostComment) {
                sendPostComment("TEXT", commentText);
            }else {
                sendStoryComment("TEXT", commentText);
            }


            etCommentText.setText(null);

        }
    }

    // Over loading method
    private void sendPostComment(String type, String source){
        sendPostComment(type, source, source);
    }
    private void sendPostComment(String type, String source, String localSource){
        hideNothingFound();

        // Create CommentItem object to add it in adapter
        final CommentItem commentItem = new CommentItem();

        // Create a comment object to send it server
        Comment comment = new Comment();
        comment.setType(type);
        comment.setSource(source);
        comment.setAnonymous(isAnonymous);

        if (taggedUser.size() > 0)
            comment.setTaggedUser(taggedUser);

        CommentTask<Comment> createComment = new CommentTask<>();
        createComment.setUserId(mUserId);
        createComment.setPostSafeKey(postSafeKey);
        createComment.setComment(comment);
        createComment.execute("createComment");
        createComment.setListener(new CommentTask.Response<Comment>() {
            @Override
            public void response(Comment response) {

                if (response != null) {
                    // Find the index of this item
                    int index = commentsAdapter.findItemIndex(commentItem);
                    // Update Item
                    commentItem.setCommentSafeKey(response.getCommnetSafeKey());
                    commentsAdapter.updateCommentView(commentItem, index);
                }else {
                    Log.w("APIDebugging", "null response in CommentActivity->sendPostComment");
                    // If There is some problem remove comment view from adapter
                    commentsAdapter.removeCommentView(commentItem);
                }
            }
        });

        // Send comment immediately

        commentItem.setType(type).setSource(localSource).setCommentSafeKey("AAAAAAA")
                .setAnonymous(isAnonymous).setStarCount(0).setUserFullName(userFullName).setPostedOnHumanReadable("Sec")
                .setUserId(mUserId).setProfilePic(userProfilePic).setUserBadge(userBadge).setStarByMe(false);

        // Check if comment adapter is not null
        if (commentsAdapter == null){
            List<CommentItem> newSingleItem = new ArrayList<>();
            newSingleItem.add(commentItem);
            setAdapter(newSingleItem);
        }else{
            // setting animation
            rvComments.smoothScrollToPosition(0);
            commentsAdapter.addCommentView(commentItem);

        }


        etCommentText.setText(null);
    }

    // Over loading method
    private void sendStoryComment(String type, String source){
        sendStoryComment(type, source, source);
    }
    private void sendStoryComment(String type, String source, String localSource){
        hideNothingFound();

        // Create CommentItem object to add it in adapter
        final CommentItem commentItem = new CommentItem();

        StoryComment storyComment = new StoryComment();
        storyComment.setType(type);
        storyComment.setSource(source);
        storyComment.setStoryNum(storyNum);
        storyComment.setAnonymous(isAnonymous);
        storyComment.setTaggedUser(taggedUser);

        StoryTask<StoryComment> createComment = new StoryTask<>(mUserId);
        createComment.setStorySafeKey(storySafeKey);
        createComment.setStoryComment(storyComment);
        createComment.execute("createStoryComment");
        createComment.setListener(new StoryTask.Response<StoryComment>() {
            @Override
            public void response(StoryComment response) {
                if (response != null) {
                    // Find the index of this item
                    int index = commentsAdapter.findItemIndex(commentItem);
                    // Update Item
                    commentItem.setCommentSafeKey(response.getCommnetSafeKey());
                    commentsAdapter.updateCommentView(commentItem, index);
                }else {
                    Log.w("APIDebugging", "null response in CommentActivity->sendStoryComment");
                    // If There is some problem remove comment view from adapter
                    commentsAdapter.removeCommentView(commentItem);
                }
            }
        });


        // Send Comment immediately

        commentItem.setCommentSafeKey("AAAAAAA").setType(type).setSource(localSource)
                .setAnonymous(isAnonymous).setStarCount(0).setUserFullName(userFullName).setPostedOnHumanReadable("Sec")
                .setUserId(mUserId).setProfilePic(userProfilePic).setUserBadge(userBadge).setStarByMe(false);

        // Check if comment adapter is not null
        if (commentsAdapter == null){
            List<CommentItem> newSingleItem = new ArrayList<>();
            newSingleItem.add(commentItem);
            setAdapter(newSingleItem);
        }else{
            // setting animation
            commentsAdapter.addCommentView(commentItem);
            rvComments.smoothScrollToPosition(0);
        }

        etCommentText.setText(null);

    }


    private boolean validateComment() {
        if (isEmpty(etCommentText.getText())) {
            ibSendTextComment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
            return false;
        }

        return true;
    }

    @Override
    public void onTopReached() {

        if (commentsAdapter != null) {

            if (isPostComment && cursor != null) {
                CommentTask<CommentResponse> commentResponse = new CommentTask<>(mPostId);
                commentResponse.setUserId(mUserId);
                commentResponse.setCursor(cursor);
                commentResponse.execute("getComments");
                commentResponse.setListener(new CommentTask.Response<CommentResponse>() {
                    @Override
                    public void response(CommentResponse response) {
                        if (response.getCommentList() != null) {
                            cursor = response.getCursor();
                            commentsAdapter.addItemsToTop(response.getCommentList());
                        }else{
                            cursor = null;
                        }
                    }
                });
            } else if (cursor != null) {
                StoryTask<CommentResponse> storyComment = new StoryTask<>();
                storyComment.setStoryId(storyId);
                storyComment.setStoryNum(storyNum);
                storyComment.setCursor(cursor);
                storyComment.execute("storyComment");
                storyComment.setListener(new StoryTask.Response<CommentResponse>() {
                    @Override
                    public void response(CommentResponse response) {
                        if (response.getCommentList() != null) {
                            cursor = response.getCursor();
                            commentsAdapter.addItemsToTop(response.getCommentList());
                        }else{
                            cursor = null;
                        }
                    }
                });
            }

        }

    }

    @Override
    public void onProfileClick(View v, Long profileId) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2; //2
        ProfileActivity.startUserProfileFromLocation(startingLocation, this, String.valueOf(profileId));
        overridePendingTransition(0, 0);
    }

    @Override
    public void onReplyClick(String username) {
        etCommentText.setText(username);
        etCommentText.setSelection( etCommentText.getText().length());
        etCommentText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etCommentText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void onCommentDelete(String commentSafeKey, int position) {
        commentsAdapter.commentList.remove(position);
        commentsAdapter.notifyItemRemoved(position);

        CommentTask<Void> commentTask = new CommentTask<>();
        commentTask.setCommentSafeKey(commentSafeKey);
        commentTask.execute("deleteComment");
    }

    private void showLoading(){
        if (vLoadingView == null)
            vLoadingView = ((ViewStub) findViewById(R.id.vsCommentLoading)).inflate();
        else
            vLoadingView.setVisibility(View.VISIBLE);
    }
    private void hideLoading(){
        if (vLoadingView != null)
            vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null)
            vNoInternetView = ((ViewStub) findViewById(R.id.vsCommentNoInternet)).inflate();
        else
            vNoInternetView.setVisibility(View.VISIBLE);
    }
    private void hideNoInternet(){
        if (vNoInternetView != null)
            vNoInternetView.setVisibility(View.GONE);
    }
    private void showNothingFound(){
        if (vNothingFoundView == null) {
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsCommentNothingFound)).inflate();
            TextView title = findViewById(R.id.tvNothingFoundTitle);
            TextView message = findViewById(R.id.tvNothingFoundMessage);
            TextView messageLine2 = findViewById(R.id.tvNothingFoundMessageLine2);
            title.setText(R.string.nothing_found_title_in_comment);
            message.setText(R.string.nothing_found_message_in_comment);
            messageLine2.setText(R.string.nothing_found_message2_in_comment);
        }else {
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }

}
