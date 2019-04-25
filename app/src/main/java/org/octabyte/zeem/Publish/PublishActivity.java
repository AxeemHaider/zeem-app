package org.octabyte.zeem.Publish;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.appspot.octabyte_zeem.zeem.model.Post;
import com.appspot.octabyte_zeem.zeem.model.User;
import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.API.SearchTask;
import org.octabyte.zeem.Camera.SquaredFrameLayout;
import org.octabyte.zeem.Camera.helper.GeneralUtils;
import org.octabyte.zeem.Comment.MentionAdapter;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.Home.PublicActivity;
import org.octabyte.zeem.List.ListActivity;
import org.octabyte.zeem.Profile.RevealBackgroundView;
import org.octabyte.zeem.Utils.CircularTextView;
import org.octabyte.zeem.Utils.CloudStorage;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.FocusView;
import org.octabyte.zeem.Utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static org.octabyte.zeem.Camera.CameraActivity.MEDIA_TYPE_IMAGE;
import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/1/2017.
 */

public class PublishActivity extends AppCompatActivity implements
        MentionAdapter.OnMentionItemClickListener,
        RevealBackgroundView.OnStateChangeListener {
    private static final String ARG_TAKEN_MEDIA_URI = "arg_taken_media_uri";
    private final static String TAG = "com.azeem.DP";
    private static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    public static FeedItem newFeedItem; // Used to move data from publish activity to Home activity, also from camera

    private ImageView
            ivUserProfile,
            ivPhoto;

    private Spinner
            publishSpinner,
            expireSpinner;

    private EditText
            etTagPeople,
            etDescription,
            etStatusCard;

    private RecyclerView rvMention;
    private LinearLayout llStatusLayout, llTagUsers, llStatusLocation;

    private TextView
            tvUserFullName,
            tvAddedLocation;

    private Switch commentSwitch;

    private LinearLayout
            llCardStatus;
    private LinearLayout llMainLayout;
    private LinearLayout llMentionView;


    private TextView tvTagPeople;

    private RevealBackgroundView vRevealBackground;

    private MentionAdapter mentionAdapter;

    private int mentionFlag = 0;
    private int mentionCount = 0;
    private String mentionName;
    private int matcherStart;

    private List<Long> tagIds = new ArrayList<>();

    private boolean propagatingToggleState = false;
    private Uri mediaUri;
    private CircularTextView currColor;
    private Boolean isAnonymous = false;

    private Long mUserId;

    private String fullname;
    private String profilePic;
    private int badge;
    private String postMode;
    private String expireOn = "NEVER";
    private String mediaPath, localMediaPath;
    private String statusType;
    private Boolean commentOnPost = true;
    private String cardColor = "#673AB7,#ffffff"; // Default etStatusCard color (Holo red and white Text)
    private Boolean isMentionAdapterSet = false;
    private long userSelectedListId;
    private int userSelectedListMemberCount;
    private String userListSelection;
    private boolean mentionAdapterActive;
    private boolean tagToPeople = false;
    private Long expireTimeInMilli;
    private String location;
    private List<User> tagUserList;
    private boolean tagUserAlreadyFetch = false;
    private int audioCancelWidth, _x, xDelta;
    private boolean isAudioCancel = false;
    private String mFileName;
    private MediaRecorder mRecorder;
    private String mediaCover;
    private String audioSource, localAudioSource;
    private String localMediaCover;
    private ScrollView svMainView;
    private EditText etCaption;
    private LinearLayout rlAudio;
    private TextView tvAudioCancel, tvAudioTime;
    private ImageView ivSendAudioStatus, ivAudioRecording;
    private SharedPreferences pref;
    private View vLoadingView, vNothingFoundView;


    public static void openWithMediaUri(Activity openingActivity, Uri mediaUri, String mode, String statusType) {
        Intent intent = new Intent(openingActivity, PublishActivity.class);
        intent.putExtra(ARG_TAKEN_MEDIA_URI, mediaUri);
        intent.putExtra("USER_STATUS_MODE", mode);
        intent.putExtra("USER_STATUS_PRIVATE_PUBLIC_TYPE", statusType);
        openingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        initVariable();

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);
        String username = pref.getString("username", null);
        fullname = pref.getString("fullName", null);
        profilePic = pref.getString("profilePic", null);
        badge = pref.getInt("badge", 0);

        Glide.with(this).load(profilePic)
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                .apply(RequestOptions.circleCropTransform()).into(ivUserProfile);
        tvUserFullName.setText(Utils.capitalize(fullname));

        statusType = getIntent().getStringExtra("USER_STATUS_MODE");
        postMode = getIntent().getStringExtra("USER_STATUS_PRIVATE_PUBLIC_TYPE");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showUserInfoFirstTime();
            }
        }, 1000);

        if(statusType.equals("CARD")){
            llStatusLayout.setVisibility(View.GONE);
            llCardStatus.setVisibility(View.VISIBLE);
        }else if(statusType.equals("IMAGE")){
            mediaUri = getIntent().getParcelableExtra(ARG_TAKEN_MEDIA_URI);
            llStatusLayout.setVisibility(View.VISIBLE);
            llCardStatus.setVisibility(View.GONE);
            loadThumbnailPhoto();
        }else if (statusType.equals("AUDIO")){
            llStatusLayout.setVisibility(View.GONE);
            etCaption.setVisibility(View.VISIBLE);
            rlAudio.setVisibility(View.VISIBLE);
            setupAudioTouch();
        }else if(statusType.equals("VIDEO") || statusType.equals("GIF")){
            mediaUri = getIntent().getParcelableExtra(ARG_TAKEN_MEDIA_URI);
            llStatusLayout.setVisibility(View.VISIBLE);
            llCardStatus.setVisibility(View.GONE);
            Bitmap thumb = createVideoThumb(mediaUri.getPath());
            ivPhoto.setImageBitmap(thumb);
        }else if (statusType.equals("TALKING_PHOTO")){
            etDescription.setVisibility(View.GONE);
            mediaUri = getIntent().getParcelableExtra(ARG_TAKEN_MEDIA_URI);
            llStatusLayout.setVisibility(View.VISIBLE);
            rlAudio.setVisibility(View.VISIBLE);
            loadThumbnailPhoto();
            setupAudioTouch();
        }

        if (postMode.equals("PRIVATE"))
            setPublishSpinner(true);
        else
            setPublishSpinner(false);

        setExpireSpinner();
        setupTagPeople();
        setupMentionPeople();
        setupCurrentLocation();
        setupRevealBackground(null);
    }

    private void showUserInfoFirstTime(){
        boolean firstTime = pref.getBoolean("PublishFirstTime", true);

        if (firstTime) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(R.id.ivUserProfile, this))
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(R.string.info_comment_anonymous_title)
                    .setContentText(R.string.info_comment_anonymous_detail)
                    .replaceEndButton(R.layout.show_case_hide_button)
                    .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            infoExpireFirstTime();
                        }
                    })
                    .build();
        }else{
            infoColorsForStatus();
        }

    }
    private void infoExpireFirstTime(){
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.expire_spinner, this))
                .withHoloShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.info_publish_expire_post_title)
                .setContentText(R.string.info_publish_expire_post_detail)
                .replaceEndButton(R.layout.show_case_hide_button)
                .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        infoListFirstTime();
                    }
                })
                .build();
    }
    private void infoListFirstTime(){
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.publish_spinner, this))
                .withHoloShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.info_publish_post_title)
                .setContentText(R.string.info_publish_expire_post_detail)
                .replaceEndButton(R.layout.show_case_hide_button)
                .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        infoCommentOffFirstTime();
                    }
                })
                .build();
    }
    private void infoCommentOffFirstTime(){
        new ShowcaseView.Builder(this)
                .withHoloShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.info_publish_post_turn_comment_title)
                .setContentText(R.string.info_publish_post_turn_comment_detail)
                .replaceEndButton(R.layout.show_case_hide_button)
                .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("PublishFirstTime", false);
                        editor.apply();
                        infoColorsForStatus();
                    }
                })
                .build();
    }
    private void infoColorsForStatus(){
        boolean firstTime = pref.getBoolean("ColorForStatusInfo", true);

        if (firstTime) {
            if (statusType.equals("CARD")) {
                new ShowcaseView.Builder(this)
                        .withHoloShowcase()
                        .setStyle(R.style.CustomShowcaseTheme)
                        .setContentTitle(R.string.info_color_title)
                        .setContentText(R.string.info_color_user_msg)
                        .replaceEndButton(R.layout.show_case_hide_button)
                        .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                            @Override
                            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putBoolean("ColorForStatusInfo", false);
                                editor.apply();
                            }
                        })
                        .build();
            }
        }
    }

    private Bitmap createVideoThumb(String filePath){

        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);

        File pictureFile = GeneralUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return null;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        localMediaCover = pictureFile.getAbsolutePath();

        return thumb;
    }

    private void initVariable(){

        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        etCaption = findViewById(R.id.etCaption);
        rlAudio = findViewById(R.id.llAudio);
        tvAudioCancel = findViewById(R.id.tvAudioCancel);
        ivSendAudioStatus = findViewById(R.id.ivSendAudioStatus);
        ivAudioRecording = findViewById(R.id.ivAudioRecording);
        tvAudioTime = findViewById(R.id.tvAudioTime);
        ivPhoto = findViewById(R.id.ivPhoto);
        publishSpinner = findViewById(R.id.publish_spinner);
        expireSpinner = findViewById(R.id.expire_spinner);
        etTagPeople = findViewById(R.id.etTagPeople);
        rvMention = findViewById(R.id.rvMention);
        llStatusLayout = findViewById(R.id.llStatusLayout);
        llStatusLocation = findViewById(R.id.llStatusLocation);
        etDescription = findViewById(R.id.etDescription);
        SquaredFrameLayout cardStatus = findViewById(R.id.cardStatus);
        llCardStatus = findViewById(R.id.llCardStatus);
        etStatusCard = findViewById(R.id.etStatusCard);
        vRevealBackground = findViewById(R.id.vRevealBackground);
        llMainLayout = findViewById(R.id.llMainLayout);
        LinearLayout llUserLayout = findViewById(R.id.llUserLayout);
        llMentionView = findViewById(R.id.llMentionView);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvAddedLocation = findViewById(R.id.tvAddedLocation);
        llTagUsers = findViewById(R.id.llTagUsers);
        commentSwitch = findViewById(R.id.commentSwitch);
        svMainView = findViewById(R.id.svMainView);

        setupCommentSwitch();

        ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToggle();
            }
        });

        tvTagPeople = findViewById(R.id.tvTagPeople);
        tvTagPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagToPeople = true;
                showMentionAdapter();
            }
        });

        tvAddedLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llStatusLocation.setVisibility(View.VISIBLE);
                tvAddedLocation.setText(R.string.add_location);
                tvAddedLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_blue,0,0,0);
            }
        });
    }

    private void setupCurrentLocation(){

        LocationTask locationTask = new LocationTask(this);
        locationTask.execute();
        locationTask.setUserLocationTracker(new LocationTask.UserLocationTracker() {
            @Override
            public void onLocationTrack(List<Address> addresses) {
                for (Address address : addresses){
                    final TextView textView = new TextView(PublishActivity.this);
                    textView.setText(address.getAddressLine(0));
                    //textView.setTextSize(getResources().getDimension(R.dimen.font_size_small));
                    textView.setBackgroundResource(R.drawable.shape_round_corner_light);
                    textView.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                    textView.setPadding(Utils.dpToPx(8),Utils.dpToPx(8),Utils.dpToPx(8),Utils.dpToPx(8));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.rightMargin = Utils.dpToPx(8);
                    textView.setLayoutParams(layoutParams);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            llStatusLocation.setVisibility(View.GONE);
                            tvAddedLocation.setText(textView.getText());
                            tvAddedLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_blue,0,R.drawable.ic_close, 0);
                        }
                    });
                    llStatusLocation.addView(textView);
                }
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupAudioTouch(){
        ivSendAudioStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        tvAudioCancel.setVisibility(View.VISIBLE);
                        sendAudioRecordingStatus(false);
                        //Utils.scaleView(ivSendAudioComment, 1.5f, 1.5f);
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
                                tvAudioCancel.setAlpha(0.5f);
                        }else{
                            isAudioCancel = true;
                        }
                        if(isAudioCancel){

                            cancelRecording();

                            tvAudioTime.setVisibility(View.GONE);
                            ivAudioRecording.clearAnimation();
                            ivAudioRecording.setImageResource(R.drawable.ic_trash);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        tvAudioCancel.setAlpha(1);
                        ivAudioRecording.setImageResource(R.drawable.ic_mic_red);
                        tvAudioTime.setVisibility(View.VISIBLE);
                        tvAudioCancel.setVisibility(View.GONE);
                        //Utils.scaleView(ivSendAudioComment,1f,1f);
                        LinearLayout.LayoutParams vLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
                        Log.i("com.azeem.AA", String.valueOf(vLayoutParams.leftMargin));
                        vLayoutParams.rightMargin = _x;
                        v.setLayoutParams(vLayoutParams);

                        if (!isAudioCancel)
                            sendAudioRecordingStatus(true);
                        else
                            clearRecordingButton();

                        isAudioCancel = false;
                        break;
                }
                return true;
            }
        });
    }

    private void clearRecordingButton(){
        ivAudioRecording.clearAnimation();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ivSendAudioStatus.getLayoutParams();
        layoutParams.width = Utils.dpToPx(40);
        layoutParams.height = Utils.dpToPx(40);
        layoutParams.rightMargin = Utils.dpToPx(8);
        layoutParams.topMargin = Utils.dpToPx(8);
        layoutParams.bottomMargin = Utils.dpToPx(8);
        ivSendAudioStatus.setLayoutParams(layoutParams);
    }

    private void sendAudioRecordingStatus(Boolean isAudioActive){
        if(isAudioActive){

            stopRecording();
            //sendRecording();

            clearRecordingButton();
            //rlAudio.setVisibility(View.GONE);
            //llText.setVisibility(View.VISIBLE);
        }else{
            //llText.setVisibility(View.GONE);
            //rlAudio.setVisibility(View.VISIBLE);
            rlAudio.setAnimation(AnimationUtils.loadAnimation(PublishActivity.this, R.anim.slide_in_from_right));
            Utils.blinkAnimation(ivAudioRecording);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ivSendAudioStatus.getLayoutParams();
            layoutParams.width = Utils.dpToPx(80);
            layoutParams.height = Utils.dpToPx(80);
            layoutParams.rightMargin = Utils.dpToPx(0);
            layoutParams.topMargin = Utils.dpToPx(0);
            layoutParams.bottomMargin = Utils.dpToPx(-12);
            ivSendAudioStatus.setLayoutParams(layoutParams);

            startRecording();
        } // end if
    }

    private void startRecording(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFileName = Utils.getOutputAudioMedia(PublishActivity.this);
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
        }, 500);
    }

    private void setupTimer(){

        CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(final long l) {
                if (mRecorder != null){
                    tvAudioTime.post(new Runnable() {
                        @Override
                        public void run() {
                            tvAudioTime.setText(Utils.milliSecondsToTimer(30000 - l));
                        }
                    });
                }else{
                    this.cancel();
                }
            }

            @Override
            public void onFinish() {
                sendAudioRecordingStatus(true);
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

    private void sendRecording(){
        if (mFileName != null) {

            if (Utils.isMediaRecorded(mFileName)) { // Something is recorded in file

                audioSource = fileNameForCloudStorage("AUDIO");
                localAudioSource = mFileName;

                mFileName = null;

            }else {
                Toast.makeText(this, R.string.recording_is_too_short_or_not_found, Toast.LENGTH_SHORT).show();
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

    private String fileNameForCloudStorage(String folder){
        String globalSource = null;

        switch (folder) {
            case "AUDIO":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.AUDIO);
                break;
            case "VIDEO":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.VIDEO);
                break;
            case "IMAGE":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.IMAGE);
                break;
            case "GIF":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.GIF);
                break;
            case "TALKING_PHOTO":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.COVER);
                break;
            case "COVER":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.COVER);
                break;
        }

        return globalSource;
    }

    private void uploadFileToCloudStorage(String filePath, String fileName){
        if (filePath != null && fileName != null) {
            CloudStorage cloudStorage = new CloudStorage(this);
            cloudStorage.execute(filePath, fileName);
        }
    }

    public void publishStatus(View v){

        if (!Utils.isNetworkAvailable(this)){
            Toast.makeText(this, "Error in Network connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaUri != null)
            localMediaPath = mediaUri.getPath();

        // isAnonymous
        // mUserId, username, fullname, profilePic, badge
        // postMode
        // expireOn
        // mediaPath
        // statusType
        String locationText = tvAddedLocation.getText().toString();

        if (!locationText.isEmpty())
            location = locationText;//locationText.substring(1, locationText.length()-1); // This is for replacement of [ and ]

        String caption = etDescription.getText().toString();
        if (statusType.equals("AUDIO")){
            caption = etCaption.getText().toString();
        }

        final String cardStatus = etStatusCard.getText().toString();

        PostTask<Post> createPost = new PostTask<>(mUserId);


        final Post post = new Post();
        post.setStarCount(0);
        post.setAnonymous(isAnonymous);
        post.setAllowComment(commentOnPost);


        post.setType(statusType);

        // If post type is card then set card colors else set source for images, audio, video etc
        if (statusType.equals("CARD")) {

            if (cardStatus.isEmpty()){
                Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
                return;
            }

            post.setCardColor(cardColor);
            post.setCaption(cardStatus);
        }else if(statusType.equals("VIDEO") || statusType.equals("GIF")){

            if(statusType.equals("VIDEO")){
                mediaCover = fileNameForCloudStorage("COVER");

                mediaPath = fileNameForCloudStorage( "VIDEO");
            }else {
                mediaCover = fileNameForCloudStorage("COVER");
                mediaPath = fileNameForCloudStorage("GIF");
            }

            post.setCover(mediaCover);
            post.setSource(mediaPath);
            post.setCaption(caption);
        }else if(statusType.equals("TALKING_PHOTO")){

            mediaPath = fileNameForCloudStorage("IMAGE");

            sendRecording();

            post.setCover(mediaPath);
            post.setSource(audioSource);
            post.setCaption(caption);
            if (localAudioSource == null){
                Toast.makeText(this, "You must need to record Audio caption", Toast.LENGTH_SHORT).show();
                return;
            }
        }else if(statusType.equals("AUDIO")){
            sendRecording();
            post.setSource(audioSource);
            post.setCaption(caption);
            if (localAudioSource == null){
                Toast.makeText(this, "You must need to record Audio", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if(statusType.equals("IMAGE")){
            mediaPath = fileNameForCloudStorage("IMAGE");
            post.setSource(mediaPath);
            post.setCaption(caption);
        }

        // If there is any expire time set it
        if (!expireOn.equals("NEVER"))
            post.setExpireTime(expireTimeInMilli);

        // If location is specified set location
        if (location != null)
            post.setLocation(location);

        post.setMode(postMode);

        // Check post is tagged to someone or not, And it's not a list post
        if (tagIds.size() > 0 && userListSelection == null){ // some users ara tagged with this post
            post.setPostTag(true);
            createPost.setTaggedUserList(tagIds);
        }

        // Check is it a list post
        if (userListSelection != null){ // It's a list post
            post.setListId(userListSelection);
        }

        createPost.setNewPost(post);
        createPost.execute("createPost");
        createPost.setListener(new PostTask.Response<Post>() {
            @Override
            public void response(Post response) {
                if (response != null){

                    // Send local receiver with post id and post safe key
                    Intent intent = new Intent("new-post-local-receiver");
                    intent.putExtra("postId", response.getPostId());
                    intent.putExtra("postSafeKey", response.getPostSafeKey());
                    intent.putExtra("operationSuccess", true);

                    LocalBroadcastManager.getInstance(PublishActivity.this).sendBroadcast(intent);

                }else {
                    Log.w("APIDebugging", "null response in PublishActivity->publishStatus");

                    // Send local receiver that there is some problem remove this post from feed
                    Intent intent = new Intent("new-post-local-receiver");
                    intent.putExtra("operationSuccess", false);

                    LocalBroadcastManager.getInstance(PublishActivity.this).sendBroadcast(intent);

                }
            }
        });

        newFeedItem = new FeedItem();
        // Initial values
        newFeedItem.setStarByMe(false);
        newFeedItem.setTaggedMe(false);
        newFeedItem.setTaggedApproved(false);

        // Set postId and safeKey to prevent Null Pointer Exception
        newFeedItem.setPostId(1111111L);
        newFeedItem.setPostSafeKey("AAAAAAA");

        newFeedItem.setUserId(mUserId).setUserFullName(fullname).setUserProfilePic(profilePic).setUserBadge(badge)
                .setPostedOnHumanReadable("Moment Ago").setMode(postMode)
                .setType(statusType).setAllowComment(commentOnPost).setAnonymous(isAnonymous)
                .setStarCount(0).setTotalComments(0);

        if (location != null)
            newFeedItem.setLocation(location);

        if (!expireOn.equals("NEVER"))
            newFeedItem.setExpireTimeHumanReadable(" Expire in " + expireOn);

        if (statusType.equals("CARD")){
            newFeedItem.setCardColor(cardColor);
            newFeedItem.setCaption(cardStatus);
        }else if(statusType.equals("VIDEO") || statusType.equals("GIF")){
            newFeedItem.setCover(localMediaCover);
            newFeedItem.setSource(localMediaPath);
            newFeedItem.setCaption(caption);
        }else if(statusType.equals("TALKING_PHOTO")){
            localMediaCover = localMediaPath;
            newFeedItem.setCover(localMediaCover);
            newFeedItem.setSource(localAudioSource);
            newFeedItem.setCaption(caption);
        }else if(statusType.equals("AUDIO")){
            newFeedItem.setSource(localAudioSource);
            newFeedItem.setCaption(caption);
        } else if(statusType.equals("IMAGE")){
            newFeedItem.setSource(localMediaPath);
            newFeedItem.setCaption(caption);
        }


        // Check post is tagged to someone or not
        if (tagIds.size() > 0 && userListSelection == null){ // Post is tag to someone
            newFeedItem.setPostTag(true);
            newFeedItem.setTaggedCount(tagIds.size());
        }else {
            newFeedItem.setPostTag(false);
        }

        // Check it's a list post or not
        if (userListSelection != null) { // It's a list post
            newFeedItem.setListCount(userSelectedListMemberCount);
            newFeedItem.setPostListId(userSelectedListId);
        }

        // upload files to cloud storage
        uploadFileToCloudStorage(localMediaCover, mediaCover);
        uploadFileToCloudStorage(localMediaPath, mediaPath);
        uploadFileToCloudStorage(localAudioSource, audioSource);

        bringMainActivityToTop();

    }

    private void setupCommentSwitch(){
        commentSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentOnPost = !commentSwitch.isChecked();
            }
        });
    }

    private void showToggle(){
        toggleAnonymous();
    }

    private void toggleAnonymous(){
        if(isAnonymous){
            isAnonymous = false;
            Glide.with(this).load(profilePic)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform()).into(ivUserProfile);
            tvUserFullName.setText(Utils.capitalize(fullname));
        }else{
            isAnonymous = true;
            ivUserProfile.setImageResource(R.drawable.ic_anonymous);
            tvUserFullName.setText("Anonymous");
        }
    }

    public void setCardColor(View v){
        if(currColor != null)
            currColor.setStrokeWidth(1);

        CircularTextView circularTextView = (CircularTextView) v;
        circularTextView.setStrokeWidth(5);
        currColor = circularTextView;

        cardColor = v.getTag().toString();

        String[] colors = cardColor.split(",");
        if(colors.length > 2){
            int[] colorsInt = new int[colors.length-1];
            for (int i = 0; i < colors.length-1; i++) {
                colorsInt[i] = Color.parseColor(colors[i]);
            }
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorsInt);
            etStatusCard.setBackgroundDrawable(gd);
            etStatusCard.setTextColor(Color.parseColor(colors[colors.length-1]));
        }else{
            etStatusCard.setBackgroundColor(Color.parseColor(colors[0]));
            etStatusCard.setTextColor(Color.parseColor(colors[1]));
        }
    }

    private void setPublishSpinner(Boolean isPrivate){

        String[] publishActions;

        if (isPrivate)
             publishActions = new String[]{"Private", "Circle"};
        else
            publishActions = new String[]{"Public"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, publishActions);
        publishSpinner.setAdapter(adapter);

        publishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        tvTagPeople.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        postMode = "PRIVATE";
                        tvTagPeople.setVisibility(View.GONE); // Hide tag people option
                        selectUserList();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void selectUserList(){
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("OPEN_LIST_FOR_PUBLISH_STATUS", true);
        startActivityForResult(intent, 777);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 777){
            Boolean isPrivate = true;
            if (resultCode == RESULT_OK){

                userSelectedListMemberCount = data.getIntExtra("USER_SELECTED_LIST_MEMBER_COUNT", 0);
                userSelectedListId = data.getLongExtra("USER_SELECTED_LIST_ID", 0L);
                userListSelection = userSelectedListMemberCount +"_"+ userSelectedListId;

            }else{
                // not selected any list set spinner back to default
                if (!isPrivate){
                    publishSpinner.setSelection(1);
                    postMode = "PUBLIC";
                }else {
                    publishSpinner.setSelection(0);
                    postMode = "PRIVATE";
                }
                // Show user message
                Toast.makeText(this, "You Must Need To Select One Of Circle", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setExpireSpinner(){
        final String[] publishActions = new String[]{"NEVER", "1 Hr", "2 Hr", "5 Hr", "10 Hr", "1 Week", "1 Month"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, publishActions);
        expireSpinner.setAdapter(adapter);
        expireSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                expireOn = publishActions[position];
                switch (position){
                    case 0:
                        expireTimeInMilli = null;
                        break;
                    case 1:
                        expireTimeInMilli = System.currentTimeMillis() + 3600000;
                        break;
                    case 2:
                        expireTimeInMilli = System.currentTimeMillis() + 3600000 * 2;
                        break;
                    case 3:
                        expireTimeInMilli = System.currentTimeMillis() + 3600000 * 5;
                        break;
                    case 4:
                        expireTimeInMilli = System.currentTimeMillis() + 3600000 * 10;
                        break;
                    case 5:
                        expireTimeInMilli = System.currentTimeMillis() + 604800000;
                        break;
                    case 6:
                        expireTimeInMilli = System.currentTimeMillis() + 2592000000L;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setupMentionPeople(){

        etStatusCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    String localString = String.valueOf(s);
                    localString = localString.substring(localString.length() - 1);

                    if (localString.equals("@")){
                        tagToPeople = false;
                        showMentionAdapter();
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    String localString = String.valueOf(s);
                    localString = localString.substring(localString.length() - 1);

                    if (localString.equals("@")){
                        tagToPeople = false;
                        showMentionAdapter();
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupTagPeople(){

        etTagPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            private Timer timer = new Timer();
            private final long DELAY = 1000; // milliseconds

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

                if (!s.toString().isEmpty()) {

                    if (!tagUserAlreadyFetch) {

                        hideNothingFound();
                        showLoading();

                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        SearchTask<List<User>> searchUser = new SearchTask<>(mUserId);
                                        searchUser.setMode(postMode);
                                        searchUser.setAnonymous(isAnonymous);

                                        if (postMode.equals("PUBLIC"))
                                            searchUser.setFullName(s.toString()); // It's search name
                                        else
                                            tagUserAlreadyFetch = true; // Get friend list only once if it's private post

                                        searchUser.execute("searchTagUser");
                                        searchUser.setListener(new SearchTask.Response<List<User>>() {
                                            @Override
                                            public void response(List<User> response) {
                                                hideLoading();
                                                if (response != null) {
                                                    showMentionUser(response);
                                                } else {
                                                    showNothingFound();
                                                    Log.w("APIDebugging", "null response in PublishActivity->setupTagPeople");
                                                }
                                            }
                                        });
                                    }
                                },
                                DELAY
                        );

                    } else {
                        filter(s.toString());
                    }

                }


            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showMentionUser(List<User> userList){

        if(!isMentionAdapterSet){
            isMentionAdapterSet = true;
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            rvMention.setLayoutManager(linearLayoutManager);
            rvMention.setHasFixedSize(true);

            tagUserList = userList;

            mentionAdapter = new MentionAdapter(this, tagUserList);
            mentionAdapter.setOnMentionItemClickListener(this);
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

    @Override
    public void onMentionClick(String username, Long userId) {
        etTagPeople.setText("");
        hideMentionAdapter();

        if (tagToPeople) { // Tag people not mention

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etTagPeople.getWindowToken(), 0);
            }

            tagIds.add(userId);
            TextView textView = new TextView(this);
            textView.setText(username);
            textView.setTag(userId);
            //textView.setTextSize(getResources().getDimension(R.dimen.font_size_small));
            textView.setBackgroundResource(R.drawable.shape_round_corner_light);
            textView.setTextColor(getResources().getColor(R.color.colorSecondaryText));
            textView.setPadding(Utils.dpToPx(8),Utils.dpToPx(8),Utils.dpToPx(8),Utils.dpToPx(8));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.rightMargin = Utils.dpToPx(8);
            textView.setLayoutParams(layoutParams);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.GONE);
                    tagIds.remove(v.getTag());
                }
            });
            llTagUsers.addView(textView);
        }else{ // Mention user

            // check it's a card status or not
            if (statusType.equals("CARD")){ // Card status
                etStatusCard.setText( etStatusCard.getText().toString() + username + " " );
                etStatusCard.setSelection( etTagPeople.getText().length() );
            }else { // Other than card status
                etDescription.setText( etDescription.getText().toString() + username + " " );
                etDescription.setSelection( etDescription.getText().length() );
                etDescription.requestFocus();
            }

        }
        /*mentionCount++;
        etTagPeople.setText(etTagPeople.getText().toString().replace(mentionName, username+",@"));
        etTagPeople.setSelection(matcherStart+username.length()+2);*/
    }

    private void showMentionAdapter(){
        mentionAdapterActive = true;
        svMainView.setVisibility(View.GONE);
        llMentionView.setVisibility(View.VISIBLE);
        etTagPeople.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etTagPeople, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    private void hideMentionAdapter(){
        mentionAdapterActive = false;
        svMainView.setVisibility(View.VISIBLE);
        llMentionView.setVisibility(View.GONE);
        etTagPeople.requestFocus();
    }

    private void loadThumbnailPhoto() {
        ivPhoto.setScaleX(0);
        ivPhoto.setScaleY(0);
        Glide.with(this)
                .load(mediaUri)
                .into(ivPhoto);
        ivPhoto.animate()
                .scaleX(1.f).scaleY(1.f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(400)
                .setStartDelay(200)
                .start();
    }

    private void bringMainActivityToTop() {
        if (postMode.equals("PRIVATE")) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setAction(HomeActivity.ACTION_SHOW_LOADING_ITEM);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, PublicActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setAction(HomeActivity.ACTION_SHOW_LOADING_ITEM);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_TAKEN_MEDIA_URI, mediaUri);
    }

    public static void startStatusPublishFromLocation(int[] startingLocation, Activity startingActivity, String mode, String type) {
        Intent intent = new Intent(startingActivity, PublishActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra("USER_STATUS_MODE", mode);
        intent.putExtra("USER_STATUS_PRIVATE_PUBLIC_TYPE", type);
        startingActivity.startActivity(intent);
    }
    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            if(getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION) != null) {
                final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
                vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                        vRevealBackground.startFromLocation(startingLocation);
                        return true;
                    }
                });
            }else{
                vRevealBackground.setToFinishedFrame();
            }
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }

    @Override
    public void onBackPressed() {
        if (mentionAdapterActive){
            hideMentionAdapter();
        }else  {
            if(getIntent().getStringExtra("USER_STATUS_MODE").equals("photo")){
                startActivity(new Intent(this, SelectStatusActivity.class));
            }else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            llMainLayout.setVisibility(View.VISIBLE);
        } else {
            llMainLayout.setVisibility(View.GONE);
        }
    }

    private void showLoading(){
        if (vLoadingView == null)
            vLoadingView = ((ViewStub) findViewById(R.id.vsPublishMentionLoading)).inflate();
        else
            vLoadingView.setVisibility(View.VISIBLE);
    }
    private void hideLoading(){
        if (vLoadingView != null)
            vLoadingView.setVisibility(View.GONE);
    }

    private void showNothingFound(){
        if (vNothingFoundView == null) {
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsPublishMentionNothingFound)).inflate();
        }else {
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }

}
