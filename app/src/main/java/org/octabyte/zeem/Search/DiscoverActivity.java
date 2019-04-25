package org.octabyte.zeem.Search;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.appspot.octabyte_zeem.zeem.model.PostFeed;
import com.appspot.octabyte_zeem.zeem.model.User;
import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.API.SearchTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Comment.CommentActivity;
import org.octabyte.zeem.Home.FeedAdapter;
import org.octabyte.zeem.Home.FeedContextMenu;
import org.octabyte.zeem.Home.FeedContextMenuManager;
import org.octabyte.zeem.Home.FeedItemAnimator;
import org.octabyte.zeem.Like.LikeActivity;
import org.octabyte.zeem.List.ShowListActivity;
import org.octabyte.zeem.Profile.PostGridAdapter;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.BottomNavigationSetup;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/29/2017.
 */

public class DiscoverActivity extends AppCompatActivity implements
        SearchAdapter.OnSearchListClickListener,
        PostGridAdapter.OnGridImageClickListener,
        FeedAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener,
        View.OnClickListener {

    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    private int drawingStartLocation;

    private RelativeLayout lyRoot;
    private LinearLayout lySuggestion;
    private AppBarLayout appBarLayout;
    private BottomNavigationView bottomNavigation;
    private EditText etSearch;
    private RecyclerView rvSearchResult;
    private List<User> searchList = null;
    private SearchAdapter searchAdapter;
    private Boolean suggestionMode = false;

    // ####### List and Grid view variable
    private CoordinatorLayout clContent;
    private RecyclerView rvFeedListView, rvFeedGridView;
    private PostGridAdapter postGridAdapter;
    private FeedAdapter postListAdapter;
    private List<FeedItem> postItems;
    private int position = -1;
    private int scrollPosition;
    private Boolean isGridView = true;

    private Long mUserId;
    private Boolean isSearchAdapterAdapterSet = false;
    private String mode;
    private View vLoadingView, vNoInternetView;
    private View vNothingFoundView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);

        initVariable();
        getResponse();

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

        mode = getIntent().getStringExtra("ARG_NAV_SENDER_MODE_TYPE");

        BottomNavigationSetup.init(bottomNavigation, DiscoverActivity.this, -1, mode);
        setupSearch();
    }

    private void initVariable() {

        TextView tvTopPost = findViewById(R.id.tvTopPost);
        tvTopPost.setOnClickListener(this);

        TextView tvBrownPost = findViewById(R.id.tvBrownPost);
        tvBrownPost.setOnClickListener(this);

        TextView tvSilverPost = findViewById(R.id.tvSilverPost);
        tvSilverPost.setOnClickListener(this);

        TextView tvBrownUser = findViewById(R.id.tvBrownUser);
        tvBrownUser.setOnClickListener(this);

        TextView tvSilverUser = findViewById(R.id.tvSilverUser);
        tvSilverUser.setOnClickListener(this);

        TextView tvGoldUser = findViewById(R.id.tvGoldUser);
        tvGoldUser.setOnClickListener(this);

        lyRoot = findViewById(R.id.root);
        appBarLayout = findViewById(R.id.appBarLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        etSearch = findViewById(R.id.etSearch);
        etSearch.setOnClickListener(this);
        etSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    suggestionMode = true;
                    clContent.setVisibility(View.GONE);
                    lySuggestion.setVisibility(View.VISIBLE);
                }
            }
        });
        rvSearchResult = findViewById(R.id.rvSearchResult);
        lySuggestion = findViewById(R.id.lySuggestion);

        // Grid and list view variable
        clContent = findViewById(R.id.content);
        rvFeedGridView = findViewById(R.id.rvFeedGridView);
        rvFeedListView = findViewById(R.id.rvFeedListView);
    }

    private void getResponse() {
        if (Utils.isNetworkAvailable(this)) {

            hideNoInternet();
            showLoading();

            UserTask<PostFeed> discoverPosts = new UserTask<>(mUserId);
            discoverPosts.setOffset(0); // start from begning when you reach at end increase this offset
            discoverPosts.execute("discover");
            discoverPosts.setListener(new UserTask.Response<PostFeed>() {
                @Override
                public void response(PostFeed response) {
                    hideLoading();
                    hideNothingFound();

                    if (response != null) {
                        if (response.getFeedList() != null) {
                            if(response.getFeedList().size() > 0) {
                                postItems = response.getFeedList();
                                setupGridView();
                                setupLinearAdapter();
                            }else {
                                Log.w("APIDebugging", "null response in DiscoverActivity->getResponse");
                                showNothingFound();
                            }
                        }else {
                            Log.w("APIDebugging", "null response in DiscoverActivity->getResponse");
                            showNothingFound();
                        }
                    } else {
                        Log.w("APIDebugging", "null response in DiscoverActivity->getResponse");
                        showNothingFound();
                    }
                }
            });

        } else {
            // If network is not available check offline feeds
            // If offline feeds are also not available then show
            // No Internet Connection Message
            rvFeedGridView.setVisibility(View.GONE);
            showNoInternet();
            Button btnInternetRetry = findViewById(R.id.btnInternetRetry);
        }
    }

    private void setupGridView() {
        final StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvFeedGridView.setLayoutManager(gridLayoutManager);
        rvFeedGridView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                postGridAdapter.setLockedAnimations(true);
            }
        });
        setupGridAdapter();
    }

    private void setupGridAdapter() {
        postGridAdapter = new PostGridAdapter(this, postItems);
        postGridAdapter.setOnGridImageClickListener(DiscoverActivity.this);
        rvFeedGridView.setAdapter(postGridAdapter);
    }

    private void setupLinearAdapter() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        rvFeedListView.setLayoutManager(linearLayoutManager);

        postListAdapter = new FeedAdapter(this, postItems, linearLayoutManager, this);
        postListAdapter.setOnFeedItemClickListener(DiscoverActivity.this);
        rvFeedListView.setAdapter(postListAdapter);
        rvFeedListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
            }
        });
        rvFeedListView.setItemAnimator(new FeedItemAnimator());
    }

    private void showGridView() {
        isGridView = true;
        rvFeedGridView.setVisibility(View.VISIBLE);
        rvFeedListView.setVisibility(View.GONE);
        scrollPosition = ((LinearLayoutManager) rvFeedListView.getLayoutManager()).findFirstVisibleItemPosition();
        Log.i("SearchActivityDebug", String.valueOf(scrollPosition));
        rvFeedGridView.scrollToPosition(scrollPosition);
    }

    private void showLinearView(int position) {
        isGridView = false;
        rvFeedGridView.setVisibility(View.GONE);
        rvFeedListView.setVisibility(View.VISIBLE);
        scrollPosition = position;
        Log.i("SearchActivityDebug", String.valueOf(scrollPosition));
        rvFeedListView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onBottomReached(int position) {

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
        dialog.setContentView(R.layout.dialog_post_report);

        dialog.show();

        Button reportSpam = dialog.findViewById(R.id.reportSpam);
        reportSpam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(DiscoverActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "SPAM");
                postReport.execute("report");
            }
        });
        Button reportAntiReligion = dialog.findViewById(R.id.reportAntiReligion);
        reportAntiReligion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(DiscoverActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "ANTI_RELIGION");
                postReport.execute("report");
            }
        });
        Button reportSexualContent = dialog.findViewById(R.id.reportSexualContent);
        reportSexualContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(DiscoverActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "SEXUAL_CONTENT");
                postReport.execute("report");
            }
        });
        Button reportOther = dialog.findViewById(R.id.reportOther);
        reportOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(DiscoverActivity.this, R.string.report_send, Toast.LENGTH_SHORT).show();
                PostTask<Void> postReport = new PostTask<>(mUserId, postSafeKey, "OTHER");
                postReport.execute("report");
            }
        });

    }


    @Override
    public void onApproveTagClick(final Long postId, final Boolean isPublic) {
        FeedContextMenuManager.getInstance().hideContextMenu();
        postListAdapter.feedItems.get(position).setTaggedApproved(true);

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
        if (position != -1) {
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
        PostTask<Void> savePost = new PostTask<>(mUserId);
        savePost.setPostSafeKey(postSafeKey);
        savePost.execute("savePost");
        Toast.makeText(this, "Post Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelClick(String postId) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etSearch:
                suggestionMode = true;
                hideLoading();
                hideNoInternet();
                hideNothingFound();
                clContent.setVisibility(View.GONE);
                lySuggestion.setVisibility(View.VISIBLE);
                break;
            case R.id.tvTopPost:
                startSearchActivity("topPost");
                break;
            case R.id.tvBrownPost:
                startSearchActivity("filterBadgePost", 1);
                break;
            case R.id.tvSilverPost:
                startSearchActivity("filterBadgePost", 2);
                break;
            case R.id.tvBrownUser:
                startSearchActivity("filterBadgeUser", 1);
                break;
            case R.id.tvSilverUser:
                startSearchActivity("filterBadgeUser", 2);
                break;
            case R.id.tvGoldUser:
                startSearchActivity("filterBadgeUser", 3);
                break;

        }
    }

    private void startSearchActivity(String searchQuery, int badge){
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
        intent.putExtra("ARG_SEARCH_QUERY", searchQuery);
        intent.putExtra("FILTER_BADGE", badge);
        startActivity(intent);
    }

    private void startSearchActivity(String searchQuery){
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
        intent.putExtra("ARG_SEARCH_QUERY", searchQuery);
        startActivity(intent);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            private Timer timer = new Timer();
            private final long DELAY = 1000; // milliseconds

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    lySuggestion.setVisibility(View.GONE);
                    rvSearchResult.setVisibility(View.VISIBLE);

                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    DiscoverActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showLoading();
                                        }
                                    });
                                    SearchTask<List<User>> searchUser = new SearchTask<>();
                                    searchUser.setFullName(String.valueOf(s));
                                    searchUser.execute("searchUser");
                                    searchUser.setListener(new SearchTask.Response<List<User>>() {
                                        @Override
                                        public void response(List<User> response) {
                                            hideLoading();
                                            hideNothingFound();
                                            if (response != null) {
                                                setupSearchAdapter(response);
                                            } else {
                                                Log.w("APIDebugging", "null response in DiscoverActivity->setupSearch");
                                               DiscoverActivity.this.runOnUiThread(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       showNothingFound();
                                                   }
                                               });
                                            }
                                        }
                                    });
                                }
                            },
                            DELAY
                    );

                } else {
                    lySuggestion.setVisibility(View.VISIBLE);
                    rvSearchResult.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupSearchAdapter(List<User> response) {
        if (!isSearchAdapterAdapterSet) {
            isSearchAdapterAdapterSet = true;
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DiscoverActivity.this);
            rvSearchResult.setLayoutManager(linearLayoutManager);

            searchAdapter = new SearchAdapter(DiscoverActivity.this, response);
            searchAdapter.setOnSearchListClickListener(DiscoverActivity.this);
            rvSearchResult.setAdapter(searchAdapter);
            rvSearchResult.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            searchAdapter.updateList(response);
        }
    }

    private void startIntroAnimation() {
        ViewCompat.setElevation(appBarLayout, 0);
        lyRoot.setScaleY(0.1f);
        lyRoot.setPivotY(drawingStartLocation);

        lyRoot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setElevation(appBarLayout, Utils.dpToPx(8));
                    }
                })
                .start();
        appBarLayout.animate()
                .translationY(0)
                .setDuration(300);
    }

    @Override
    public void onBackPressed() {
        if (suggestionMode) {
            suggestionMode = false;
            lySuggestion.setVisibility(View.GONE);
            rvSearchResult.setVisibility(View.GONE);
            clContent.setVisibility(View.VISIBLE);
        } else if (isGridView) {
            //ViewCompat.setElevation(toolbar, 0);
            lyRoot.animate()
                    .translationY(Utils.getScreenHeight(this))
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            DiscoverActivity.super.onBackPressed();
                            overridePendingTransition(0, 0);
                        }
                    })
                    .start();
        } else {
            showGridView();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            bottomNavigation.getMenu().getItem(0).setChecked(false);
            bottomNavigation.getMenu().getItem(0).setCheckable(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreformAction(Boolean follow, Long userId) {
        // Send data to server and preform action if follow is true
        // Follow this person otherwise un Follow this
    }

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsDiscoverLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsDiscoverNoInternet)).inflate();
            Button btnInternetRetry = findViewById(R.id.btnInternetRetry);
            btnInternetRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getResponse();
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
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsDiscoverNothingFound)).inflate();
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
}
