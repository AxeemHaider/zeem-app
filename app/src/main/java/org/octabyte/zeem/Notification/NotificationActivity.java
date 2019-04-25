package org.octabyte.zeem.Notification;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.Notification;
import com.appspot.octabyte_zeem.zeem.model.NotificationItem;
import com.appspot.octabyte_zeem.zeem.model.UserFriendRequest;
import org.octabyte.zeem.API.FriendTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.SinglePost.SinglePostActivity;
import org.octabyte.zeem.Utils.BottomNavigationSetup;
import org.octabyte.zeem.Utils.Utils;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/29/2017.
 */

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener, FriendRequestAdapter.OnFriendRequestClick {
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    private int drawingStartLocation;

    private RelativeLayout lyRoot;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvNotification, rvFriendRequest;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    private Long mUserId;
    private TextView tvFriendRequest, tvClearNotification;
    private Boolean isNotification = true;
    private Boolean isRequestAdapterSet = false;
    private String notificationMode;
    private String cursor;
    public static boolean NEW_NOTIFICATION = false;
    private View vLoadingView, vNothingFoundView, vNoInternetView, vMiniLoadingView;
    private TabLayout notificationTabLayout;
    private FriendRequestAdapter requestAdapter;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] writeContactPermission = new String[]{Manifest.permission.WRITE_CONTACTS};
    private SharedPreferences pref;

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

        setContentView(R.layout.activity_notification);
        initVariable();

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);

        notificationMode = getIntent().getStringExtra("ARG_NAV_SENDER_MODE_TYPE");

        boolean isForFriendRequest = getIntent().getBooleanExtra("ARG_NOTIFICATION_FOR_REQUEST", false);

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

        BottomNavigationSetup.init(bottomNavigation, NotificationActivity.this, 2, notificationMode);

        if (isForFriendRequest) {
            TabLayout.Tab tab = notificationTabLayout.getTabAt(1);
            if (tab != null) {
                tab.select();
            }
        }else {
            setupNotificationAdapter();
        }

    }
    private void initVariable(){
        lyRoot = findViewById(R.id.root);
        Toolbar toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvNotification = findViewById(R.id.rvNotification);
        rvFriendRequest = findViewById(R.id.rvFriendRequest);

        notificationTabLayout = findViewById(R.id.notificationTabLayout);

        notificationTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0){
                    showNotification();
                }else if (tab.getPosition() == 1){
                    showFriendRequest();
                    if (!isRequestAdapterSet){
                        setupFriendRequestAdapter();
                        isRequestAdapterSet = true;
                    }
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

    private void showFriendRequest(){
        hideNothingFound();

        rvFriendRequest.setVisibility(View.VISIBLE);
        rvNotification.setVisibility(View.GONE);
    }

    private void showNotification(){
        hideNothingFound();

        rvNotification.setVisibility(View.VISIBLE);
        rvFriendRequest.setVisibility(View.GONE);
    }

    private void setupNotificationAdapter(){

        boolean offlineNotificationNotFound = true;

        hideNoInternet();
        hideNothingFound();

        if(!NEW_NOTIFICATION){

            String fileName = "private_notification";
            if (notificationMode.equals("PUBLIC"))
                fileName = "public_notification";

            File offlineNotification = Utils.getOutputJsonFile(this, fileName);

            if ((offlineNotification != null ? offlineNotification.length() : 0) > 0) { // Here is some offline feeds
                JacksonFactory jacksonFactory = new JacksonFactory();
                try {

                    offlineNotificationNotFound = false;
                    InputStream inputStream = new FileInputStream(offlineNotification);

                    Notification notification = jacksonFactory.fromInputStream(inputStream, Notification.class);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationActivity.this);
                    rvNotification.setLayoutManager(linearLayoutManager);
                    rvNotification.setHasFixedSize(true);

                    cursor = notification.getCursor();
                    notificationList = notification.getNotificationList();

                    notificationAdapter = new NotificationAdapter(NotificationActivity.this, notificationList);
                    notificationAdapter.setOnNotificationClickListener(NotificationActivity.this);
                    rvNotification.setAdapter(notificationAdapter);
                    rvNotification.setOverScrollMode(View.OVER_SCROLL_NEVER);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

            if (Utils.isNetworkAvailable(this)) {

            if (offlineNotificationNotFound) {
                showLoading();
            }else{
                showMiniLoading();
            }

                UserTask<Notification> notification = new UserTask<>(mUserId, notificationMode);
                notification.execute("getNotification");
                notification.setListener(new UserTask.Response<Notification>() {
                    @Override
                    public void response(Notification response) {
                        hideLoading();
                        hideMiniLoading();

                        if (response.getNotificationList() != null) {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationActivity.this);
                            rvNotification.setLayoutManager(linearLayoutManager);
                            rvNotification.setHasFixedSize(true);

                            cursor = response.getCursor();
                            notificationList = response.getNotificationList();

                            notificationAdapter = new NotificationAdapter(NotificationActivity.this, notificationList);
                            notificationAdapter.setOnNotificationClickListener(NotificationActivity.this);
                            rvNotification.setAdapter(notificationAdapter);
                            rvNotification.setOverScrollMode(View.OVER_SCROLL_NEVER);
                        } else {
                            Log.w("APIDebugging", "null response in NotificationActivity->setupNotificationAdapter");
                            showNothingFound();
                        }
                    }
                });
            } else {
                showNoInternet();
            }

    }

    private void setupFriendRequestAdapter(){

        hideNoInternet();
        hideNothingFound();

        if (Utils.isNetworkAvailable(this)) {
            showLoading();

            UserTask<List<UserFriendRequest>> friendRequest = new UserTask<>(mUserId);
            friendRequest.execute("getFriendRequest");
            friendRequest.setListener(new UserTask.Response<List<UserFriendRequest>>() {
                @Override
                public void response(List<UserFriendRequest> response) {
                    hideLoading();
                    if (response.size() > 0) {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationActivity.this);
                        rvFriendRequest.setLayoutManager(linearLayoutManager);
                        rvFriendRequest.setHasFixedSize(true);

                        requestAdapter = new FriendRequestAdapter(NotificationActivity.this, response);
                        requestAdapter.setOnFriendRequestClick(NotificationActivity.this);
                        rvFriendRequest.setAdapter(requestAdapter);
                    }else {
                        Log.w("APIDebugging", "null response in NotificationActivity->setupFriendRequestAdapter");
                        showNothingFound();
                    }
                }
            });
        } else {
            showNoInternet();
        }

    }

    private void startIntroAnimation() {
        //ViewCompat.setElevation(appBarLayout, 0);
        lyRoot.setScaleY(0.1f);
        lyRoot.setPivotY(drawingStartLocation);

        lyRoot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }
                })
                .start();
    }
    @Override
    public void onBackPressed() {
        //ViewCompat.setElevation(toolbar, 0);
        lyRoot.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        NotificationActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.getMenu().getItem(2).setChecked(true);
    }

    @Override
    public void onBottomReached(int position) {

        if (cursor != null && notificationAdapter != null){
            UserTask<Notification> notification = new UserTask<>(mUserId, notificationMode);
            notification.setCursor(cursor);
            notification.execute("getNotification");
            notification.setListener(new UserTask.Response<Notification>() {
                @Override
                public void response(Notification response) {
                    if (response.getNotificationList() != null) {
                        cursor = response.getCursor();
                        notificationAdapter.updateItemsAtEnd(response.getNotificationList());
                    }else {
                        Log.w("APIDebugging", "null response in NotificationActivity->onBottomReached");
                    }
                }
            });
        }

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
    public void onStatusClick(View v, String postSafeKey) {
        Intent intent = new Intent(this, SinglePostActivity.class);
        intent.putExtra("ARG_SINGLE_POST_SAFE_KEY", postSafeKey);
        intent.putExtra("ARG_SINGLE_POST_POST_MODE", notificationMode);
        startActivity(intent);
    }

    @Override
    public void onFriendRequestClick(Long requestId, Long friendId, Long phone, String name, int adapterPosition) {
        // Also add the number in Phone contact list
        if (Utils.isNetworkAvailable(this)) {

            Toast.makeText(this, R.string.request_accept, Toast.LENGTH_SHORT).show();
            requestAdapter.removeItem(adapterPosition);

            FriendTask<Void> acceptFriendRequest = new FriendTask<>(mUserId);
            acceptFriendRequest.setRequestId(requestId);
            acceptFriendRequest.setFriendId(friendId);
            acceptFriendRequest.execute("acceptFriendRequest");

            // Save Request person phone number in mobile
            if(ActivityCompat.checkSelfPermission(NotificationActivity.this, writeContactPermission[0]) == PackageManager.PERMISSION_GRANTED) {
                createNewContact(name, String.valueOf(phone));
            }else{
                checkRuntimePermission();
            }


        } else {
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
        }
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
                        ActivityCompat.requestPermissions(NotificationActivity.this,writeContactPermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (pref.getBoolean("write_contact_permission",false)) {
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

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("write_contact_permission",true);
            editor.apply();
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

    @Override
    protected void onStop() {
        super.onStop();

        if (notificationAdapter != null) {
            // Save Feeds into local file for offline use
            JacksonFactory jacksonFactory = new JacksonFactory();

            // Create Feed object
            Notification notification = new Notification();
            notification.setCursor(cursor);
            notification.setNotificationList(notificationAdapter.getNotificationList());

            String fileName = "private_notification";

            if (notificationMode.equals("PUBLIC"))
                fileName = "public_notification";

            // Save into Local json file
            try {
                byte[] postFeedBytes = jacksonFactory.toByteArray(notification);

                File outPutFile = Utils.getOutputJsonFile(this,fileName);

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

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsNotificationLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showMiniLoading(){
        if (vMiniLoadingView == null) {
            vMiniLoadingView = ((ViewStub) findViewById(R.id.vsNotificationMiniLoading)).inflate();
        }else{
            vMiniLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideMiniLoading(){
        if (vMiniLoadingView != null) vMiniLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsNotificationNoInternet)).inflate();
            Button btnInternetRetry = findViewById(R.id.btnInternetRetry);
            btnInternetRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupNotificationAdapter();
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
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsNotificationNothingFound)).inflate();
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }
}
