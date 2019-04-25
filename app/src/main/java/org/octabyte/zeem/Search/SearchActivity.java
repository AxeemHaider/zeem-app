package org.octabyte.zeem.Search;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.appspot.octabyte_zeem.zeem.model.PostFeed;
import com.appspot.octabyte_zeem.zeem.model.StoryFeed;
import com.appspot.octabyte_zeem.zeem.model.User;
import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.API.SearchTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Comment.CommentActivity;
import org.octabyte.zeem.Home.FeedAdapter;
import org.octabyte.zeem.Home.FeedContextMenu;
import org.octabyte.zeem.Home.FeedContextMenuManager;
import org.octabyte.zeem.Home.FeedItemAnimator;
import org.octabyte.zeem.Home.StoryActivity;
import org.octabyte.zeem.Home.StoryAdapter;
import org.octabyte.zeem.Like.LikeActivity;
import org.octabyte.zeem.Like.LikeAdapter;
import org.octabyte.zeem.List.ShowListActivity;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.GPSTracker;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;
import static org.octabyte.zeem.Home.StorySlide.STORY_SLIDES;


/**
 * Created by Azeem on 9/8/2017.
 */

public class SearchActivity extends AppCompatActivity implements FeedAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener, LikeAdapter.OnLikeItemClickListener, StoryAdapter.OnStoryItemClickListener {

    private String TAG = "DPostsActivity";

    private RecyclerView rvFeed;

    private Toolbar toolbar;

    private FeedAdapter feedAdapter;
    private List<FeedItem> feedItems;
    private int position = -1;
    private String searchQuery;

    private Long mUserId;
    private int filterBadge;
    private String mode;
    private View vLoadingView, vNoInternetView, vNothingFoundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initVariable();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        mUserId = sharedPreferences.getLong("userId", 123L);

        mode = getIntent().getStringExtra("ARG_NAV_SENDER_MODE_TYPE");
        searchQuery = getIntent().getStringExtra("ARG_SEARCH_QUERY");

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);

        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (searchQuery.equals("topPost") || searchQuery.equals("filterBadgePost")){
            setupFeed();
            if (searchQuery.equals("filterBadgePost")){
                toolbarTitle.setText("Badge Post");
            }else{
                toolbarTitle.setText("Top Post");
            }
        }else if (searchQuery.equals("filterBadgeUser") || searchQuery.equals("searchUser")){
            setupUserAdapter();
            if (searchQuery.equals("searchUser")){
                toolbarTitle.setText("Results");
            }else{
                toolbarTitle.setText("Badge User");
            }
        }

    }
    private void initVariable(){
        rvFeed = findViewById(R.id.rvFeed);
    }

    private void setupFeed() {
        Log.d(TAG, "setup Feed");
        if (Utils.isNetworkAvailable(this)) {
            showLoading();

            SearchTask<PostFeed> searchPosts = new SearchTask<>(mUserId);
            searchPosts.setMode(mode);

            if (searchQuery.equals("filterBadgePost"))
                searchPosts.setBadge(getIntent().getIntExtra("FILTER_BADGE",0));

            searchPosts.execute(searchQuery);
            searchPosts.setListener(new SearchTask.Response<PostFeed>() {
                @Override
                public void response(PostFeed response) {
                    hideLoading();

                    if (response.getFeedList() != null){
                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this) {
                            @Override
                            protected int getExtraLayoutSpace(RecyclerView.State state) {
                                return 300;
                            }
                        };
                        rvFeed.setLayoutManager(linearLayoutManager);

                        feedItems = response.getFeedList();
                        feedAdapter = new FeedAdapter(SearchActivity.this, feedItems, linearLayoutManager,SearchActivity.this);
                        Log.d(TAG, "Number of Feeds received: " + feedItems.size());
                        feedAdapter.setOnFeedItemClickListener(SearchActivity.this);
                        rvFeed.setAdapter(feedAdapter);
                        rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
                            }
                        });
                        rvFeed.setItemAnimator(new FeedItemAnimator());
                    }else {
                        Log.w("APIDebugging", "null response in SearchActivity->setupFeed");
                        showNothingFound();
                    }
                }
            });

        }else{
            showNoInternet();
        }

    }

    private void setupUserAdapter(){
        if (Utils.isNetworkAvailable(this)) {
            showLoading();

            SearchTask<List<User>> filterUser = new SearchTask<>(mUserId);
            filterUser.setRelation("PUBLIC");

            if(searchQuery.equals("filterBadgeUser")){
                filterUser.setBadge(getIntent().getIntExtra("FILTER_BADGE",0));
            }

            filterUser.execute(searchQuery);
            filterUser.setListener(new SearchTask.Response<List<User>>() {
                @Override
                public void response(List<User> response) {
                    hideLoading();

                    if (response != null) {
                        if (response.size() > 0) {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
                            rvFeed.setLayoutManager(linearLayoutManager);
                            rvFeed.setHasFixedSize(true);

                            final LikeAdapter userAdapter = new LikeAdapter(SearchActivity.this, response);
                            userAdapter.setOnLikeItemClickListener(SearchActivity.this);
                            rvFeed.setAdapter(userAdapter);
                            rvFeed.setOverScrollMode(View.OVER_SCROLL_NEVER);
                            rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                                        userAdapter.setAnimationsLocked(true);
                                    }
                                }
                            });
                        } else {
                            Log.w("APIDebugging", "null response in SearchActivity->setupUserAdapter");
                            showNothingFound();
                        }
                    } else {
                        Log.w("APIDebugging", "null response in SearchActivity->setupUserAdapter");
                        showNothingFound();
                    }
                }
            });
        } else {
            showNoInternet();
        }
    }

    @Override
    public void onBottomReached(int position) {

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
        dialog.setContentView(R.layout.dialog_post_report);

        dialog.show();

        Button reportSpam = (Button) dialog.findViewById(R.id.reportSpam);
        reportSpam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(SearchActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "SPAM");
                postReport.execute("report");
            }
        });
        Button reportAntiReligion = (Button) dialog.findViewById(R.id.reportAntiReligion);
        reportAntiReligion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(SearchActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "ANTI_RELIGION");
                postReport.execute("report");
            }
        });
        Button reportSexualContent = (Button) dialog.findViewById(R.id.reportSexualContent);
        reportSexualContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(SearchActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "SEXUAL_CONTENT");
                postReport.execute("report");
            }
        });
        Button reportOther = (Button) dialog.findViewById(R.id.reportOther);
        reportOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(SearchActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "OTHER");
                postReport.execute("report");
            }
        });

    }


    @Override
    public void onApproveTagClick(final Long postId, final Boolean isPublic) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        feedAdapter.feedItems.get(position).setTaggedApproved(true);

        PostTask<Void> approveTag = new PostTask<>(mUserId, postId, isPublic);
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
            feedAdapter.feedItems.remove(position);
            feedAdapter.notifyItemRemoved(position);
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
        if (Utils.isNetworkAvailable(this)) {
            FeedContextMenuManager.getInstance().hideContextMenu();
            PostTask<Void> savePost = new PostTask<>(mUserId);
            savePost.setPostSafeKey(postSafeKey);
            savePost.execute("savePost");
            Toast.makeText(this, "Post Saved", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelClick(String postId) {
        FeedContextMenuManager.getInstance().hideContextMenu();
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

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsSearchLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsSearchNoInternet)).inflate();
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
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsSearchNothingFound)).inflate();
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }

}