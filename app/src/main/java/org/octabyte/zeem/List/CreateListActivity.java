package org.octabyte.zeem.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.User;
import com.appspot.octabyte_zeem.zeem.model.UserList;
import org.octabyte.zeem.API.ListTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/19/2017.
 */

public class CreateListActivity extends AppCompatActivity implements FriendAdapter.OnFriendListClickListener {

    private static final String TAG = "com.azeem.DCreateList";

    private Toolbar toolbar;
    private TextSwitcher tsFriendCount;
    private int friendCount = 0;
    private EditText etListName, etSearchFriends;
    private RecyclerView rvFriends;

    private List<User> friendsList;

    private Long mUserId;
    private FriendAdapter friendAdapter;
    private List<Long> listUser = new ArrayList<>();
    private View vLoadingView, vNoInternetView, vNothingFoundView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);
        initVariable();

        SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);

        if(Utils.isNetworkAvailable(this)) {
            setupFriends();
        }else {
            showNoInternet();
        }
    }
    private void initVariable(){
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        TextView tvListAction = findViewById(R.id.tvListAction);

        etListName = findViewById(R.id.etListName);
        etSearchFriends = findViewById(R.id.etSearchFriends);
        rvFriends = findViewById(R.id.rvFriends);
        tsFriendCount = findViewById(R.id.tsFriendCount);

        tvListAction.setText(R.string.toolbar_action_done);
        tvListAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createList();
            }
        });

        toolbarTitle.setText(R.string.toolbar_new_list);
        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setupFriends(){
        showLoading();

        UserTask<List<User>> userFriends = new UserTask<>(mUserId);
        userFriends.setRelationType("FRIEND");
        userFriends.execute("getRelation");
        userFriends.setListener(new UserTask.Response<List<User>>() {
            @Override
            public void response(List<User> response) {
                hideLoading();

                if (response != null) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CreateListActivity.this);
                    rvFriends.setLayoutManager(linearLayoutManager);

                    friendsList = response;
                    friendAdapter = new FriendAdapter(CreateListActivity.this, friendsList);
                    friendAdapter.setOnFriendListClickListener(CreateListActivity.this);
                    rvFriends.setAdapter(friendAdapter);
                    rvFriends.setOverScrollMode(View.OVER_SCROLL_NEVER);

                    setupSearch();
                }else {
                    Log.w("APIDebugging", "null response in CreateListActivity -> setupFriends");
                    showNothingFound();
                }
            }
        });
    }

    private void setupSearch(){
        etSearchFriends.addTextChangedListener(new TextWatcher() {
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
        if (friendsList != null) {
            List<User> temp = new ArrayList<>();
            for(User fm: friendsList){
                if(fm.getFullName().toLowerCase().contains(text))
                    temp.add(fm);
            }
            friendAdapter.searchFilter(temp);
        }
    }

    private void createList(){
        // Get also a picture path for list
        // also check user has at least add one member
        // after checking all these send list to server
        // and send user back to list Activity
        if (Utils.isNetworkAvailable(this)) {
            String listName = etListName.getText().toString();
            if(listName.isEmpty()) {
                Toast.makeText(this, "Enter Circle Name", Toast.LENGTH_SHORT).show();
            }else if(friendCount == 0){
                Toast.makeText(this, "Enter Some Members", Toast.LENGTH_SHORT).show();
            }else {
                showLoading();

                UserList userList = new UserList();
                userList.setName(listName);
                userList.setMemberCount(friendCount);

                ListTask<Void> createList = new ListTask<>(mUserId);
                createList.setUserList(userList);
                createList.setMemberIds(listUser);
                createList.execute("createList");
                createList.setListener(new ListTask.Response<Void>() {
                    @Override
                    public void response(Void response) {
                        Intent intent = new Intent(CreateListActivity.this, ListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });

            }
        } else {
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFriendSelect(Long userId) {
        // save user id from list
        if (!listUser.contains(userId)) {
            listUser.add(userId);
        }
        friendCount++;
        tsFriendCount.setCurrentText(this.getResources().getQuantityString(R.plurals.members_count, friendCount, friendCount));
        tsFriendCount.setText(this.getResources().getQuantityString(R.plurals.members_count, friendCount, friendCount));
    }
    @Override
    public void onFriendDeSelect(Long userId) {
        // delete user id from list
        listUser.remove(userId);
        friendCount--;
        tsFriendCount.setCurrentText(this.getResources().getQuantityString(R.plurals.members_count, friendCount));
        tsFriendCount.setText(this.getResources().getQuantityString(R.plurals.members_count, friendCount, friendCount));
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
            TextView title = findViewById(R.id.tvNothingFoundTitle);
            TextView message = findViewById(R.id.tvNothingFoundMessage);
            TextView messageLine2 = findViewById(R.id.tvNothingFoundMessageLine2);
            title.setText(R.string.nothing_found_title_in_create_list);
            message.setText(R.string.nothing_found_message_in_create_list);
            messageLine2.setText(R.string.nothing_found_message2_in_create_list);
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
}
