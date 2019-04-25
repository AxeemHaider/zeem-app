package org.octabyte.zeem.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.UserList;
import org.octabyte.zeem.API.ListTask;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/18/2017.
 */

public class ListActivity extends AppCompatActivity implements ListAdapter.OnListItemClickListener {

    private static final String TAG = "com.azeem.DList";

    private RecyclerView rvLists;

    private ListAdapter listAdapter;
    private List<UserList> listItems;
    private Long USER_ID;
    private Boolean forPublishStatus;
    private View vLoadingView, vNoInternetView, vNothingFoundView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        USER_ID = pref.getLong("userId", 123L);

        forPublishStatus = getIntent().getBooleanExtra("OPEN_LIST_FOR_PUBLISH_STATUS", false);

        initVariable();

        setupListAdapter();
    }
    private void initVariable(){
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        rvLists = findViewById(R.id.rvLists);
        TextView tvListAction = findViewById(R.id.tvListAction);

        tvListAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewList();
            }
        });

        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setupListAdapter(){
        if (Utils.isNetworkAvailable(this)) {
            showLoading();

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            rvLists.setLayoutManager(linearLayoutManager);

            ListTask<List<UserList>> userList = new ListTask<>(USER_ID);
            userList.execute("getUserList");
            userList.setListener(new ListTask.Response<List<UserList>>() {
                @Override
                public void response(List<UserList> response) {
                    hideLoading();

                    if (response != null){

                        if (response.size() > 0) {
                            listItems = response;
                            listAdapter = new ListAdapter(ListActivity.this, listItems);
                            listAdapter.setOnListItemClickListener(ListActivity.this);
                            rvLists.setAdapter(listAdapter);
                            rvLists.setOverScrollMode(View.OVER_SCROLL_NEVER);
                            rvLists.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                                        listAdapter.setAnimationsLocked(true);
                                    }
                                }
                            });
                        } else {
                            Log.w("APIDebugging","null response in ListActivity -> setupListAdapter");
                            showNothingFound();
                        }
                    }else {
                        Log.w("APIDebugging","null response in ListActivity -> setupListAdapter");
                        showNothingFound();
                    }
                }
            });

        }else{
            showNoInternet();
        }
    }

    private void createNewList(){
        startActivity(new Intent(this, CreateListActivity.class));
    }

    @Override
    public void onListClick(View v, Long listId, String listName, int memberCount) {
        if (forPublishStatus) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("USER_SELECTED_LIST_ID", listId);
            returnIntent.putExtra("USER_SELECTED_LIST_MEMBER_COUNT", memberCount);
            setResult(ListActivity.RESULT_OK, returnIntent);
            finish();
        }else{
            final Intent intent = new Intent(this, UpdateListActivity.class);
            int[] startingLocation = new int[2];
            v.getLocationOnScreen(startingLocation);
            intent.putExtra(UpdateListActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
            intent.putExtra(UpdateListActivity.LIST_ID, listId);
            intent.putExtra("LIST_ACTIVITY_LIST_NAME", listName);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onListDelete(String listSafeKey, int position) {
        // Send listId to server to remove list
        if (Utils.isNetworkAvailable(this)) {
            ListTask<Void> removeList = new ListTask<>();
            removeList.setListSafeKey(listSafeKey);
            removeList.execute("removeList");
            listAdapter.listItems.remove(position);
            listAdapter.notifyItemRemoved(position);
        }else{
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //moveTaskToBack(false);
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
            title.setText(R.string.nothing_found_title_list);
            message.setText(R.string.nothing_found_mesaage1);
            messageLine2.setText(R.string.nothing_found_message2);
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
}
