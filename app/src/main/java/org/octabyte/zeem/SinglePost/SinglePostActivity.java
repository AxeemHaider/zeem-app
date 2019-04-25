package org.octabyte.zeem.SinglePost;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.appspot.octabyte_zeem.zeem.model.PostFeed;
import com.appspot.octabyte_zeem.zeem.model.TaskComplete;

import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Comment.CommentActivity;
import org.octabyte.zeem.Home.FeedAdapter;
import org.octabyte.zeem.Home.FeedContextMenu;
import org.octabyte.zeem.Home.FeedContextMenuManager;
import org.octabyte.zeem.Home.FeedItemAnimator;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.Like.LikeActivity;
import org.octabyte.zeem.List.ShowListActivity;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.BottomNavigationSetup;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

public class SinglePostActivity extends AppCompatActivity implements FeedAdapter.OnFeedItemClickListener, FeedContextMenu.OnFeedContextMenuItemClickListener {

    private RecyclerView rvFeed;
    private BottomNavigationView bottomNavigation;
    private long appUserId;
    private String postSafeKey;
    private View vLoadingView, vNoInternetView, vNothingFoundView;
    private List<FeedItem> feedItems;
    private FeedAdapter feedAdapter;
    private int position = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        initVariable();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        appUserId = sharedPreferences.getLong("userId", 123L);

        postSafeKey = getIntent().getStringExtra("ARG_SINGLE_POST_SAFE_KEY");
        String postMode = getIntent().getStringExtra("ARG_SINGLE_POST_POST_MODE");

        BottomNavigationSetup.init(bottomNavigation, this, -1, postMode);

        setupPost();
    }

    private void initVariable(){
        rvFeed = findViewById(R.id.rvFeed);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        toolbarTitle.setText("Post");

        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SinglePostActivity.this, HomeActivity.class));
            }
        });
    }

    private void setupPost() {
        hideNoInternet();

        if (Utils.isNetworkAvailable(this)) {

            showLoading();

            // Get single post
            PostTask<PostFeed> postTask = new PostTask<>(appUserId);
            postTask.setPostSafeKey(postSafeKey);
            postTask.execute("getSinglePost");
            postTask.setListener(new PostTask.Response<PostFeed>() {
                @Override
                public void response(PostFeed response) {

                    hideLoading();

                    if (response != null) {
                        if (response.getFeedList() != null) {

                            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SinglePostActivity.this) {
                                @Override
                                protected int getExtraLayoutSpace(RecyclerView.State state) {
                                    return 300;
                                }
                            };
                            rvFeed.setLayoutManager(linearLayoutManager);

                            feedItems = response.getFeedList();

                            if (feedItems.size() > 0) { // There are some feeds available
                                feedAdapter = new FeedAdapter(SinglePostActivity.this, feedItems, linearLayoutManager, SinglePostActivity.this);
                                feedAdapter.setOnFeedItemClickListener(SinglePostActivity.this);
                                rvFeed.setAdapter(feedAdapter);
                                rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                        FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
                                    }
                                });
                                rvFeed.setItemAnimator(new FeedItemAnimator());
                            } else { // There is no feed available for this user at this time
                                showNothingFound();
                            }

                        } else {
                            Log.w("APIDebugging", "null response in SinglePost -> getSinglePost");
                            showNothingFound();
                        }

                    } else {
                        Log.w("APIDebugging", "null response in SinglePost -> getSinglePost");
                        showNothingFound();
                    }

                }
            });

        }else{
            showNoInternet();
        }

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
    public void onBottomReached(int position) {

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
                Toast.makeText(SinglePostActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(appUserId, postSafeKey, "SPAM");
                postReport.execute("report");
            }
        });
        TextView reportAntiReligion = dialog.findViewById(R.id.reportAntiReligion);
        reportAntiReligion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(SinglePostActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(appUserId, postSafeKey, "ANTI_RELIGION");
                postReport.execute("report");
            }
        });
        TextView reportSexualContent = dialog.findViewById(R.id.reportSexualContent);
        reportSexualContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(SinglePostActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(appUserId, postSafeKey, "SEXUAL_CONTENT");
                postReport.execute("report");
            }
        });
        TextView reportOther = dialog.findViewById(R.id.reportOther);
        reportOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(SinglePostActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(appUserId, postSafeKey, "OTHER");
                postReport.execute("report");
            }
        });

    }


    @Override
    public void onApproveTagClick(final Long postId, final Boolean isPublic) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        feedAdapter.feedItems.get(position).setTaggedApproved(true);

        PostTask<Void> approveTag = new PostTask<>(appUserId, postId, isPublic);
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
        PostTask<TaskComplete> savePost = new PostTask<>(appUserId);
        savePost.setPostSafeKey(postSafeKey);
        savePost.execute("savePost");
        Toast.makeText(this, "Post Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelClick(String postId) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.getMenu().getItem(0).setChecked(false);
        bottomNavigation.getMenu().getItem(0).setCheckable(false);
    }

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsSinglePostLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsSinglePostNoInternet)).inflate();
            Button btnInternetRetry = findViewById(R.id.btnInternetRetry);
            btnInternetRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupPost();
                }
            });
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
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsSinglePostNothingFound)).inflate();
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
}
