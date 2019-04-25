package org.octabyte.zeem.Like;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
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

import com.appspot.octabyte_zeem.zeem.model.User;

import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Azeem on 8/1/2017.
 */

public class LikeActivity extends AppCompatActivity implements LikeAdapter.OnLikeItemClickListener{
    private static final String TAG = "com.azeem.DL";
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    public static final String POST_ID = "post_id";

    private Toolbar toolbar;
    private LinearLayout contentRoot;
    private RecyclerView rvUsers;

    private LikeAdapter likeAdapter;

    private int drawingStartLocation;
    private InputStream raw;

    private String mPostSafeKey;
    private View vLoadingView, vNoInternetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mPostSafeKey = getIntent().getStringExtra(POST_ID);

        initVariable();
        setupLikeAdapter(true);

        int actionbarSize = Utils.dpToPx(50);
        toolbar.setTranslationY(-actionbarSize);


        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            contentRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contentRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });
        }
    }
    private void initVariable(){
        toolbar = findViewById(R.id.toolbar);
        ImageView ivToolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        TextView tvToolbarTitle = findViewById(R.id.toolbarTitle);

        tvToolbarTitle.setText(R.string.toolbar_likes);
        ivToolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        contentRoot = findViewById(R.id.llMainLayout);
        rvUsers = findViewById(R.id.rvUsers);

    }

    private void setupLikeAdapter(Boolean setAdapter){
        if(Utils.isNetworkAvailable(this)){
            if (setAdapter) {

                showLoading();

                PostTask<List<User>> postStars = new PostTask<>(mPostSafeKey);
                postStars.execute("getPostStar");
                postStars.setListener(new PostTask.Response<List<User>>() {
                    @Override
                    public void response(List<User> response) {
                        hideLoading();

                        if (response != null) {
                            setupAdapter(response);
                        }else {
                            Log.w("APIDebugging", "null response in LikeActivity->setupLikeAdapter");
                            showNothingFound();
                        }
                    }
                });

            }
        }else{
            showNoInternet();
        }
    }

    private void setupAdapter(List<User> userList) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvUsers.setLayoutManager(linearLayoutManager);
        rvUsers.setHasFixedSize(true);

        List<User> userList1 = userList;

        likeAdapter = new LikeAdapter(this, userList);
        likeAdapter.setOnLikeItemClickListener(LikeActivity.this);
        rvUsers.setAdapter(likeAdapter);
        rvUsers.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rvUsers.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    likeAdapter.setAnimationsLocked(true);
                }
            }
        });
    }

    private void startIntroAnimation() {
        ViewCompat.setElevation(toolbar, 0);
        contentRoot.setScaleY(0.1f);
        contentRoot.setPivotY(drawingStartLocation);

        contentRoot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setElevation(toolbar, Utils.dpToPx(8));
                        //animateContent();
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
        contentRoot.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        LikeActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    @Override
    public void onProfileClick(View v, Long profileId) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2; //2
        ProfileActivity.startUserProfileFromLocation(startingLocation, this, String.valueOf(profileId));
        overridePendingTransition(0, 0);
    }

    private void showLoading(){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) findViewById(R.id.vsUserLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsUserNoInternet)).inflate();
            Button btnInternetRetry = findViewById(R.id.btnInternetRetry);
            btnInternetRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupLikeAdapter(true);
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
        findViewById(R.id.vsUserNothingFound).setVisibility(View.VISIBLE);
    }
}