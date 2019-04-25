package org.octabyte.zeem.Home;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.appspot.octabyte_zeem.zeem.model.PostFeed;
import com.appspot.octabyte_zeem.zeem.model.Stories;
import com.appspot.octabyte_zeem.zeem.model.Story;
import com.appspot.octabyte_zeem.zeem.model.StoryFeed;
import com.appspot.octabyte_zeem.zeem.model.TaskComplete;
import com.appspot.octabyte_zeem.zeem.model.User;
import com.appspot.octabyte_zeem.zeem.model.UserAlert;
import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.API.StoryTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Camera.CameraActivity;
import org.octabyte.zeem.Comment.CommentActivity;
import org.octabyte.zeem.Like.LikeActivity;
import org.octabyte.zeem.List.ShowListActivity;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Search.DiscoverActivity;
import org.octabyte.zeem.Utils.BottomNavigationSetup;
import org.octabyte.zeem.Utils.CloudStorage;
import org.octabyte.zeem.Utils.FocusView;
import org.octabyte.zeem.Utils.GPSTracker;
import org.octabyte.zeem.Utils.InformerItem;
import org.octabyte.zeem.Utils.StorageInformer;
import org.octabyte.zeem.Utils.Utils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;
import static org.octabyte.zeem.Home.StorySlide.STORY_SLIDES;
import static org.octabyte.zeem.Notification.NotificationActivity.NEW_NOTIFICATION;
import static org.octabyte.zeem.Publish.PublishActivity.newFeedItem;

/**
 * Created by Azeem on 8/1/2017.
 */

public class PublicActivity extends AppCompatActivity implements FeedAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener, View.OnClickListener, StoryAdapter.OnStoryItemClickListener
{

    private static Boolean IS_APPLICATION_ACTIVE = false;

    public static final String ACTION_SHOW_LOADING_ITEM = "action_show_loading_item";

    private static final int ANIM_DURATION_FAB = 400;

    private RecyclerView rvFeed;
    private RecyclerView rvStoris;
    private FloatingActionButton btnGoToTop;

    private FeedAdapter feedAdapter;
    private List<FeedItem> feedItems;
    private int position = -1;

    private List<StoryFeed> storyItems = new ArrayList<>();
    private StoryAdapter storyAdapter;

    private SharedPreferences sharedPreferences;

    private Long APP_USER_ID;
    private String feedCursor;
    private String mode;
    private BottomNavigationView bottomNavigation;
    private ImageView ivAppbarPrivate;
    private View vLoadingView;
    private View vNothingFoundView;
    private View vNoInternetView;
    private ImageView ivAppbarUser;

    private static Long newlyPublicPostedPostId;
    private ImageView ivAppbarPublic;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initVariable();

        Boolean storyThoughThis = getIntent().getBooleanExtra("ARG_STORY_THROUGH_THIS", false);

        if (storyThoughThis){
            int[] startingLocation = getIntent().getIntArrayExtra("ARG_STORY_LOCATION_THROUGH_THIS");
            CameraActivity.startCameraFromLocation(startingLocation, this, false);
            overridePendingTransition(0, 0);
        }

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        APP_USER_ID = sharedPreferences.getLong("userId", 123L);
        String appUserProfilePic = sharedPreferences.getString("profilePic", null);


        if (appUserProfilePic != null) {
            Glide.with(this).load(appUserProfilePic)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform()).into(ivAppbarUser);
        }

        mode = "PUBLIC";

        setupFeed();

        IS_APPLICATION_ACTIVE = true;

        hideBtnGoToTop();

        BottomNavigationSetup.init(bottomNavigation, PublicActivity.this, 0, mode);

        setupTimerForUpdation();


        // Start a new local receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(newPostReceiver, new IntentFilter("new-post-local-receiver"));

    }

    private void initVariable() {
        rvFeed = findViewById(R.id.rvFeed);
        rvStoris = findViewById(R.id.rvStories);
        btnGoToTop = findViewById(R.id.btnGoToTop);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        ivAppbarPublic = findViewById(R.id.ivAppbarPublic);
        ivAppbarPrivate = findViewById(R.id.ivAppbarPrivate);
        ImageView ivAppbarExplorer = findViewById(R.id.ivAppbarExplorer);
        ivAppbarUser = findViewById(R.id.ivAppbarUser);

        btnGoToTop.setOnClickListener(this);
        ivAppbarPrivate.setOnClickListener(this);
        ivAppbarExplorer.setOnClickListener(this);
        ivAppbarUser.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ivAppbarPublic.setImageResource(R.drawable.ic_header_public_active);
        bottomNavigation.getMenu().getItem(0).setChecked(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (timer != null){
            timer.cancel();
            timer.purge();
            timer = null;
        }

        if (bottomNavigation != null)
            bottomNavigation.getMenu().findItem(R.id.navNotification).setIcon(R.drawable.ic_bottom_notification);

        // Check if feedAdapter in not null then save feeds offline
        if (feedAdapter != null) {
            // Create Feed object
            PostFeed postFeed = new PostFeed();
            postFeed.setCursor(feedCursor);
            postFeed.setFeedList(feedAdapter.feedItems);

            // ### Save into Local file

            // Create jackson factory
            JacksonFactory jacksonFactory = new JacksonFactory();
            try {
                byte[] postFeedBytes = jacksonFactory.toByteArray(postFeed);

                File outPutFile = Utils.getOutputJsonFile(this, "public_feed");

                if (outPutFile != null) {
                    FileOutputStream fos = new FileOutputStream(outPutFile);
                    fos.write(postFeedBytes);
                    fos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newPostReceiver);
    }


    private void getOfflineFeed(boolean isNetworkAvailable){

        File offlinePrivateFeed = Utils.getOutputJsonFile(this, "public_feed");

        if ((offlinePrivateFeed != null ? offlinePrivateFeed.length() : 0) > 0){ // Here is some offline feeds

            JacksonFactory jacksonFactory = new JacksonFactory();

            try {

                InputStream inputStream = new FileInputStream(offlinePrivateFeed);

                PostFeed postFeed = jacksonFactory.fromInputStream(inputStream, PostFeed.class);

                inputStream.close();

                final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PublicActivity.this) {
                    @Override
                    protected int getExtraLayoutSpace(RecyclerView.State state) {
                        return 300;
                    }
                };
                rvFeed.setLayoutManager(linearLayoutManager);

                // Set the feed cursor
                feedCursor = postFeed.getCursor();

                feedItems = postFeed.getFeedList();

                if (feedItems.size() > 0) { // There are some feeds available
                    feedAdapter = new FeedAdapter(PublicActivity.this, feedItems, linearLayoutManager, PublicActivity.this);
                    feedAdapter.setOnFeedItemClickListener(PublicActivity.this);
                    rvFeed.setAdapter(feedAdapter);
                    rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
                        }
                    });
                    rvFeed.setItemAnimator(new FeedItemAnimator());
                }else{ // No offline feed found
                    showLoading();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{ // There is no offline feed
            if (isNetworkAvailable) {
                showLoading();
            }else {
                showNoInternet();
            }
        }
    }

    private void setupFeed() {
        if (Utils.isNetworkAvailable(this)) {

            getOfflineFeed(true);


            if (!IS_APPLICATION_ACTIVE){ // Get Feed from online source only first time run this when application start

                UserTask<PostFeed> feedTask = new UserTask<>(APP_USER_ID, mode);
                feedTask.execute("getFeed");
                feedTask.setListener(new UserTask.Response<PostFeed>() {
                    @Override
                    public void response(PostFeed response) {
                        hideLoading();

                        if (response == null){
                            showNoInternet();
                            return;
                        }

                        if (response.getFeedList() == null) {
                            // If feed is not available show user to message and hide loading
                            showNothingFound();
                        } else {
                            hideNothingFound();

                            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PublicActivity.this) {
                                @Override
                                protected int getExtraLayoutSpace(RecyclerView.State state) {
                                    return 300;
                                }
                            };
                            rvFeed.setLayoutManager(linearLayoutManager);

                            // Set the feed cursor
                            feedCursor = response.getCursor();

                            feedItems = response.getFeedList();

                            if (feedItems.size() > 0) { // There are some feeds available
                                feedAdapter = new FeedAdapter(PublicActivity.this, feedItems, linearLayoutManager, PublicActivity.this);
                                feedAdapter.setOnFeedItemClickListener(PublicActivity.this);
                                rvFeed.setAdapter(feedAdapter);
                                rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                        FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
                                    }
                                });
                                rvFeed.setItemAnimator(new FeedItemAnimator());
                            } else { // There is no feed available for this user at this time
                                hideLoading();
                                showNothingFound();
                            }
                        }
                    }
                });

            }

            setupStories();

        } else {
            getOfflineFeed(false);
        }

    }

    private void setupStories() {

        UserTask<List<StoryFeed>> storyFeed = new UserTask<>(APP_USER_ID, mode);
        storyFeed.execute("getStoriesFeed");
        storyFeed.setListener(new UserTask.Response<List<StoryFeed>>() {
            @Override
            public void response(List<StoryFeed> response) {
                if (response != null) {
                    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PublicActivity.this, LinearLayoutManager.HORIZONTAL, false) {
                        @Override
                        protected int getExtraLayoutSpace(RecyclerView.State state) {
                            return 300;
                        }
                    };
                    rvStoris.setLayoutManager(linearLayoutManager);

                    // Bring app user story to first position
                    for(StoryFeed sf : response){
                        if (sf.getUser().getUserId().equals(APP_USER_ID)){
                            response.remove(sf);
                            response.add(0, sf);
                            break;
                        }
                    }

                    storyItems = response;
                    storyAdapter = new StoryAdapter(PublicActivity.this, storyItems);
                    storyAdapter.setOnStoryItemClickListener(PublicActivity.this);
                    rvStoris.setAdapter(storyAdapter);
                }else {
                    Log.w("APIDebugging", "null response in PublicActivity -> setupStories");
                }
            }
        });

    }

    private void setupTimerForUpdation(){
        final int oneMint = 1000 * 60;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateApp();
            }
        }, oneMint, oneMint);
    }

    // Check something is new coming or not
    private void updateApp(){

        UserTask<UserAlert> userAlertTask = new UserTask<>(APP_USER_ID);
        userAlertTask.execute("getUpdationAlert");
        userAlertTask.setListener(new UserTask.Response<UserAlert>() {
            @Override
            public void response(UserAlert response) {
                if (response != null){

                    final Long publicFeedId = sharedPreferences.getLong("publicFeedId", 0L);
                    final Long publicNotificationId = sharedPreferences.getLong("publicNotificationId", 0L);

                    if (!publicFeedId.equals(response.getPublicFeedId()) && response.getPublicFeedId() != 0L){
                        // Update shred Pref.
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong("publicFeedId", response.getPrivateFeedId());
                        editor.apply();

                        // update feed from top
                        updateFeedOnTop(response.getPublicFeedId());
                    }

                    if (!publicNotificationId.equals(response.getPublicNotificationId()) && response.getPublicNotificationId() != 0L){
                        // Update shred Pref.
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong("publicNotificationId", response.getPrivateNotificationId());
                        editor.apply();

                        // Change notification icon to show user there is something new
                        bottomNavigation.getMenu().findItem(R.id.navNotification).setIcon(R.drawable.ic_bottom_notification_new);
                        NEW_NOTIFICATION = true;
                    }

                }else{
                    Log.w("APIDebugging", "null response in publicActivity -> updateApp");
                }
            }
        });
    }

    private void updateFeedOnTop(Long feedAlertId){
        if (feedAdapter != null){
            UserTask<PostFeed> feedTask = new UserTask<>(APP_USER_ID, mode);
            feedTask.setAlertId(feedAlertId);
            feedTask.execute("updateFeed");
            feedTask.setListener(new UserTask.Response<PostFeed>() {
                @Override
                public void response(PostFeed response) {
                    if (response.getFeedList() != null){

                        List<FeedItem> freshFeedItems = response.getFeedList();

                        // Check newlyPostedPostId is not null then remove this post from updated post
                        if (newlyPublicPostedPostId != null){
                            for (FeedItem feedItem : freshFeedItems){
                                if (feedItem.getPostId().equals(newlyPublicPostedPostId)){
                                    freshFeedItems.remove(feedItem);
                                    break;
                                }
                            }
                        }

                        feedAdapter.updateItemsAtTop(freshFeedItems);

                        // Also show arrow button to reach at top
                        showBtnGoToTop();

                    }
                }
            });
        }
    }

    @Override
    public void onBottomReached(int position) {
        // If cursor is not null then get more feeds
        if (feedCursor != null){

            UserTask<PostFeed> feedTask = new UserTask<>(APP_USER_ID, mode);
            feedTask.setCursor(feedCursor);
            feedTask.execute("getFeed");
            feedTask.setListener(new UserTask.Response<PostFeed>() {
                @Override
                public void response(PostFeed response) {
                    if (response.getFeedList() != null){

                        feedCursor = response.getCursor();
                        feedAdapter.updateItemsAtEnd(response.getFeedList());

                    }else {
                        feedCursor = null;
                    }
                }
            });

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_SHOW_LOADING_ITEM.equals(intent.getAction())) {
            showFeedLoadingItemDelayed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 777){
            if (resultCode == RESULT_OK){

                if (!Utils.isNetworkAvailable(this)){
                    Toast.makeText(this, "Error in Network connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                String storyType = data.getStringExtra("ARG_STORY_TYPE");
                String storyMediaPath = data.getStringExtra("ARG_STORY_MEDIA_PATH");
                createNewStory(storyType, storyMediaPath);
            }
        }
    }

    private BroadcastReceiver newPostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean operationSuccess = intent.getBooleanExtra("operationSuccess", false);

            // Check the operation is success or not
            if (operationSuccess && feedAdapter != null && newFeedItem != null){ // Post is Successfully created now update the feedItem postId and postSafeKey

                Long postId = intent.getLongExtra("postId", 1111111L);
                String postSafeKey = intent.getStringExtra("postSafeKey");

                newlyPublicPostedPostId = postId;

                // Find the index
                int index = feedAdapter.feedItems.indexOf(newFeedItem);

                // update the FeedItem
                newFeedItem.setPostId(postId);
                newFeedItem.setPostSafeKey(postSafeKey);

                // Check if array index is not -1 means feed item not found
                if (index != -1)
                    feedAdapter.feedItems.set(index, newFeedItem);

            }else if (feedAdapter != null){ // There is some problem, remove post from
                feedAdapter.feedItems.remove(newFeedItem);
                feedAdapter.notifyDataSetChanged();
                Toast.makeText(PublicActivity.this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
            }

            // Release variable
            newFeedItem = null;
        }
    };

    private void showFeedLoadingItemDelayed() {
        // Just check it out it's not null
        if (newFeedItem != null) { // It's a new post
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // Check if feed adapter is not null
                    if (feedAdapter != null) {

                        rvFeed.smoothScrollToPosition(0);
                        feedAdapter.showLoadingView(newFeedItem);

                    }else{

                        // Hide No Feed message
                        hideNothingFound();

                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PublicActivity.this) {
                            @Override
                            protected int getExtraLayoutSpace(RecyclerView.State state) {
                                return 300;
                            }
                        };
                        rvFeed.setLayoutManager(linearLayoutManager);

                        feedItems = new ArrayList<>();
                        feedItems.add(newFeedItem);

                        feedAdapter = new FeedAdapter(PublicActivity.this, feedItems, linearLayoutManager, PublicActivity.this);
                        feedAdapter.showLoadingView = true;
                        feedAdapter.setOnFeedItemClickListener(PublicActivity.this);
                        rvFeed.setAdapter(feedAdapter);
                        rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
                            }
                        });
                    }

                }
            }, 500);
        }
    }


    private void createNewStory(String storyType, String storyMediaPath) {
        // Create a story feed item, first create a user object
        User user = new User();
        user.setUserId(APP_USER_ID);
        user.setUsername(sharedPreferences.getString("username", null));
        user.setFullName(sharedPreferences.getString("fullName", null));
        user.setProfilePic(sharedPreferences.getString("profilePic", null));
        user.setBadge(sharedPreferences.getInt("badge", 0));



        Stories newStory = new Stories();
        newStory.setType(storyType);
        newStory.setSource(storyMediaPath);


        // check user has already story or not
        if (storyItems.size() > 0 && storyItems.get(0).getUser().getUserId().equals(APP_USER_ID)){ // User already started the story

            int storyNum = storyItems.get(0).getStory().getStoriesCount();

            newStory.setStoryNum(storyNum);
            storyItems.get(0).getStory().getStories().add(newStory);

            storyItems.get(0).getStory().setStoriesCount(storyNum+1);
            storyAdapter.notifyDataSetChanged();

        }else{
            Story story = new Story();
            story.setMode(mode);
            story.setStoriesCount(1);

            newStory.setStoryNum(0);

            List<Stories> stories = new ArrayList<>();
            stories.add(newStory);

            story.setStories(stories);

            // Create a story feed object
            StoryFeed storyFeed = new StoryFeed();
            storyFeed.setUser(user);
            storyFeed.setStory(story);
            storyFeed.setUpdatedOnHumanReadable("Moment Ago");

            // Update the adapter
            storyItems.add(0, storyFeed);

            if(storyAdapter != null) {
                storyAdapter.notifyDataSetChanged();
            }else{
                storyAdapter = new StoryAdapter(PublicActivity.this, storyItems);
                storyAdapter.setOnStoryItemClickListener(PublicActivity.this);
                rvStoris.setAdapter(storyAdapter);
            }

        }

        String globalSource = fileNameForCloudStorage(storyType);

        Stories stories = new Stories();
        stories.setType(storyType);
        stories.setSource(Utils.bucketURL + globalSource);

        StoryTask<Story> storyTask = new StoryTask<>();
        storyTask.setUserId(APP_USER_ID);
        storyTask.setMode(mode);
        storyTask.setStory(stories);
        storyTask.execute("createStory");

        // upload file to cloud storage
        uploadFileToCloudStorage(storyMediaPath, globalSource);

    }
    private String fileNameForCloudStorage(String folder){
        String globalSource = null;

        switch (folder) {
            case "GIF":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.STORY_GIF);
                break;
            case "VIDEO":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.STORY_VID);
                break;
            case "IMAGE":
                globalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.STORY_IMG);
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

    private void hideBtnGoToTop(){
        btnGoToTop.setTranslationY(Utils.dpToPx(90));
    }

    private void showBtnGoToTop(){
        btnGoToTop.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(300)
                .setDuration(ANIM_DURATION_FAB)
                .start();
    }

    @Override
    public void onStoryClick(View v, StoryFeed storyItem) {
        STORY_SLIDES = storyItem;
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2; //2
        StoryActivity.startStoryFromLocation(startingLocation, this, storyItem.getStory().getStories().size());
        overridePendingTransition(0, 0);
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
    public void onLikesClick(View v, String postSafeKey) {
        final Intent intent = new Intent(this, LikeActivity.class);
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        intent.putExtra(LikeActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        intent.putExtra(LikeActivity.POST_ID, postSafeKey);
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

        // if list is true it mean it's a list if not it is a tag
        // send extra with intent to open tag or list in new activity
        // show activity handle both of these no new activity for tags
        intent.putExtra("ACTIVITY_IS_LIST", list);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onProfileClick(View v, Long profileId) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2; //2
        // convert profileId long into string because sometimes we need to pass username inside of profile id
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
                Toast.makeText(PublicActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(APP_USER_ID, postSafeKey, "SPAM");
                postReport.execute("report");
            }
        });
        TextView reportAntiReligion = dialog.findViewById(R.id.reportAntiReligion);
        reportAntiReligion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(PublicActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(APP_USER_ID, postSafeKey, "ANTI_RELIGION");
                postReport.execute("report");
            }
        });
        TextView reportSexualContent = dialog.findViewById(R.id.reportSexualContent);
        reportSexualContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(PublicActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(APP_USER_ID, postSafeKey, "SEXUAL_CONTENT");
                postReport.execute("report");
            }
        });
        TextView reportOther = dialog.findViewById(R.id.reportOther);
        reportOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(PublicActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(APP_USER_ID, postSafeKey, "OTHER");
                postReport.execute("report");
            }
        });

    }


    @Override
    public void onApproveTagClick(final Long postId, final Boolean isPublic) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        feedAdapter.feedItems.get(position).setTaggedApproved(true);

        PostTask<Void> approveTag = new PostTask<>(APP_USER_ID, postId, isPublic);
        approveTag.execute("tagApproved");

    }

    @Override
    public void onRemoveTagClick(String postId) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        feedAdapter.feedItems.get(position).setTaggedApproved(false);
    }

    @Override
    public void onHidePostClick(String feedSafeKey) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        if (position != -1) {
            feedAdapter.feedItems.remove(position);
            feedAdapter.notifyItemRemoved(position);
        }
        UserTask<Void> userTask = new UserTask<>();
        userTask.setFeedSafeKey(feedSafeKey);
        userTask.execute("deleteFeed");

    }

    @Override
    public void onDeletePostClick(String postSafeKey) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        if (position != -1) {
            feedAdapter.feedItems.remove(position);
            feedAdapter.notifyItemRemoved(position);
        }

        PostTask<Void> postTask = new PostTask<>(postSafeKey);
        postTask.execute("deletePost");
    }

    @Override
    public void onSavePostClick(String postSafeKey) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        PostTask<TaskComplete> savePost = new PostTask<>(APP_USER_ID);
        savePost.setPostSafeKey(postSafeKey);
        savePost.execute("savePost");
        Toast.makeText(this, "Post Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelClick(String postId) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    private void moveUserToTop(){
        if (rvFeed != null){
            rvFeed.smoothScrollToPosition(0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideBtnGoToTop();
                }
            }, 500);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnInternetRetry:
                hideNoInternet();
                setupFeed();
                break;
            case R.id.btnGoToTop:
                moveUserToTop();
                break;
            case R.id.ivAppbarPrivate:
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.ivAppbarExplorer:
                final Intent intent = new Intent(this, DiscoverActivity.class);
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                intent.putExtra(DiscoverActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
                intent.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            case R.id.ivAppbarUser:
                int[] startingLocation2 = new int[2];
                v.getLocationOnScreen(startingLocation2);
                startingLocation2[0] += v.getWidth() / 2;
                ProfileActivity.startUserProfileFromLocation(startingLocation2, PublicActivity.this, String.valueOf(APP_USER_ID));
                overridePendingTransition(0, 0);
                break;
        }
    }

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsHomeLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsHomeNoInternet)).inflate();
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
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsHomeNothingFound)).inflate();
            Button btnInviteFriends = findViewById(R.id.btnInviteFriends);
            btnInviteFriends.setVisibility(View.VISIBLE);
            btnInviteFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_SUBJECT, "ZEEM");
                        String strShareMessage = "\nHi, \nI'm using ZEEM, A new age of social media. Install this app and have fun!\n\n";
                        strShareMessage = strShareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName();
                        i.putExtra(Intent.EXTRA_TEXT, strShareMessage);
                        i.setPackage("com.whatsapp");
                        startActivity(Intent.createChooser(i, "Share via"));
                    } catch(Exception e) {
                        Toast.makeText(PublicActivity.this, "Unable to open whatsapp", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }

}