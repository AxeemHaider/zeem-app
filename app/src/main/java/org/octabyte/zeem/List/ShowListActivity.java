package org.octabyte.zeem.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.User;

import org.octabyte.zeem.API.ListTask;
import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

/**
 * Created by Azeem on 8/9/2017.
 */

public class ShowListActivity extends AppCompatActivity implements SingleListAdapter.OnUserClickListener {

    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    public static final String LIST_ID = "list_id";
    private int drawingStartLocation;

    private LinearLayout llShowListLayout;
    private Toolbar toolbar;
    private TextView tvMemberCount;

    private RecyclerView rvListMembers;
    private List<User> memberList;
    private SingleListAdapter singleListAdapter;

    private Long mListOrTagId;
    private TextView toolbarTitle;
    private View vLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initVariable();

        mListOrTagId = getIntent().getLongExtra(LIST_ID, 123L);

        Boolean isListActivity = getIntent().getBooleanExtra("ACTIVITY_IS_LIST", true);

        if(isListActivity) {
            membersAdapter();
        }else{
            toolbarTitle.setText(R.string.toolbar_tag);
            tagAdapter();
        }

        int actionbarSize = Utils.dpToPx(50);
        toolbar.setTranslationY(-actionbarSize);

        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            llShowListLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    llShowListLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });
        }

    }
    private void initVariable(){
        toolbarTitle = findViewById(R.id.toolbarTitle);
        tvMemberCount = findViewById(R.id.tvListAction);
        tvMemberCount.setText(R.string.loading_msg);
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);

        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        toolbar = findViewById(R.id.toolbar);
        rvListMembers = findViewById(R.id.rvLists);
        llShowListLayout = findViewById(R.id.llMainLayout);
    }

    private void membersAdapter(){
        if(Utils.isNetworkAvailable(this)){
            showLoading();

            ListTask<List<User>> listMembers = new ListTask<>();
            listMembers.setListId(mListOrTagId);
            listMembers.execute("getListMembers");
            listMembers.setListener(new ListTask.Response<List<User>>() {
                @Override
                public void response(List<User> userList) {
                    hideLoading();

                    if (userList != null){
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ShowListActivity.this);
                        rvListMembers.setLayoutManager(linearLayoutManager);

                        memberList = userList;
                        singleListAdapter = new SingleListAdapter(ShowListActivity.this, memberList);
                        singleListAdapter.setOnUserClickListener(ShowListActivity.this);
                        rvListMembers.setAdapter(singleListAdapter);
                        rvListMembers.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        rvListMembers.setOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                                    singleListAdapter.setAnimationsLocked(true);
                                }
                            }
                        });
                        // set List Name
                        toolbarTitle.setText(R.string.toolbar_list_members);
                        // set member count
                        tvMemberCount.setText(getResources().getQuantityString(R.plurals.members_count, userList.size(), userList.size()));
                    }else {
                        Log.w("APIDebugging","null response ShowListActivity -> membersAdapter");
                        showNothingFound();
                    }
                }
            });

        }else{
            showNoInternet();
        }
    }

    private void tagAdapter(){
        if(Utils.isNetworkAvailable(this)){
            showLoading();

            PostTask<List<User>> taggedMembers = new PostTask<>();
            taggedMembers.setPostId(mListOrTagId);
            taggedMembers.execute("getTaggedUser");
            taggedMembers.setListener(new PostTask.Response<List<User>>() {
                @Override
                public void response(List<User> userList) {
                    hideLoading();

                    if (userList != null){
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ShowListActivity.this);
                        rvListMembers.setLayoutManager(linearLayoutManager);

                        memberList = userList;
                        singleListAdapter = new SingleListAdapter(ShowListActivity.this, memberList);
                        singleListAdapter.setOnUserClickListener(ShowListActivity.this);
                        rvListMembers.setAdapter(singleListAdapter);
                        rvListMembers.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        rvListMembers.setOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                                    singleListAdapter.setAnimationsLocked(true);
                                }
                            }
                        });
                        // set List Name
                        toolbarTitle.setText(R.string.toolbar_tag);
                        // set member count
                        tvMemberCount.setText(getResources().getQuantityString(R.plurals.members_count, userList.size(), userList.size()));
                    }else {
                        Log.w("APIDebugging", "null response in ShowListActivity -> tagAdapter");
                        showNothingFound();
                    }
                }
            });


        }else{
            showNoInternet();
        }
    }


    public void updateList(View view){
        // send memberList to server and update new members
        // And send user back to List Activity
        startActivity(new Intent(this, ListActivity.class));
    }

    private void startIntroAnimation() {
        ViewCompat.setElevation(toolbar, 0);
        llShowListLayout.setScaleY(0.1f);
        llShowListLayout.setPivotY(drawingStartLocation);

        llShowListLayout.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setElevation(toolbar, Utils.dpToPx(8));
                    }
                })
                .start();
        toolbar.animate()
                .translationY(0)
                .setDuration(300);
    }


    @Override
    public void onBackPressed() {
        ViewCompat.setElevation(toolbar, 0);
        llShowListLayout.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ShowListActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    @Override
    public void onUserClick(View v, Long userId) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        ProfileActivity.startUserProfileFromLocation(startingLocation, this, String.valueOf(userId));
        overridePendingTransition(0, 0);
    }

    @Override
    public void onUserDelete(Long userId, int position) {
        // Do Nothing...
    }

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsListLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        findViewById(R.id.vsListNoInternet).setVisibility(View.VISIBLE);
    }
    private void hideNoInternet(){
        findViewById(R.id.vsListNoInternet).setVisibility(View.GONE);
    }
    private void showNothingFound(){
        findViewById(R.id.vsListNothingFound).setVisibility(View.VISIBLE);
    }
    private void hideNothingFound(){
        findViewById(R.id.vsListNothingFound).setVisibility(View.VISIBLE);
    }
}
