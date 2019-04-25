package org.octabyte.zeem.Home;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import org.octabyte.zeem.Profile.RevealBackgroundView;
import org.octabyte.zeem.R;

import jp.shts.android.storiesprogressview.StoriesProgressView;


//import static org.octabyte.zeem.Home.StorySlide.storyVideoPlayer;


/**
 * Created by Azeem on 2/24/2018.
 */

public class StoryActivity extends FragmentActivity implements RevealBackgroundView.OnStateChangeListener, StorySlide.OnStoryLoad {

    private final Handler handler = new Handler();
    private Runnable updateStory;
    private final int slideChangingTime = 10000;
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static int NUM_STORIES;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    // Animation to start this activity
    private static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";
    private static final String ARG_NUM_STORIES = "arg_stories_count";
    private RevealBackgroundView vRevealBackground;
    private boolean killThread = false;
    private boolean instansKill;
    private StoriesProgressView storiesProgressView;
    private boolean slideChangeWithScroll;
    private int viewPageCurrentPosition = -1;

    public static void startStoryFromLocation(int[] startingLocation, Activity startingActivity, int storiesCount) {
        //Log.i("StoryActivity", String.valueOf(storiesCount));
        Intent intent = new Intent(startingActivity, StoryActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra(ARG_NUM_STORIES, storiesCount);
        startingActivity.startActivity(intent);
    }
    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        NUM_STORIES = getIntent().getIntExtra(ARG_NUM_STORIES, 0);
        instansKill = false;

        vRevealBackground = findViewById(R.id.vRevealBackground);
        setupRevealBackground(null); //savedInstanceState

        storiesProgressView = (StoriesProgressView) findViewById(R.id.storiesProgressView);
        storiesProgressView.setStoriesCount(NUM_STORIES);
        storiesProgressView.setStoryDuration(slideChangingTime);

        Log.i("StoryActivityDebug", "onCreate");
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.story);

    }

    private void initSlider(){
        /*
      The pager adapter, which provides the pages to the view pager widget.
     */
        final PagerAdapter mPagerAdapter = new StorySlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        storiesProgressView.startStories();

        updateStory = new Runnable() {
            @Override
            public void run() {

                if (killThread)
                    return;

                Log.i("StorySlideDebug", "next slide");

                /*if (storyVideoPlayer != null){
                    storyVideoPlayer.clearVideoSurface();
                    storyVideoPlayer.release();
                    storyVideoPlayer = null;
                }*/

                if(mPager.getCurrentItem() == NUM_STORIES - 1 || instansKill){
                    Log.i("StorySlideDebug", "Stop this story ");
                    /*if (mStoryVideoPlayerManager != null) {
                        mStoryVideoPlayerManager.stopAnyPlayback();
                        mStoryVideoPlayerManager.resetMediaPlayer();
                    }*/
                    finish();
                }else {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1, false);
                    handler.postDelayed(updateStory, slideChangingTime);
                }
            }
        };
        handler.postDelayed(updateStory, slideChangingTime);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (slideChangeWithScroll){
                    slideChangeWithScroll = false;
                    if (viewPageCurrentPosition != -1){
                        if (i < viewPageCurrentPosition){
                            storiesProgressView.reverse();
                        }else{
                            storiesProgressView.skip();
                        }
                    }
                }
                handler.removeCallbacks(updateStory);
                handler.postDelayed(updateStory, slideChangingTime);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_SETTLING) {
                    slideChangeWithScroll = true;
                }else{
                    viewPageCurrentPosition = mPager.getCurrentItem();
                }
            }
        });
    }

    @Override
    public void storyLoaded(Boolean removeCallback, Boolean changeSlide, Boolean instantChange) {
        if (removeCallback){
            killThread = true;
            handler.removeCallbacks(updateStory);
            storiesProgressView.pause();
        }else if (changeSlide){
            killThread = false;
            handler.postDelayed(updateStory, slideChangingTime);
            storiesProgressView.resume();
        }else if (instantChange){
            killThread = false;
            handler.post(updateStory);
            storiesProgressView.skip();
        }else{
            instansKill = true;
            handler.post(updateStory);
        }
    }

    /**
     * A simple pager adapter that represents objects, in
     * sequence.
     */
    private class StorySlidePagerAdapter extends FragmentStatePagerAdapter {
        StorySlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            StorySlide storySlide = StorySlide.create(position);
            storySlide.setOnStoryLoad(StoryActivity.this);
            return storySlide;
        }

        @Override
        public int getCount() {
            return NUM_STORIES;
        }
    }


    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            //vRevealBackground.setVisibility(View.GONE);
            initSlider();
        }
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }

}