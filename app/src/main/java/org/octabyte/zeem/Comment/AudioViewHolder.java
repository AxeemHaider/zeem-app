package org.octabyte.zeem.Comment;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.CommentItem;
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

class AudioViewHolder extends RecyclerView.ViewHolder {

    public ImageView ivCommentProfilePic;
    public ImageView ivLike;
    private ImageView ivCommentUserBadge;
    private ImageView ivCommentSoundWaves;
    public ImageView ivCommentAudioPlay;
    public TextView tvUserFullName;
    private TextView tvCommentTime;
    public TextView tvReply;
    public TextView tvLikeCount;
    private TextView tvCommentAudioTime;
    public LinearLayout llCommentLayout;

    public CommentItem commentItem;

    private Boolean isAudioPlaying = false;
    private Context context;
    private static SimpleExoPlayer audioPlayer;
    private String commentSrc;

    public AudioViewHolder(View itemView, Context context) {
        super(itemView);
        initVariable(itemView);
        this.context = context;
    }

    private void initVariable(View view){
        llCommentLayout = view.findViewById(R.id.llCommentLayout);
        ivCommentProfilePic = view.findViewById(R.id.ivCommentProfilePic);
        ivCommentUserBadge = view.findViewById(R.id.ivCommentUserBadge);
        ivCommentSoundWaves = view.findViewById(R.id.ivCommentSoundWaves);
        ivCommentAudioPlay = view.findViewById(R.id.ivCommentAudioPlay);
        tvUserFullName = view.findViewById(R.id.tvCommentUserFullName);
        tvCommentTime = view.findViewById(R.id.tvCommentTime);
        tvCommentAudioTime = view.findViewById(R.id.tvCommentAudioTime);
        tvReply = view.findViewById(R.id.tvCommentReply);
        tvLikeCount = view.findViewById(R.id.tvCommentLikeCount);
        ivLike = view.findViewById(R.id.ivCommentLike);
    }

    public void bindView(CommentItem commentItem){
        this.commentItem = commentItem;

        if (commentItem.getAnonymous()){
            ivCommentProfilePic.setImageResource(R.drawable.ic_anonymous);
            tvUserFullName.setText(R.string.anonymous);
        }else{
            ivCommentUserBadge.setImageResource(Utils.getBadgePic(commentItem.getUserBadge()));
            Glide.with(itemView).load(commentItem.getProfilePic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform()).into(ivCommentProfilePic);

            tvUserFullName.setText(Utils.capitalize(commentItem.getUserFullName()));
        }

        commentSrc = commentItem.getSource();

        String splitDuration = commentSrc.substring(commentSrc.lastIndexOf("_") + 1);

        // Remove duration from source
        commentSrc = commentSrc.replace("_"+splitDuration, "");

        if (commentSrc.substring(0,5).equals("AUDIO")){
            commentSrc = Utils.bucketURL + commentSrc;
        }

        tvCommentTime.setText(" - "+commentItem.getPostedOnHumanReadable());

        tvCommentAudioTime.setText(Utils.milliSecondsToTimer(Long.parseLong(splitDuration)));
        tvLikeCount.setText(String.valueOf(commentItem.getStarCount()));

        if(commentItem.getStarByMe())
            ivLike.setImageResource(R.drawable.ic_fill_star);
        else
            ivLike.setImageResource(R.drawable.ic_hollo_star);

    }

    public void startMyAudio(){

        if(isAudioPlaying) {
            stopMyAudio();
        }else{
            isAudioPlaying = true;
            Log.i("com.azeem.DAudio", "Audio Playing...");

            tvCommentAudioTime.setText(R.string.loading_msg);
            ivCommentAudioPlay.setImageResource(R.drawable.ic_stop);

            String audioSrc = commentSrc;
            /*if (audioSrc.substring(0,5).equals("AUDIO")){
                audioSrc = Utils.bucketURL + audioSrc;
            }*/

            audioPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "zeem"));
            ExtractorMediaSource mediaSource;
            if (audioSrc.substring(0,4).equals("http")) {
                CacheDataSourceFactory cacheDataSourceFactory =
                        new CacheDataSourceFactory(ExoUtil.getCache(context), dataSourceFactory);
                mediaSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(Uri.parse(audioSrc));
            } else {
                mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(audioSrc));
            }

            // Prepare media videoPlayer
            audioPlayer.prepare(mediaSource);
            audioPlayer.setPlayWhenReady(true);

            audioPlayer.addListener(new Player.DefaultEventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    switch (playbackState) {
                        case Player.STATE_READY:
                            audioIsReady();
                            break;
                        case Player.STATE_ENDED:
                            stopMyAudio();
                            break;
                    }

                    super.onPlayerStateChanged(playWhenReady, playbackState);
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    Toast.makeText(context, R.string.error_media_play, Toast.LENGTH_SHORT).show();

                    super.onPlayerError(error);
                }
            });
        }
    }
    private void setupTimer(){
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (audioPlayer != null && isAudioPlaying) {
                    tvCommentAudioTime.post(new Runnable() {
                        @Override
                        public void run() {
                            tvCommentAudioTime.setText(Utils.milliSecondsToTimer(audioPlayer.getDuration()-audioPlayer.getCurrentPosition()));
                        }
                    });
                } else {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 1000);
    }
    private void stopMyAudio(){
        if (isAudioPlaying) {
            isAudioPlaying = false;
            ivCommentAudioPlay.setImageResource(R.drawable.ic_play);
            ivCommentSoundWaves.clearAnimation();

            if (audioPlayer != null) {
                tvCommentAudioTime.setText(Utils.milliSecondsToTimer(audioPlayer.getDuration()));
                audioPlayer.release();
                audioPlayer = null;
            }
        }
    }

    private void audioIsReady() {
        setupTimer();
        Utils.audioWaveAnimation(ivCommentSoundWaves);
    }

}
