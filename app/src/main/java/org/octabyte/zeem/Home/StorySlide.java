package org.octabyte.zeem.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.StoryFeed;
import org.octabyte.zeem.Comment.CommentActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.ExoUtil;
import org.octabyte.zeem.Utils.FocusView;
import org.octabyte.zeem.Utils.Utils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;
import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;


/**
 * Created by Azeem on 2/24/2018.
 */

public class StorySlide extends Fragment {

    public static StoryFeed STORY_SLIDES;

    /**
     * The argument key for the page number this fragment represents.
     */
    private static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mSlideNumber;

    // Video Player for story
    private static Boolean isStoryVideoPlaying = false;

    // Create interface
    private OnStoryLoad onStoryLoad;
    private boolean slideIsStop = false;
    private boolean mediaAlreadyPrepaid = false;
    private View vLoadingView;
    private SimpleExoPlayer storyVideoPlayer;
    private String storyType;
    private static boolean USER_INFO_OPEN = true;

    public void setOnStoryLoad(OnStoryLoad onStoryLoad){
        this.onStoryLoad = onStoryLoad;
    }

    public interface OnStoryLoad{
        void storyLoaded(Boolean removeCallback, Boolean changeSlide, Boolean instantChange);
    }

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given story.
     */
    public static StorySlide create(int slideNumber) {

        // Destroy if any video is playing

        StorySlide storySlide = new StorySlide();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, slideNumber);
        storySlide.setArguments(args);
        return storySlide;
    }

    public StorySlide() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSlideNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (STORY_SLIDES != null) {
                if (!mediaAlreadyPrepaid) {
                    // Stop the slide until it's media load
                    onStoryLoad.storyLoaded(true, false, false);
                    slideIsStop = true;
                }else{
                    if (storyType.equals("VIDEO") || storyType.equals("GIF")) {
                        if (storyVideoPlayer != null) {
                            storyVideoPlayer.setPlayWhenReady(true);
                        }
                    }
                }
            }

        }

    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        storyType = STORY_SLIDES.getStory().getStories().get(mSlideNumber).getType();

        ViewGroup rootView = null;
        if (storyType.equals("IMAGE")) {

            rootView = (ViewGroup) inflater.inflate(R.layout.view_story_image, container, false);

        } else if (storyType.equals("VIDEO") || storyType.equals("GIF")){

            rootView = (ViewGroup) inflater.inflate(R.layout.view_story_video, container, false);

        }

        if (rootView == null)
            return null;

        ImageView userPic = rootView.findViewById(R.id.ivUserProfile);
        Glide.with(this).load(STORY_SLIDES.getUser().getProfilePic())
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                .apply(RequestOptions.circleCropTransform()).into(userPic);

        ((ImageView) rootView.findViewById(R.id.ivUserBadge)).setImageResource(Utils.getBadgePic(STORY_SLIDES.getUser().getBadge()));

        ((TextView) rootView.findViewById(R.id.tvUserFullName)).setText(Utils.capitalize(STORY_SLIDES.getUser().getFullName()));
        ((TextView) rootView.findViewById(R.id.tvStoryTime)).setText("update on "+STORY_SLIDES.getUpdatedOnHumanReadable());

        // Setup click listener for comments
        final ImageView ivStoryComment = rootView.findViewById(R.id.ivStoryComment);
        ivStoryComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStoryLoad.storyLoaded(false, false,false);
                final Intent intent = new Intent(getActivity(), CommentActivity.class);
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                intent.putExtra(CommentActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
                intent.putExtra("STORY_ID", STORY_SLIDES.getStory().getStoryId());
                intent.putExtra("STORY_NUM", STORY_SLIDES.getStory().getStories().get(mSlideNumber).getStoryNum());
                intent.putExtra("STORY_SAFE_KEY", STORY_SLIDES.getStory().getStorySafeKey());
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
            }
        });

        // Setup story according to it's type


        if(storyType.equals("IMAGE")){
            showLoading(rootView);

            ImageView ivStoryPic = rootView.findViewById(R.id.ivStoryPic);
            Glide.with(this).load(STORY_SLIDES.getStory().getStories().get(mSlideNumber).getSource())
                    .apply(RequestOptions.placeholderOf(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.iconColor))))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            onStoryLoad.storyLoaded(false,false,true);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            hideLoading();
                            mediaAlreadyPrepaid = true;

                            if (slideIsStop) {
                                onStoryLoad.storyLoaded(false, true, false);
                            }
                            return false;
                        }
                    }).into(ivStoryPic);
        }else if(
                storyType.equals("VIDEO") ||
                storyType.equals("GIF")
                ){

            String videoSrc = STORY_SLIDES.getStory().getStories().get(mSlideNumber).getSource();

            final View vCover = rootView.findViewById(R.id.vCover);
            SurfaceView svStoryVideo = rootView.findViewById(R.id.svStoryVideo);

            storyVideoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), new DefaultTrackSelector());
            storyVideoPlayer.setVideoSurfaceView(svStoryVideo);
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                    Util.getUserAgent(getContext(), "zeem"));
            ExtractorMediaSource mediaSource;
            if (videoSrc.substring(0,4).equals("http")) {
                CacheDataSourceFactory cacheDataSourceFactory =
                        new CacheDataSourceFactory(ExoUtil.getCache(getContext()), dataSourceFactory);
                mediaSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(Uri.parse(videoSrc));
            } else {
                mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(videoSrc));
            }

            // For GIF
            if(STORY_SLIDES.getStory().getStories().get(mSlideNumber).getType().equals("GIF"))
                storyVideoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

            // Prepare media videoPlayer
            storyVideoPlayer.prepare(mediaSource);
            //storyVideoPlayer.setPlayWhenReady(true);

            // Show loading message
            showLoading(rootView);

            storyVideoPlayer.addListener(new Player.DefaultEventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    switch (playbackState){
                        case Player.STATE_READY:

                            hideLoading();
                            vCover.setVisibility(View.GONE);

                            isStoryVideoPlaying = true;
                            mediaAlreadyPrepaid = true;

                            if (slideIsStop) {
                                onStoryLoad.storyLoaded(false, true, false);
                            }

                            break;
                        case Player.STATE_ENDED:
                            if(!STORY_SLIDES.getStory().getStories().get(mSlideNumber).getType().equals("GIF")) {

                                onStoryLoad.storyLoaded(false,false,true);
                            }
                            break;
                    }

                    super.onPlayerStateChanged(playWhenReady, playbackState);
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    onStoryLoad.storyLoaded(false,false,true);

                    super.onPlayerError(error);
                }
            });

        }

        final SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        boolean firstTime = sharedPreferences.getBoolean("StoryFirstTime", true);

        if (firstTime && USER_INFO_OPEN) {

            USER_INFO_OPEN = false;

            final ViewGroup finalRootView = rootView;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new ShowcaseView.Builder(getActivity())
                            .setTarget(new ViewTarget(R.id.ivStoryComment, getActivity()))
                            .withHoloShowcase()
                            .setStyle(R.style.CustomShowcaseTheme)
                            .setContentTitle(R.string.info_story_comment_title)
                            .setContentText(R.string.info_story_comment_detail)
                            .replaceEndButton(R.layout.show_case_hide_button)
                            .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                                @Override
                                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("StoryFirstTime", false);
                                    editor.apply();
                                }
                            })
                            .build();
                }
            }, 500);
        }

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (storyVideoPlayer != null){
            storyVideoPlayer.clearVideoSurface();
            storyVideoPlayer.release();
            storyVideoPlayer = null;
        }

    }

    private void showLoading(ViewGroup rootView){
        if (vLoadingView == null) {
            vLoadingView = ((ViewStub) rootView.findViewById(R.id.vsStoryLoading)).inflate();
        }else{
            vLoadingView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoading(){
        if (vLoadingView != null) vLoadingView.setVisibility(View.GONE);
    }

}
