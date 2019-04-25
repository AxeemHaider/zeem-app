package org.octabyte.zeem.Friend;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.appspot.octabyte_zeem.zeem.model.User;
import org.octabyte.zeem.API.FriendTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/25/2017.
 */

public class FriendActivity extends AppCompatActivity implements FriendAdapter.OnFriendListClickListener {

    private RecyclerView rvFriends;
    private TabLayout friendsTabLayout;
    private EditText etSearch;
    private List<User> friendList, followerList, followingList, blockFriendList;
    private FriendAdapter friendAdapter;

    private Long myUserId;
    private View vLoadingView, vNothingFoundView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = this.getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color of status bar
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.defaultBackground));
        }

        setContentView(R.layout.activity_friend);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        myUserId = sharedPreferences.getLong("userId", 123L);

        initVariable();
        setupTabs();
        setupSearch();

        // Check request is for friends or for followers
        String request = getIntent().getStringExtra("REQUEST_MODE");

        // if request mode is friend setup friend adapter and set first tab active
        if(request.equals("friends")){
            friendsTabLayout.getTabAt(0).select();
            setupFriendAdapter();
        }else if(request.equals("followers")){
            friendsTabLayout.getTabAt(1).select();
            setupFollowerAdapter();
        }else if(request.equals("following")){
            friendsTabLayout.getTabAt(2).select();
            setupFollowingAdapter();
        }

    }
    private void initVariable(){
        rvFriends = findViewById(R.id.rvFriends);
        friendsTabLayout = findViewById(R.id.friendsTabLayout);
        etSearch = findViewById(R.id.etSearch);
    }
    private void setupTabs() {
        friendsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0){
                    setupFriendAdapter();
                }else if(tab.getPosition() == 1){
                    setupFollowerAdapter();
                }else if(tab.getPosition() == 2){
                    setupFollowingAdapter();
                }else if(tab.getPosition() == 3){
                    setupBlockFriendAdapter();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    private void setupSearch(){
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }
    private void filter(String text){
        if (friendList != null) {
            List<User> temp = new ArrayList<>();
            for(User fm: friendList){
                if(fm.getFullName().toLowerCase().contains(text))
                    temp.add(fm);
            }
            friendAdapter.searchFilter(temp);
        }
    }

    private void setupFriendAdapter(){

        hideNothingFound();
        if (friendList == null) {
            if (Utils.isNetworkAvailable(this)) {
                showLoading();

                UserTask<List<User>> userFriends = new UserTask<>(myUserId);
                userFriends.setRelationType("FRIEND");
                userFriends.execute("getRelation");
                userFriends.setListener(new UserTask.Response<List<User>>() {
                    @Override
                    public void response(List<User> response) {
                        hideLoading();


                        if (response != null) {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FriendActivity.this);
                            rvFriends.setLayoutManager(linearLayoutManager);

                            friendList = response;
                            friendAdapter = new FriendAdapter(FriendActivity.this, friendList, FriendAdapter.RelationType.FRIEND);
                            friendAdapter.setOnFriendListClickListener(FriendActivity.this);
                            rvFriends.setAdapter(friendAdapter);
                            rvFriends.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        }else {
                            Log.w("APIDebugging", "null response in FriendActivity->setupFriendAdapter");
                            showNothingFound();
                        }
                    }
                });
            }else {
                showNoInternet();
            }
        }else {
            friendAdapter.setNewList(friendList);
        }

    }
    private void setupFollowerAdapter(){

        hideNothingFound();

        if (followerList == null) {
            if (Utils.isNetworkAvailable(this)) {
                showLoading();

                UserTask<List<User>> userFriends = new UserTask<>(myUserId);
                userFriends.setRelationType("FOLLOWER");
                userFriends.execute("getRelation");
                userFriends.setListener(new UserTask.Response<List<User>>() {
                    @Override
                    public void response(List<User> response) {
                        hideLoading();

                        if (response != null) {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FriendActivity.this);
                            rvFriends.setLayoutManager(linearLayoutManager);

                            followerList = response;
                            friendAdapter = new FriendAdapter(FriendActivity.this, followerList, FriendAdapter.RelationType.FOLLOWER);
                            friendAdapter.setOnFriendListClickListener(FriendActivity.this);
                            rvFriends.setAdapter(friendAdapter);
                            rvFriends.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        }else {
                            Log.w("APIDebugging", "null response in FriendActivity->setupFollowerAdapter");
                            showNothingFound();
                        }
                    }
                });
            }else {
                showNoInternet();
            }
        }else {
            friendAdapter.setNewList(followerList);
        }
    }
    private void setupFollowingAdapter(){
        hideNothingFound();

        if (followingList == null) {
            if (Utils.isNetworkAvailable(this)) {
                showLoading();

                UserTask<List<User>> userFriends = new UserTask<>(myUserId);
                userFriends.setRelationType("FOLLOWING");
                userFriends.execute("getRelation");
                userFriends.setListener(new UserTask.Response<List<User>>() {
                    @Override
                    public void response(List<User> response) {
                        hideLoading();

                        if (response != null) {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FriendActivity.this);
                            rvFriends.setLayoutManager(linearLayoutManager);

                            followingList = response;
                            friendAdapter = new FriendAdapter(FriendActivity.this, followingList, FriendAdapter.RelationType.FOLLOWING);
                            friendAdapter.setOnFriendListClickListener(FriendActivity.this);
                            rvFriends.setAdapter(friendAdapter);
                            rvFriends.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        }else {
                            Log.w("APIDebugging", "null response in FriendActivity->setupFollowingAdapter");
                            showNothingFound();
                        }
                    }
                });
            } else {
                showNoInternet();
            }
        } else {
            friendAdapter.setNewList(followingList);
        }
    }
    private void setupBlockFriendAdapter(){
        hideNothingFound();

        if (blockFriendList == null) {
            if (Utils.isNetworkAvailable(this)) {
                showLoading();

                UserTask<List<User>> userFriends = new UserTask<>(myUserId);
                userFriends.setRelationType("BLOCK_FRIEND");
                userFriends.execute("getRelation");
                userFriends.setListener(new UserTask.Response<List<User>>() {
                    @Override
                    public void response(List<User> response) {
                        hideLoading();

                        if (response != null) {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FriendActivity.this);
                            rvFriends.setLayoutManager(linearLayoutManager);

                            blockFriendList = response;
                            friendAdapter = new FriendAdapter(FriendActivity.this, blockFriendList, FriendAdapter.RelationType.BLOCK_FRIEND);
                            friendAdapter.setOnFriendListClickListener(FriendActivity.this);
                            rvFriends.setAdapter(friendAdapter);
                            rvFriends.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        }else {
                            Log.w("APIDebugging", "null response in FriendActivity->setupBlockFriendAdapter");
                            showNothingFound();
                        }
                    }
                });
            } else {
                showNoInternet();
            }
        } else {
            friendAdapter.setNewList(blockFriendList);
        }
    }

    @Override
    public void onBlock(Long friendId) {
        FriendTask<Void> friendTask = new FriendTask<>(myUserId);
        friendTask.setFriendId(friendId);
        friendTask.execute("blockFriend");
    }

    @Override
    public void onUnBlock(Long friendId) {
        FriendTask<Void> friendTask = new FriendTask<>(myUserId);
        friendTask.setFriendId(friendId);
        friendTask.execute("unBlockFriend");
    }

    @Override
    public void onProfileClick(View v, Long userId) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2; //2
        ProfileActivity.startUserProfileFromLocation(startingLocation, this, String.valueOf(userId));
        overridePendingTransition(0, 0);
    }

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsFriendLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }

    private void showNoInternet(){
        findViewById(R.id.vsFriendNoInternet).setVisibility(View.VISIBLE);
    }
    private void hideNoInternet(){
        findViewById(R.id.vsFriendNoInternet).setVisibility(View.GONE);
    }
    private void showNothingFound(){
        if (vNothingFoundView == null) {
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsFriendNothingFound)).inflate();
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
}
