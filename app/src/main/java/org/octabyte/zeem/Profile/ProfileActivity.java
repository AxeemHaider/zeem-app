package org.octabyte.zeem.Profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.ChatMessage;
import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.appspot.octabyte_zeem.zeem.model.PostFeed;
import com.appspot.octabyte_zeem.zeem.model.ProfileView;
import com.appspot.octabyte_zeem.zeem.model.Relationship;
import com.appspot.octabyte_zeem.zeem.model.TaskComplete;
import com.appspot.octabyte_zeem.zeem.model.User;
import com.appspot.octabyte_zeem.zeem.model.UserProfile;
import org.octabyte.zeem.API.FriendTask;
import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Comment.CommentActivity;
import org.octabyte.zeem.Friend.FriendActivity;
import org.octabyte.zeem.Home.FeedAdapter;
import org.octabyte.zeem.Home.FeedContextMenu;
import org.octabyte.zeem.Home.FeedContextMenuManager;
import org.octabyte.zeem.Home.FeedItemAnimator;
import org.octabyte.zeem.Like.LikeActivity;
import org.octabyte.zeem.List.ShowListActivity;
import org.octabyte.zeem.Utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.octabyte.zeem.R;

import java.util.ArrayList;
import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;


/**
 * Created by Azeem on 8/1/2017.
 */

public class ProfileActivity extends AppCompatActivity implements
        RevealBackgroundView.OnStateChangeListener,
        PostGridAdapter.OnGridImageClickListener,
        FeedAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener,
        View.OnClickListener
{
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";
    public static final String USER_PROFILE_ID = "user_profile_id";
    public static final String USER_NAME = "user_name";

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    private RevealBackgroundView vRevealBackground;

    private RecyclerView
            rvGridProfile,
            rvListProfile,
            rvSavedPosts;

    private ImageView
            ivUserProfilePhoto,
            ivAddFriend,
            ivUserOption;

    private View
            vUserDetails,
            vUserStats,
            vUserProfileRoot;

    private TextView
            tvUserFullName,
            tvUserStatus,
            tvPostCount,
            tvPostCountBottom,
            tvFollowerCount,
            tvFollowingCount,
            tvStarCount;


    private Button btnUserAction;

    private LinearLayout llUserFollowing;


    private String textEditProfile;
    private String textBlock;
    private String textUnBlock;
    private String textFollow;
    private String textUnFollow;

    private PostGridAdapter postGridAdapter;
    private FeedAdapter postListAdapter;

    private Boolean loadSavedPost = true;
    private int position = -1;

    private int scrollPosition;
    private Boolean isUsername = false;
    private String profileUsername;

    private Long myUserId;
    private Long userId = null;
    private UserProfile mUserProfile;
    private Boolean isMe;
    private Boolean gotRelationship = false;
    private Relationship relationshipData;
    private Boolean gotProfileInfo = false;
    private Boolean isFollowing = false;
    private String myUsername;
    private SharedPreferences sharedPreferences;
    private String userProfilePic;
    private User user;
    private ImageButton ibUserBadge, ibSavedPost, ibInstantChat;
    private boolean isLinearView = false;
    private View vLoadingView, vNoInternetView, vNothingFoundView, vNoPostView, vNoSavedPostView;
    private boolean anonymousChat = false;
    private String receiverToken;
    public static boolean INSTANT_EDIT_PROFILE = false;
    public static String INSTANT_PROFILE_LOCAL_PIC;
    private boolean saveContactInMobile = true;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] writeContactPermission = new String[]{Manifest.permission.WRITE_CONTACTS};

    //private StaggeredGridLayoutManager layoutManager;

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity, String profileId) {
        Intent intent = new Intent(startingActivity, ProfileActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra(USER_PROFILE_ID, profileId);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        initVariable();

        Log.w("ActivityCycle", "profile activity create");
        relationshipData = new Relationship();

        profileUsername = getIntent().getStringExtra(USER_PROFILE_ID);

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);

        myUserId = sharedPreferences.getLong("userId", 123L);
        myUsername = sharedPreferences.getString("username", null);

        if(profileUsername.startsWith("@")) {
            isUsername = true;
            profileUsername = profileUsername.substring(1);
        }else {
            userId = Long.valueOf(profileUsername);
            // get Long value from this string and save it into userId
        }

        Resources r = this.getResources();
        textEditProfile = r.getString(R.string.edit_profile);
        textBlock = r.getString(R.string.block);
        textUnBlock = r.getString(R.string.un_block);
        textFollow = r.getString(R.string.follow);
        textUnFollow = r.getString(R.string.un_follow);

        getResponse();
        setupRevealBackground(null);
    }
    private void initVariable(){
        ibUserBadge = findViewById(R.id.ibUserBadge);
        ibSavedPost = findViewById(R.id.ibSavedPost);
        ibInstantChat = findViewById(R.id.ibInstantChat);

        ibUserBadge.setOnClickListener(this);
        ibSavedPost.setOnClickListener(this);
        ibInstantChat.setOnClickListener(this);

        vRevealBackground = findViewById(R.id.vRevealBackground);
        rvGridProfile = findViewById(R.id.rvProfileGridView);
        rvListProfile = findViewById(R.id.rvProfileListView);
        ivUserProfilePhoto = findViewById(R.id.ivUserProfilePhoto);
        ivUserProfilePhoto.setOnClickListener(this);
        vUserDetails = findViewById(R.id.vUserDetails);
        vUserStats = findViewById(R.id.vUserStats);
        vUserProfileRoot = findViewById(R.id.vUserProfileRoot);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        btnUserAction = findViewById(R.id.btnUserAction);
        btnUserAction.setOnClickListener(this);
        LinearLayout llUserPostsCount = findViewById(R.id.llUserPostsCount);
        llUserPostsCount.setOnClickListener(this);
        LinearLayout llFollowersCount = findViewById(R.id.llFollowersCount);
        llFollowersCount.setOnClickListener(this);
        tvPostCount = findViewById(R.id.tvPostCount);
        tvPostCountBottom = findViewById(R.id.tvPostCountBottom);
        tvFollowerCount = findViewById(R.id.tvFollowerCount);
        llUserFollowing = findViewById(R.id.llUserFollowing);
        llUserFollowing.setOnClickListener(this);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvStarCount = findViewById(R.id.tvStarCount);
        ivAddFriend = findViewById(R.id.ivAddFriend);
        ivUserOption = findViewById(R.id.ivUserOption);
        rvSavedPosts = findViewById(R.id.rvSavedPosts);

        ivAddFriend.setOnClickListener(this);
        ivUserOption.setOnClickListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("ActivityCycle", "profile activity pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("ActivityCycle", "profile activity resume");
        if (INSTANT_EDIT_PROFILE){

            String name = sharedPreferences.getString("fullName", null);
            String status = sharedPreferences.getString("status", null);

            if (INSTANT_PROFILE_LOCAL_PIC != null){
                Glide.with(this).load(INSTANT_PROFILE_LOCAL_PIC)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivUserProfilePhoto);
            }

            tvUserFullName.setText(name);
            tvUserStatus.setText(status);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w("ActivityCycle", "profile activity stop");
    }

    private void getResponse(){

        // First of all check it's my id or not if username check it's my username or not
         isMe = false;
        // Check it's a username or id
        if (isUsername){ // It's a username
            // Check it's me or not
            // it's me
            // someone other
            isMe = profileUsername.equals(myUsername);
        }else{ // It's an user Id
            // Check it's me or not
            // it's me
            // someone else
            isMe = userId.equals(myUserId);
        }

        if (isMe){

            ivUserProfilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                }
            });

            relationshipData.setRelation("ME");
            relationshipData.setFollowing(false);
            isUsername = false; // Because now I have user Id
            userId = myUserId;

            // Create user object and set user info
            user = new User();
            user.setUserId(myUserId);
            user.setUsername(myUsername);
            user.setFullName(sharedPreferences.getString("fullName", null));
            user.setProfilePic(sharedPreferences.getString("profilePic", null));
            user.setBadge(sharedPreferences.getInt("badge", 0));

            // Save user profile pic for further use
            userProfilePic = sharedPreferences.getString("profilePic", null);

            // Show Save post Button if it's me
            ibSavedPost.setVisibility(View.VISIBLE);

            ibSavedPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSavedPosts();
                }
            });

        }

        getProfileView();


        // For offline Feeds
        /*InputStream raw = getResources().openRawResource(R.raw.posts);
        Reader rd = new BufferedReader(new InputStreamReader(raw));
        Gson gson = new Gson();
        postResponse = gson.fromJson(rd, PostResponse.class);
        postItems = postResponse.getUserPosts();

        setupProfileInfo();*/
        setupUserProfileGrid();
    }

    private void showUserInfoFirstTime(boolean isMe){
        boolean firstTime = sharedPreferences.getBoolean("UserProfileFirstTime", true);

        if (firstTime && isMe) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(R.id.ibSavedPost, this))
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(R.string.info_profile_saved_title)
                    .setContentText(R.string.info_profile_saved_detail)
                    .replaceEndButton(R.layout.show_case_hide_button)
                    .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            infoProfileBadgeFirstTime();
                        }
                    })
                    .build();
        }

    }
    private void infoProfileBadgeFirstTime(){
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.ibUserBadge, this))
                .withHoloShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.info_profile_badge_title)
                .setContentText(R.string.info_profile_badge_detail)
                .replaceEndButton(R.layout.show_case_hide_button)
                .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        infoAddFriendFirstTime();
                    }
                })
                .build();

    }
    private void infoAddFriendFirstTime(){
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.ivAddFriend, this))
                .withHoloShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.info_add_friend_title)
                .setContentText(R.string.info_add_friend_msg_detail)
                .replaceEndButton(R.layout.show_case_hide_button)
                .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("UserProfileFirstTime", false);
                        editor.apply();
                    }
                })
                .build();

    }

    private void showChatInfoFirstTime(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean firstTime = sharedPreferences.getBoolean("UserChatFirstTime", true);

                if (firstTime) {
                    new ShowcaseView.Builder(ProfileActivity.this)
                            .setTarget(new ViewTarget(R.id.ibInstantChat, ProfileActivity.this))
                            .withHoloShowcase()
                            .setStyle(R.style.CustomShowcaseTheme)
                            .setContentTitle(R.string.info_profile_chat_title)
                            .setContentText(R.string.info_profile_chat_detail)
                            .replaceEndButton(R.layout.show_case_hide_button)
                            .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                                @Override
                                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("UserChatFirstTime", false);
                                    editor.apply();
                                }
                            })
                            .build();
                }
            }
        }, 1000);

    }

    private void getProfileView(){
        if (Utils.isNetworkAvailable(this)) {
            showLoading();

            UserTask<ProfileView> userTask = new UserTask<>();
            userTask.setMyUserId(myUserId);
            userTask.setUserId(userId);
            userTask.execute("getProfileView");
            userTask.setListener(new UserTask.Response<ProfileView>() {
                @Override
                public void response(ProfileView response) {
                    hideLoading();

                    if (response != null){

                        mUserProfile = response.getUserProfile();

                        if (response.getUser() != null){
                            setupUserInfo(response.getUser());
                            receiverToken = response.getUser().getFirebaseToken();
                        }else {
                            setupUserInfo(user);
                        }

                        if (response.getRelationship() != null) {
                            setupProfileInfo(response.getRelationship());
                        }else {
                            setupProfileInfo(relationshipData);
                        }

                        if (response.getPost().getFeedList() != null){
                            hideNoPosts();
                            hideNoSavedPosts();

                            setupGridView(response.getPost().getFeedList());
                            setupLinearView(response.getPost().getFeedList());
                        }else{
                            showNoPosts();
                        }

                        animateUserProfileHeader();
                    }else{
                        Log.w("APIDebugging", "null response in ProfileActivity->getProfileView");
                        showNothingFound();
                    }
                }
            });
        } else {
            showNoInternet();
        }
    }

    private void setupUserInfo(User user){
        userId = user.getUserId();

        // Save profile pic for further use
        userProfilePic = user.getProfilePic();

        Glide.with(this).load(user.getProfilePic())
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                .apply(RequestOptions.circleCropTransform())
                .into(ivUserProfilePhoto);

        tvUserFullName.setText(Utils.capitalize(user.getFullName()));

        ibUserBadge.setImageResource(Utils.getBadgePic(user.getBadge()));

    }



    private void setupProfileInfo(Relationship relationship){

        tvUserStatus.setText(mUserProfile.getStatus());
        tvFollowerCount.setText(String.valueOf(mUserProfile.getFollowerCount()));
        tvStarCount.setText(String.valueOf(mUserProfile.getStarCount()));

        switch (relationship.getRelation()) {
            case "ME":
                tvPostCount.setText(String.valueOf(mUserProfile.getFriendCount()));
                tvPostCountBottom.setText("friends");
                btnUserAction.setText(textEditProfile);
                tvFollowingCount.setText(String.valueOf(mUserProfile.getFollowingCount()));
                ivAddFriend.setVisibility(View.VISIBLE);
                ivUserOption.setVisibility(View.VISIBLE);
                break;
            case "FRIEND":
                ibInstantChat.setVisibility(View.VISIBLE);
                showChatInfoFirstTime();
                tvPostCount.setText(String.valueOf(mUserProfile.getPostCount()));
                tvFollowingCount.setText(String.valueOf(mUserProfile.getFollowingCount()));
                btnUserAction.setText(textBlock);
                break;
            case "NO_RELATION":
                btnUserAction.setText(textFollow);
            case "FOLLOWER":
                llUserFollowing.setVisibility(View.GONE);
                tvPostCount.setText(String.valueOf(mUserProfile.getPostCount()));
                // If it's my follower and I'm also following him then show textUnFollow else show text Follow
                if (relationship.getFollowing()){
                    btnUserAction.setText(textUnFollow);
                }else {
                    btnUserAction.setText(textFollow);
                }
                break;
            case "FOLLOWING":
                btnUserAction.setText(textUnFollow);
                tvPostCount.setText(String.valueOf(mUserProfile.getPostCount()));
                tvFollowingCount.setText(String.valueOf(mUserProfile.getFollowingCount()));
        }

    }

    @Override
    public void onBackPressed() {
        if (isLinearView){
            showGridView();
        }else {
            super.onBackPressed();
        }
    }

    private void showSavedPosts(){
        if(loadSavedPost){

            if (Utils.isNetworkAvailable(this)) {
                showLoading();

                UserTask<PostFeed> savedPosts = new UserTask<>(myUserId);
                savedPosts.execute("getSavedPost");
                savedPosts.setListener(new UserTask.Response<PostFeed>() {
                    @Override
                    public void response(PostFeed response) {
                        hideLoading();
                        hideNoSavedPosts();
                        hideNoPosts();

                        if (response.getFeedList() != null){
                            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProfileActivity.this) {
                                @Override
                                protected int getExtraLayoutSpace(RecyclerView.State state) {
                                    return 300;
                                }
                            };
                            rvSavedPosts.setLayoutManager(linearLayoutManager);

                            FeedAdapter savedPostsAdapter = new FeedAdapter(ProfileActivity.this, response.getFeedList(), linearLayoutManager, ProfileActivity.this);
                            savedPostsAdapter.setOnFeedItemClickListener(ProfileActivity.this);
                            rvSavedPosts.setAdapter(savedPostsAdapter);
                            rvSavedPosts.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                    FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
                                }
                            });
                            rvSavedPosts.setItemAnimator(new FeedItemAnimator());
                        }else{
                            showNoSavedPosts();
                            Log.w("APIDebugging", "null response in ProfileActivity->showSavedPosts");
                        }
                    }
                });
                loadSavedPost = false;
            } else {
                Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
            }

        }
        rvGridProfile.setVisibility(View.GONE);
        rvListProfile.setVisibility(View.GONE);
        rvSavedPosts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBottomReached(int position) {
        // If cursor is not null then get more feeds

    }

    private void addFriend(){

        // Open a dialog to get name and phone from user
        final Dialog dialog = new Dialog(this);
        try {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        dialog.setContentView(R.layout.dialog_new_contact);
        dialog.show();

        Button btnNewContact = dialog.findViewById(R.id.btnContactAdd);
        btnNewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ( (EditText) dialog.findViewById(R.id.etContactName) ).getText().toString();
                String phone = ( (EditText) dialog.findViewById(R.id.etContactPhone)).getText().toString();

                // Make sure these fields are not empty
                if (!name.isEmpty() && !phone.isEmpty() ){

                    if (saveContactInMobile){
                        if(ActivityCompat.checkSelfPermission(ProfileActivity.this, writeContactPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                            checkRuntimePermission();
                            return;
                        }
                    }

                    try {
                        phone = phone.substring(phone.length() - 10);
                    } catch (StringIndexOutOfBoundsException e) {
                        e.printStackTrace();

                        Toast.makeText(ProfileActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();

                        return;
                    } catch (Exception e){
                        e.printStackTrace();

                        Toast.makeText(ProfileActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    dialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Contact added", Toast.LENGTH_SHORT).show();

                    // Add new Friend
                    FriendTask<Void> friendTask = new FriendTask<>(userId);
                    friendTask.setUserPhone(sharedPreferences.getLong("phone", 0L));
                    friendTask.setFriendPhone(Long.valueOf(phone));
                    friendTask.execute("addFriend");

                    // Create new Contact
                    if (saveContactInMobile) {
                        createNewContact(name, phone);
                    }

                    saveContactInMobile = true;

                }else{
                    Toast.makeText(ProfileActivity.this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void checkBoxChecked(View view){
        CheckBox checkBox = (CheckBox) view;
        saveContactInMobile = checkBox.isChecked();
    }


    private void checkRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, writeContactPermission[0]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,writeContactPermission[0])){
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_write_contact_title);
                builder.setMessage(R.string.permission_write_contact_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ProfileActivity.this,writeContactPermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (sharedPreferences.getBoolean("write_contact_permission",false)) {
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
                ActivityCompat.requestPermissions(this,writeContactPermission,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("write_contact_permission",true);
            editor.apply();
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

                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,writeContactPermission[0])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_write_contact_title);
                builder.setMessage(R.string.permission_write_contact_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ProfileActivity.this,writeContactPermission,PERMISSION_CALLBACK_CONSTANT);
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
            if (ActivityCompat.checkSelfPermission(this, writeContactPermission[0]) == PackageManager.PERMISSION_GRANTED) {

                //Got Permission
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();

            }
        }
    }


    private void setupUserProfileGrid() {
        final StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvGridProfile.setLayoutManager(gridLayoutManager);
        rvGridProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (postGridAdapter != null) {
                    postGridAdapter.setLockedAnimations(true);
                }
            }
        });
    }

    private void setupGridView(List<FeedItem> postFeeds){
        postGridAdapter = new PostGridAdapter(this, postFeeds);
        postGridAdapter.setOnGridImageClickListener(ProfileActivity.this);
        rvGridProfile.setAdapter(postGridAdapter);
    }
    private void setupLinearView(List<FeedItem> postFeeds){
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        rvListProfile.setLayoutManager(linearLayoutManager);

        postListAdapter = new FeedAdapter(this, postFeeds, linearLayoutManager, this);
        postListAdapter.setOnFeedItemClickListener(ProfileActivity.this);
        rvListProfile.setAdapter(postListAdapter);
        rvListProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
            }
        });
        rvListProfile.setItemAnimator(new FeedItemAnimator());
    }

    private void showGridView(){
        isLinearView = false;
        rvSavedPosts.setVisibility(View.GONE);
        rvGridProfile.setVisibility(View.VISIBLE);
        rvListProfile.setVisibility(View.GONE);
        scrollPosition = ((LinearLayoutManager) rvListProfile.getLayoutManager()).findFirstVisibleItemPosition();
        rvGridProfile.scrollToPosition(scrollPosition);
    }
    private void showLinearView(int position){
        isLinearView = true;
        rvSavedPosts.setVisibility(View.GONE);
        rvGridProfile.setVisibility(View.GONE);
        rvListProfile.setVisibility(View.VISIBLE);
        scrollPosition = position;
        rvListProfile.scrollToPosition(scrollPosition);
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
            if (postGridAdapter != null) {
                vRevealBackground.setToFinishedFrame();
                postGridAdapter.setLockedAnimations(true);
            }
        }
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            rvGridProfile.setVisibility(View.VISIBLE);
            vUserProfileRoot.setVisibility(View.VISIBLE);

            /*setupGridView();
            setupLinearView();*/

            //animateUserProfileHeader();
            animateHideProfileHeader();
        } else {
            rvGridProfile.setVisibility(View.INVISIBLE);
            vUserProfileRoot.setVisibility(View.INVISIBLE);
        }
    }

    private void animateHideProfileHeader(){
        vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
        ivUserProfilePhoto.setTranslationY(-ivUserProfilePhoto.getHeight());
        vUserDetails.setTranslationY(-vUserDetails.getHeight());
        vUserStats.setAlpha(0);
    }
    private void animateUserProfileHeader() {
        vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
        vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showUserInfoFirstTime(isMe);
            }
        },1000);
    }

    @Override
    public void onImageClick(int position) {
        showLinearView(position);
    }

    @Override
    public void onCommentsClick(View v, Long postId, String postSafeKey, String postMode) {
        final Intent intent = new Intent(this, CommentActivity.class);
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(CommentActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        intent.putExtra(CommentActivity.POST_ID, postId);
        intent.putExtra("POST_SAFE_KEY", postSafeKey);
        intent.putExtra("ARG_POST_MODE", postMode);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onMoreClick(
            View v, String postSafeKey, String feedSafeKey, Long userId, Long postId, String postMode,
            Boolean tagged, Boolean tagApproved, int adapterPosition) {
        position = adapterPosition;
        Boolean isPublic = false;
        if (postMode.equals("PUBLIC")) {
            isPublic = true;
        }
        FeedContextMenuManager.getInstance().toggleContextMenuFromView(v, postSafeKey, feedSafeKey, userId, postId, isPublic, tagged, tagApproved, this);
    }

    @Override
    public void onLikesClick(View v, String postId) {
        final Intent intent = new Intent(this, LikeActivity.class);
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(LikeActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        intent.putExtra(LikeActivity.POST_ID, postId);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onListClick(View v, Long listId, Boolean list) {
        final Intent intent = new Intent(this, ShowListActivity.class);
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(ShowListActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        intent.putExtra(ShowListActivity.LIST_ID, listId);
        startActivity(intent);
        overridePendingTransition(0, 0);
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
    public void onReportClick(final String postSafeKey) {
        // what kind of this report and send to the server Here SPAM CONTENT is used as example
        FeedContextMenuManager.getInstance().hideContextMenu();

        final Dialog dialog = new Dialog(this);

        try {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        dialog.setContentView(R.layout.dialog_post_report);

        dialog.show();

        TextView reportSpam = dialog.findViewById(R.id.reportSpam);
        reportSpam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(myUserId, postSafeKey, "SPAM");
                postReport.execute("report");
            }
        });
        TextView reportAntiReligion = dialog.findViewById(R.id.reportAntiReligion);
        reportAntiReligion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(myUserId, postSafeKey, "ANTI_RELIGION");
                postReport.execute("report");
            }
        });
        TextView reportSexualContent = dialog.findViewById(R.id.reportSexualContent);
        reportSexualContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(myUserId, postSafeKey, "SEXUAL_CONTENT");
                postReport.execute("report");
            }
        });
        TextView reportOther = dialog.findViewById(R.id.reportOther);
        reportOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(myUserId, postSafeKey, "OTHER");
                postReport.execute("report");
            }
        });

    }


    @Override
    public void onApproveTagClick(final Long postId, final Boolean isPublic) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        postListAdapter.feedItems.get(position).setTaggedApproved(true);

        PostTask<Void> approveTag = new PostTask<>(myUserId, postId, isPublic);
        approveTag.execute("tagApproved");

    }

    @Override
    public void onRemoveTagClick(String postId) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onHidePostClick(String feedSafeKey) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        if(position != -1) {
            postListAdapter.feedItems.remove(position);
            postListAdapter.notifyItemRemoved(position);
        }
        UserTask<Void> userTask = new UserTask<>();
        userTask.setFeedSafeKey(feedSafeKey);
        userTask.execute("deleteFeed");
    }

    @Override
    public void onDeletePostClick(String postSafeKey) {
        PostTask<Void> postTask = new PostTask<>(postSafeKey);
        postTask.execute("deletePost");
    }

    @Override
    public void onSavePostClick(String postSafeKey) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        PostTask<Void> savePost = new PostTask<>(myUserId);
        savePost.setPostSafeKey(postSafeKey);
        savePost.execute("savePost");
        Toast.makeText(this, "Post Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelClick(String postId) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    private void openOptionActivity(){
        startActivity(new Intent(this, ProfileOptionActivity.class));
    }

    private void openChatDialog(){

        anonymousChat = false;

        final Dialog dialog = new Dialog(this);

        try {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        dialog.setContentView(R.layout.dialog_instant_chat);

        final Switch chatIdentity = (Switch) dialog.findViewById(R.id.switchChatIdentity);
        chatIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anonymousChat = chatIdentity.isChecked();
            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.btnChatCancel);
        Button btnRequest = (Button) dialog.findViewById(R.id.btnChatRequest);

        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                sendChatRequest();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void sendChatRequest(){

        String chatAnonymous = "chat_anonymous";

        String appUserToken = sharedPreferences.getString("firebaseToken", null);
        String userFullname = sharedPreferences.getString("fullName", "Someone");
        String profilePic = sharedPreferences.getString("profilePic", null);
        Long appUserID = sharedPreferences.getLong("userId", 0L);

        if (anonymousChat){
            userFullname = "Anonymous Person";
            profilePic = "anonymous";
            chatAnonymous = appUserID + "_" + "true";
        }

        String messageText = Utils.capitalize(userFullname) + " wants to chat with you";

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageText(messageText);
        chatMessage.setSenderToken(appUserToken);
        chatMessage.setReceiverToken(receiverToken);
        chatMessage.setProfilePic(profilePic);
        chatMessage.setUsername(Utils.capitalize(userFullname));
        chatMessage.setIsAnonymous(String.valueOf(anonymousChat));
        chatMessage.setMessageReceived(String.valueOf(false));
        chatMessage.setChatAnonymous(chatAnonymous);
        chatMessage.setChatTitle("Chat Request");

        UserTask<TaskComplete> userTask = new UserTask<>();
        userTask.setChatMessage(chatMessage);
        userTask.setListener(new UserTask.Response<TaskComplete>() {
            @Override
            public void response(TaskComplete response) {
                if (response != null){
                    if (response.getComplete()){
                        Log.w("InstantChat", "Successfully chat request send");
                        Toast.makeText(ProfileActivity.this, "Chat request send", Toast.LENGTH_SHORT).show();
                    }else{
                        Log.e("InstantChat", "Chat request sending fail");
                        Toast.makeText(ProfileActivity.this, "Chat request sending fail", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.e("InstantChat", "null response in ProfileActivity -> sendChatRequest");
                    Log.e("InstantChat", "Chat request sending fail");
                }
            }
        });
        userTask.execute("sendChatMessage");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibInstantChat:
                openChatDialog();
                break;
            case R.id.btnUserAction:
                String s = btnUserAction.getText().toString();
                if (s.equals(textEditProfile)) {
                    startActivity(new Intent(this, EditProfileActivity.class));

                } else if (s.equals(textBlock)) {// send user_id and block this user for that person
                    // change the user action button
                    FriendTask<Void> friendTask = new FriendTask<>(myUserId);
                    friendTask.setFriendId(userId);
                    friendTask.execute("blockFriend");
                    btnUserAction.setText(R.string.un_block);

                } else if (s.equals(textUnBlock)) {// send user_id and block this user for that person
                    // change the user action button
                    FriendTask<Void> friendTask = new FriendTask<>(myUserId);
                    friendTask.setFriendId(userId);
                    friendTask.execute("unBlockFriend");
                    btnUserAction.setText(R.string.block);

                } else if (s.equals(textFollow)) {// send user_id and block this user for that person
                    // change the user action button
                    FriendTask<Void> friendTask = new FriendTask<>(myUserId);
                    friendTask.setFriendId(userId);
                    friendTask.execute("follow");
                    btnUserAction.setText(R.string.un_follow);

                } else if (s.equals(textUnFollow)) {// send user_id and block this user for that person
                    // change the user action button
                    FriendTask<Void> friendTask = new FriendTask<>(myUserId);
                    friendTask.setFriendId(userId);
                    friendTask.execute("unFollow");
                    btnUserAction.setText(R.string.follow);
                }
                break;
            case R.id.ivUserOption:
                openOptionActivity();
                break;
            case R.id.ivAddFriend:
                addFriend();
                break;
            case R.id.ibUserBadge:
                if (relationshipData.getRelation() != null) {
                    if(relationshipData.getRelation().equals("ME")) {
                        final Dialog dialog = new Dialog(this);

                        try {
                            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                        dialog.setContentView(R.layout.dialog_badge);

                        ImageView dialogBadge = dialog.findViewById(R.id.ivDialogBadge);
                        TextView badgeInfo = dialog.findViewById(R.id.tvDialogBadgeInfo);

                        dialogBadge.setImageResource(Utils.getBadgePic(user.getBadge()+1)); // +1 for next badge
                        badgeInfo.setText(String.format(getString(R.string.dialog_badge_info),
                                Utils.getBadgeText(user.getBadge()),
                                Utils.getBadgeText(user.getBadge() + 1),
                                Utils.getBadgeStar(user.getBadge())));

                        dialog.show();
                        Button gotIt = dialog.findViewById(R.id.btnDialogBadgeGotit);
                        gotIt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                }
                break;
            case R.id.llUserPostsCount:
                if (relationshipData.getRelation() != null) {
                    if(relationshipData.getRelation().equals("ME")){
                        Intent friendIntent = new Intent(this, FriendActivity.class);
                        friendIntent.putExtra("REQUEST_MODE", "friends");
                        startActivity(friendIntent);
                    }
                }
                break;
            case R.id.llFollowersCount:
                if (relationshipData.getRelation() != null) {
                    if(relationshipData.getRelation().equals("ME")){
                        Intent followerIntent = new Intent(this, FriendActivity.class);
                        followerIntent.putExtra("REQUEST_MODE", "followers");
                        startActivity(followerIntent);
                    }
                }
                break;
            case R.id.llUserFollowing:
                if (relationshipData.getRelation() != null) {
                    if(relationshipData.getRelation().equals("ME")){
                        Intent followingIntent = new Intent(this, FriendActivity.class);
                        followingIntent.putExtra("REQUEST_MODE", "following");
                        startActivity(followingIntent);
                    }
                }
                break;
        }
    }

    private void createNewContact(String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        name) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()); // Type of mobile number

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsProfileLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsProfileNoInternet)).inflate();
            Button btnInternetRetry = findViewById(R.id.btnInternetRetry);
            btnInternetRetry.setOnClickListener(this);
        }else{
            vNoInternetView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNoInternet(){
        if (vNoInternetView != null)
            vNoInternetView.setVisibility(View.GONE);
    }
    private void showNothingFound(){
        if (vNothingFoundView == null) {
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsProfileNothingFound)).inflate();
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
    private void showNoPosts(){
        if (vNoPostView == null) {
            vNoPostView = ((ViewStub) findViewById(R.id.vsProfileNoPost)).inflate();
        }else{
            vNoPostView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNoPosts(){
        if (vNoPostView != null) vNoPostView.setVisibility(View.GONE);
    }
    private void showNoSavedPosts(){
        if (vNoSavedPostView == null) {
            vNoSavedPostView = ((ViewStub) findViewById(R.id.vsProfileNoSavedPost)).inflate();
        }else{
            vNoSavedPostView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNoSavedPosts(){
        if (vNoSavedPostView != null) vNoSavedPostView.setVisibility(View.GONE);
    }


}
