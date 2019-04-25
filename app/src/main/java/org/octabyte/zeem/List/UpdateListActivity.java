package org.octabyte.zeem.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.User;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.octabyte.zeem.API.ListTask;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.FocusView;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/25/2017.
 */

public class UpdateListActivity extends AppCompatActivity implements SingleListAdapter.OnUserClickListener {

    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    public static final String LIST_ID = "list_id";
    private int drawingStartLocation;

    private LinearLayout llUpdateListLayout;
    private Toolbar toolbar;
    //private TextView tvFriendCount;

    private RecyclerView rvListMembers;
    private List<User> memberList;
    private SingleListAdapter singleListAdapter;
    private Long listId;
    private TextView toolbarTitle;
    private View vLoadingView, vNoInternetView, vNothingFoundView;
    private SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initVariable();

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);

        listId = getIntent().getLongExtra(LIST_ID, 123L);
        String listName = getIntent().getStringExtra("LIST_ACTIVITY_LIST_NAME");

        int actionbarSize = Utils.dpToPx(50);
        toolbar.setTranslationY(-actionbarSize);
        toolbarTitle.setText(listName);

        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            llUpdateListLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    llUpdateListLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });
        }
        membersAdapter();

        showUserInfoFirstTime();
    }
    private void initVariable(){
        TextView tvListAction = findViewById(R.id.tvListAction);
        tvListAction.setText(R.string.toolbar_update);
        tvListAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateList();
            }
        });

        toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);

        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        llUpdateListLayout = findViewById(R.id.llMainLayout);
        toolbar = findViewById(R.id.toolbar);
        rvListMembers = findViewById(R.id.rvLists);
        //tvFriendCount = (TextView) findViewById(R.id.tvFriendCount);
    }

    private void showUserInfoFirstTime(){
        boolean firstTime = pref.getBoolean("UpdateListFirstTime", true);

        if (firstTime) {
            new ShowcaseView.Builder(this)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(R.string.info_list_remove_member_title)
                    .setContentText(R.string.info_list_remove_member_detail)
                    .replaceEndButton(R.layout.show_case_hide_button)
                    .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("UpdateListFirstTime", false);
                            editor.apply();
                        }
                    })
                    .build();
        }

    }

    private void membersAdapter(){
        if(Utils.isNetworkAvailable(this)){
            showLoading();

            ListTask<List<User>> listMembers = new ListTask<>();
            listMembers.setListId(listId);
            listMembers.execute("getListMembers");
            listMembers.setListener(new ListTask.Response<List<User>>() {
                @Override
                public void response(List<User> userList) {
                    hideLoading();

                    if (userList != null){
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UpdateListActivity.this);
                        rvListMembers.setLayoutManager(linearLayoutManager);

                        memberList = userList;
                        singleListAdapter = new SingleListAdapter(UpdateListActivity.this, memberList);
                        singleListAdapter.setOnUserClickListener(UpdateListActivity.this);
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
                        // set member count
                        //tvFriendCount.setText(getResources().getQuantityString(R.plurals.members_count, userList.size(), userList.size()));
                    }else {
                        Log.w("APIDebugging", "null response in UpdateListActivity -> membersAdapter");
                        showNothingFound();
                    }
                }
            });

        }else{
            showNoInternet();
        }
    }

    private void updateList(){

        onBackPressed();
    }

    private void startIntroAnimation() {
        ViewCompat.setElevation(toolbar, 0);
        llUpdateListLayout.setScaleY(0.1f);
        llUpdateListLayout.setPivotY(drawingStartLocation);

        llUpdateListLayout.animate()
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
        llUpdateListLayout.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        UpdateListActivity.super.onBackPressed();
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
        // Send listId to server to remove list
        //memberList.remove(position);
        if (Utils.isNetworkAvailable(this)) {
            ListTask<Void> removeMember = new ListTask<>();
            removeMember.setListId(listId);
            removeMember.setUserId(userId); // user that you want to remove
            removeMember.execute("removeMember");

            singleListAdapter.membersList.remove(position);
            singleListAdapter.notifyItemRemoved(position);
            //tvFriendCount.setText(this.getResources().getQuantityString(R.plurals.members_count, singleListAdapter.membersList.size(), singleListAdapter.membersList.size()));
        } else {
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
        }

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
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsListNoInternet)).inflate();
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
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsListNothingFound)).inflate();
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
}
