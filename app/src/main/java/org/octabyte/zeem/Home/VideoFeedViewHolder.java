package org.octabyte.zeem.Home;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.ExoUtil;
import org.octabyte.zeem.Utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

import static org.octabyte.zeem.Home.AudioFeedViewHolder.audioPlayer;
import static org.octabyte.zeem.Home.AudioFeedViewHolder.isAudioPlaying;
import static org.octabyte.zeem.Home.FeedAdapter.TAG;

class VideoFeedViewHolder extends RecyclerView.ViewHolder {

    public ImageView
            ivUserProfile;
    private ImageView ivUserBadge;
    public ImageView ivFeedCenter;
    private ImageView ivFeedIcon;
    public ImageView ivLike;
    public ImageView ivFeedCommentPic2;
    public ImageView ivFeedCommentPic1;
    public ImageView ivFeedCommentPic3;

    public TextView
            tvUserFullName;
    private TextView tvFeedLoc;
    private TextView tvFeedTime;
    public TextView tvFeedList;
    private TextView tvFeedCaption;
    public TextView tvCommentUserFullName2;
    private TextView tvFeedComment2;
    public TextView tvCommentUserFullName3;
    private TextView tvFeedComment3;
    private TextView tvShowTime;
    public TextView tvCommentUserFullName1;
    private TextView tvFeedComment1;
    public TextView tvTotalComments;

    public SurfaceView svFeedVideo;

    public ImageView
            btnComments,
            btnLike,
            btnMore;

    public View vBgLike;

    public TextSwitcher tsLikesCounter;

    private View
            llFeedComment1;
    private View llFeedComment2;
    private View llFeedComment3;

    private ImageView ivFeedCommentUserBadge1, ivFeedCommentUserBadge2, ivFeedCommentUserBadge3;
    private TextView tvCommentTime1, tvCommentTime2, tvCommentTime3;

    FeedItem feedItem;

    private Activity mActivity;
    private Context context;

    public static Boolean isVideoPlaying = false;

    private View divider_comment;
    public static SimpleExoPlayer videoPlayer;


    public VideoFeedViewHolder(View itemView, Activity activity) {
        super(itemView);
        initVariable(itemView);
        mActivity = activity;
        context = activity.getApplicationContext();
    }

    private void initVariable(View view){
        divider_comment = view.findViewById(R.id.divider_comment);
        tvShowTime = view.findViewById(R.id.tvShowTime);

        svFeedVideo = view.findViewById(R.id.svFeedVideo);
        ivUserProfile = view.findViewById(R.id.ivUserProfile);
        tvUserFullName = view.findViewById(R.id.tvUserFullName);
        tvFeedLoc = view.findViewById(R.id.tvFeedLoc);
        tvFeedTime = view.findViewById(R.id.tvFeedTime);
        tvFeedList = view.findViewById(R.id.tvFeedList);
        ivFeedCenter = view.findViewById(R.id.ivFeedCenter);
        ivFeedIcon = view.findViewById(R.id.ivFeedIcon);
        ivUserBadge = view.findViewById(R.id.ivUserBadge);
        tvFeedCaption = view.findViewById(R.id.ivFeedCaption);
        btnComments = view.findViewById(R.id.btnComments);
        btnLike = view.findViewById(R.id.btnLike);
        btnMore = view.findViewById(R.id.btnMore);
        vBgLike = view.findViewById(R.id.vBgLike);
        ivLike = view.findViewById(R.id.ivLike1);
        tsLikesCounter = view.findViewById(R.id.tsLikesCounter);

        llFeedComment1 = view.findViewById(R.id.layout_comment1);
        ivFeedCommentPic1 = llFeedComment1.findViewById(R.id.ivCommentProfilePic);
        ivFeedCommentUserBadge1 = llFeedComment1.findViewById(R.id.ivCommentUserBadge);
        tvCommentUserFullName1 = llFeedComment1.findViewById(R.id.tvCommentUserFullName);
        tvCommentTime1 = llFeedComment1.findViewById(R.id.tvCommentTime);
        tvFeedComment1 = llFeedComment1.findViewById(R.id.tvCommentText);

        llFeedComment2 = view.findViewById(R.id.layout_comment2);
        ivFeedCommentPic2 = llFeedComment2.findViewById(R.id.ivCommentProfilePic);
        ivFeedCommentUserBadge2 = llFeedComment2.findViewById(R.id.ivCommentUserBadge);
        tvCommentUserFullName2 = llFeedComment2.findViewById(R.id.tvCommentUserFullName);
        tvCommentTime2 = llFeedComment2.findViewById(R.id.tvCommentTime);
        tvFeedComment2 = llFeedComment2.findViewById(R.id.tvCommentText);

        llFeedComment3 = view.findViewById(R.id.layout_comment3);
        ivFeedCommentPic3 = llFeedComment3.findViewById(R.id.ivCommentProfilePic);
        ivFeedCommentUserBadge3 = llFeedComment3.findViewById(R.id.ivCommentUserBadge);
        tvCommentUserFullName3 = llFeedComment3.findViewById(R.id.tvCommentUserFullName);
        tvCommentTime3 = llFeedComment3.findViewById(R.id.tvCommentTime);
        tvFeedComment3 = llFeedComment3.findViewById(R.id.tvCommentText);

        tvTotalComments = view.findViewById(R.id.tvTotalComments);

    }

    public FeedItem getFeedItem(){
        return feedItem;
    }

    public void bindView(FeedItem feedItem) {
        Log.i(TAG, "bindView");
        this.feedItem = feedItem;

        tvFeedCaption.setText(feedItem.getCaption());
        Utils.applyClickableSpan(mActivity, tvFeedCaption, tvFeedCaption.getText().toString());

        Glide.with(itemView).load(feedItem.getCover())
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_video)).into(ivFeedCenter);

        if(feedItem.getAnonymous()){

            ivUserProfile.setImageResource(R.drawable.ic_anonymous);
            tvUserFullName.setText(R.string.anonymous);

        }else {

            Glide.with(mActivity).load(feedItem.getUserProfilePic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserProfile);
            ivUserBadge.setImageResource(Utils.getBadgePic(feedItem.getUserBadge()));

            tvUserFullName.setText(Utils.capitalize(feedItem.getUserFullName()));

        }

        if(feedItem.getLocation() != null){
            tvFeedLoc.setVisibility(View.VISIBLE);
            tvFeedLoc.setText(feedItem.getLocation());
        }else{
            tvFeedLoc.setVisibility(View.GONE);
        }

        if (feedItem.getExpireTimeHumanReadable() != null) {
            tvFeedTime.setText(feedItem.getPostedOnHumanReadable() + feedItem.getExpireTimeHumanReadable());
        }else {
            tvFeedTime.setText(feedItem.getPostedOnHumanReadable());
        }


        if(feedItem.getPostListId() != null){
            tvFeedList.setVisibility(View.VISIBLE);
            tvFeedList.setText("Available for "+feedItem.getListCount()+" members");
        }else if(feedItem.getPostTag()){
            tvFeedList.setVisibility(View.VISIBLE);

            String postTagText = "";
            if (feedItem.getTaggedMe()) {
                if (feedItem.getTaggedCount() > 1){
                    postTagText = "You and "+(feedItem.getTaggedCount() - 1)+" members are tagged";
                }else{
                    postTagText = "You are tagged";
                }
            }else{
                postTagText = "Tagged with "+ feedItem.getTaggedCount() +" members";
            }

            tvFeedList.setText(postTagText);//feedItem.getTagCount()
        }else{
            tvFeedList.setVisibility(View.GONE);
        }

        //ivFeedCenter.setImageResource(adapterPosition % 2 == 0 ? R.drawable.img_feed_center_1 : R.drawable.img_feed_center_2);

        btnLike.setImageResource(feedItem.getStarByMe() ? R.drawable.ic_fill_star : R.drawable.ic_hollo_star);//feedItem.isLike()
        tsLikesCounter.setCurrentText(mActivity.getResources().getQuantityString(
                R.plurals.likes_count, feedItem.getStarCount(), feedItem.getStarCount()
        ));

        // If comment is allowed on this post then setup comments, else hide comment button
        if (feedItem.getAllowComment())
            setupComments();
        else
            btnComments.setVisibility(View.GONE);

    }

    private void setupComments(){
        // Comments, all recent comments must be text type

        // Set total number of comments
        if (feedItem.getTotalComments() > 0) {
            tvTotalComments.setVisibility(View.VISIBLE);
            tvTotalComments.setText(mActivity.getResources().getQuantityString(
                    R.plurals.total_comments, feedItem.getTotalComments(), feedItem.getTotalComments()
            ));
        } else {
            tvTotalComments.setVisibility(View.GONE);
        }

        if (feedItem.getCommentItems() != null) {
            divider_comment.setVisibility(View.VISIBLE);

            if (feedItem.getCommentItems().size() >= 3) {
                /*llFeedComment1.setVisibility(View.VISIBLE);
                llFeedComment2.setVisibility(View.VISIBLE);
                llFeedComment3.setVisibility(View.VISIBLE);*/

                if (feedItem.getCommentItems().get(0).getAnonymous()){
                    ivFeedCommentPic1.setImageResource(R.drawable.ic_anonymous);
                    tvCommentUserFullName1.setText(R.string.anonymous);
                }else {
                    Glide.with(mActivity).load(feedItem.getCommentItems().get(0).getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                            .apply(RequestOptions.circleCropTransform()).into(ivFeedCommentPic1);
                    ivFeedCommentUserBadge1.setImageResource(Utils.getBadgePic(feedItem.getCommentItems().get(0).getUserBadge()));
                    tvCommentUserFullName1.setText(Utils.capitalize(feedItem.getCommentItems().get(0).getUserFullName()));
                }

                tvCommentTime1.setText(" - "+feedItem.getCommentItems().get(0).getPostedOnHumanReadable());
                tvFeedComment1.setText(feedItem.getCommentItems().get(0).getSource());

                if (feedItem.getCommentItems().get(1).getAnonymous()){
                    ivFeedCommentPic2.setImageResource(R.drawable.ic_anonymous);
                    tvCommentUserFullName2.setText(R.string.anonymous);
                }else {
                    Glide.with(mActivity).load(feedItem.getCommentItems().get(1).getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                            .apply(RequestOptions.circleCropTransform()).into(ivFeedCommentPic2);
                    ivFeedCommentUserBadge2.setImageResource(Utils.getBadgePic(feedItem.getCommentItems().get(1).getUserBadge()));
                    tvCommentUserFullName2.setText(Utils.capitalize(feedItem.getCommentItems().get(1).getUserFullName()));
                }

                tvCommentTime2.setText(" - "+feedItem.getCommentItems().get(1).getPostedOnHumanReadable());
                tvFeedComment2.setText(feedItem.getCommentItems().get(1).getSource());

                if (feedItem.getCommentItems().get(2).getAnonymous()){
                    ivFeedCommentPic3.setImageResource(R.drawable.ic_anonymous);
                    tvCommentUserFullName3.setText(R.string.anonymous);
                }else {
                    Glide.with(mActivity).load(feedItem.getCommentItems().get(2).getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                            .apply(RequestOptions.circleCropTransform()).into(ivFeedCommentPic3);
                    ivFeedCommentUserBadge3.setImageResource(Utils.getBadgePic(feedItem.getCommentItems().get(2).getUserBadge()));
                    tvCommentUserFullName3.setText(Utils.capitalize(feedItem.getCommentItems().get(2).getUserFullName()));
                }

                tvCommentTime3.setText(" - "+feedItem.getCommentItems().get(2).getPostedOnHumanReadable());
                tvFeedComment3.setText(feedItem.getCommentItems().get(2).getSource());
            }else if(feedItem.getCommentItems().size() == 2) {
                /*llFeedComment1.setVisibility(View.VISIBLE);
                llFeedComment2.setVisibility(View.VISIBLE);*/

                if (feedItem.getCommentItems().get(0).getAnonymous()){
                    ivFeedCommentPic1.setImageResource(R.drawable.ic_anonymous);
                    tvCommentUserFullName1.setText(R.string.anonymous);
                }else {
                    Glide.with(mActivity).load(feedItem.getCommentItems().get(0).getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                            .apply(RequestOptions.circleCropTransform()).into(ivFeedCommentPic1);
                    ivFeedCommentUserBadge1.setImageResource(Utils.getBadgePic(feedItem.getCommentItems().get(0).getUserBadge()));
                    tvCommentUserFullName1.setText(Utils.capitalize(feedItem.getCommentItems().get(0).getUserFullName()));
                }

                tvCommentTime1.setText(" - "+feedItem.getCommentItems().get(0).getPostedOnHumanReadable());
                tvFeedComment1.setText(feedItem.getCommentItems().get(0).getSource());

                if (feedItem.getCommentItems().get(1).getAnonymous()){
                    ivFeedCommentPic2.setImageResource(R.drawable.ic_anonymous);
                    tvCommentUserFullName2.setText(R.string.anonymous);
                }else {
                    Glide.with(mActivity).load(feedItem.getCommentItems().get(1).getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                            .apply(RequestOptions.circleCropTransform()).into(ivFeedCommentPic2);
                    ivFeedCommentUserBadge2.setImageResource(Utils.getBadgePic(feedItem.getCommentItems().get(1).getUserBadge()));
                    tvCommentUserFullName2.setText(Utils.capitalize(feedItem.getCommentItems().get(1).getUserFullName()));
                }
                tvFeedComment2.setText(feedItem.getCommentItems().get(1).getSource());
                tvCommentTime2.setText(" - "+feedItem.getCommentItems().get(1).getPostedOnHumanReadable());
                llFeedComment3.setVisibility(View.GONE);
            }else if(feedItem.getCommentItems().size() == 1) {
                /*llFeedComment1.setVisibility(View.VISIBLE);*/
                if (feedItem.getCommentItems().get(0).getAnonymous()){
                    ivFeedCommentPic1.setImageResource(R.drawable.ic_anonymous);
                    tvCommentUserFullName1.setText(R.string.anonymous);
                }else {
                    Glide.with(mActivity).load(feedItem.getCommentItems().get(0).getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                            .apply(RequestOptions.circleCropTransform()).into(ivFeedCommentPic1);
                    ivFeedCommentUserBadge1.setImageResource(Utils.getBadgePic(feedItem.getCommentItems().get(0).getUserBadge()));
                    tvCommentUserFullName1.setText(Utils.capitalize(feedItem.getCommentItems().get(0).getUserFullName()));
                }
                tvCommentTime1.setText(" - "+feedItem.getCommentItems().get(0).getPostedOnHumanReadable());
                tvFeedComment1.setText(feedItem.getCommentItems().get(0).getSource());
                llFeedComment2.setVisibility(View.GONE);
                llFeedComment3.setVisibility(View.GONE);
            }
        }else {
            llFeedComment1.setVisibility(View.GONE);
            llFeedComment2.setVisibility(View.GONE);
            llFeedComment3.setVisibility(View.GONE);
        }

    }


    public void startMyVideo(){

        stopMyAudio();

        if (!isVideoPlaying) {
            isVideoPlaying = true;
            Utils.blinkAnimation(ivFeedIcon);
            tvShowTime.setText(R.string.loading_msg);

            String videoSrc = feedItem.getSource();

            videoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
            videoPlayer.setVideoSurfaceView(svFeedVideo);
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "zeem"));

            ExtractorMediaSource mediaSource;
            if (videoSrc.substring(0,4).equals("http")) {
                CacheDataSourceFactory cacheDataSourceFactory =
                        new CacheDataSourceFactory(ExoUtil.getCache(context), dataSourceFactory);
                mediaSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(Uri.parse(videoSrc));
            } else {
                mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(videoSrc));
            }

            // For GIF
            if(feedItem.getType().equals("GIF"))
                videoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);


            // Prepare media videoPlayer
            videoPlayer.prepare(mediaSource);
            videoPlayer.setPlayWhenReady(true);

            videoPlayer.addListener(new Player.DefaultEventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    switch (playbackState){
                        case Player.STATE_READY:
                            videoIsReady();
                            break;
                        case Player.STATE_ENDED:

                            if (!feedItem.getType().equals("GIF"))
                                stopMyVideo();

                            break;
                    }

                    super.onPlayerStateChanged(playWhenReady, playbackState);
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    videoHasError();

                    super.onPlayerError(error);
                }
            });
        }

    }

    public void stopMyVideo(){
        if (isVideoPlaying) {
            isVideoPlaying = false;

            if (videoPlayer != null) {
                videoPlayer.clearVideoSurface();
                videoPlayer.release();
                videoPlayer = null;
            }

            ivFeedCenter.setVisibility(View.VISIBLE);
            svFeedVideo.setVisibility(View.GONE);
            ivFeedIcon.setVisibility(View.VISIBLE);
            tvShowTime.setVisibility(View.VISIBLE);
            tvShowTime.setText(R.string.tap_to_play);
        }
    }

    private void stopMyAudio() {
        if (isAudioPlaying) {
            isAudioPlaying = false;

            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            }
        }
    }

    private void videoIsReady() {
        ivFeedIcon.clearAnimation();
        ivFeedIcon.setVisibility(View.GONE);
        ivFeedCenter.setVisibility(View.GONE);
        svFeedVideo.setVisibility(View.VISIBLE);

        if (!feedItem.getType().equals("GIF"))
            setupTimer();
        else
            tvShowTime.setText("GIF");
    }

    private void videoHasError() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvShowTime.setText(R.string.error);
                ivFeedIcon.clearAnimation();
            }
        });
    }

    private void setupTimer(){
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isVideoPlaying && videoPlayer != null) {
                    tvShowTime.post(new Runnable() {
                        @Override
                        public void run() {
                            tvShowTime.setText(Utils.milliSecondsToTimer(videoPlayer.getDuration()- videoPlayer.getCurrentPosition()));
                        }
                    });
                } else {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 1000);
    }


}
