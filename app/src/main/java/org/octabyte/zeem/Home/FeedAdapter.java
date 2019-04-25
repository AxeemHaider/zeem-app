package org.octabyte.zeem.Home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import org.octabyte.zeem.API.PostTask;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.OnSwipeTouchListener;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/1/2017.
 */

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "com.azeem.DFeed";

    private static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";

    public static final int VIEW_TYPE_IMAGE = 1;
    public static final int VIEW_TYPE_AUDIO = 2;
    public static final int VIEW_TYPE_VIDEO = 3;
    public static final int VIEW_TYPE_CARD = 4;
    private static final int VIEW_TYPE_LOADER_IMAGE = 5;
    private static final int VIEW_TYPE_LOADER_AUDIO = 6;
    private static final int VIEW_TYPE_LOADER_VIDEO = 7;
    private static final int VIEW_TYPE_LOADER_CARD = 8;

    public List<FeedItem> feedItems;

    private Context mContext;
    private Activity mActivity;

    private OnFeedItemClickListener onFeedItemClickListener;
    private LinearLayoutManager linearLayoutManager;

    private AudioFeedViewHolder audioTempViewHolder;
    private VideoFeedViewHolder videoTempViewHolder;
    private int lastItem;
    private String itemName;

    public boolean showLoadingView = false;

    private Long myUserId;

    public FeedAdapter(Context context, List<FeedItem> feedItems, LinearLayoutManager linearLayoutManager, Activity activity) {
        Log.i(TAG, "constructor");
        mContext = context;
        mActivity = activity;

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        myUserId = sharedPreferences.getLong("userId", 123L);

        this.linearLayoutManager = linearLayoutManager;
        this.feedItems = feedItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateviewHolder");

        View view;
        LoadingFeedItemView loadingView;

        switch (viewType){
            case VIEW_TYPE_IMAGE:
                view = LayoutInflater.from(mContext).inflate(R.layout.view_feeditem_image, parent, false);
                ImageFeedViewHolder imageFeedViewHolder = new ImageFeedViewHolder(view, mActivity);
                setupClickableForImage(imageFeedViewHolder);
                return imageFeedViewHolder;
            case VIEW_TYPE_AUDIO:
                view = LayoutInflater.from(mContext).inflate(R.layout.view_feeditem_audio, parent, false);
                AudioFeedViewHolder audioFeedViewHolder = new AudioFeedViewHolder(view, mActivity);
                setupClickableForAudio(audioFeedViewHolder);
                return audioFeedViewHolder;
            case VIEW_TYPE_VIDEO:
                view = LayoutInflater.from(mContext).inflate(R.layout.view_feeditem_video, parent, false);
                VideoFeedViewHolder videoFeedViewHolder = new VideoFeedViewHolder(view, mActivity);
                setupClickableForVideo(videoFeedViewHolder);
                return videoFeedViewHolder;
            case VIEW_TYPE_CARD:
                view = LayoutInflater.from(mContext).inflate(R.layout.view_feeditem_card, parent, false);
                CardFeedViewHolder cardFeedViewHolder = new CardFeedViewHolder(view, mActivity);
                setupClickableForCard(cardFeedViewHolder);
                return cardFeedViewHolder;
            case VIEW_TYPE_LOADER_IMAGE:
                loadingView = new LoadingFeedItemView(mContext, viewType);
                loadingView.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                );
                return new LoadingImageFeedViewHolder(loadingView, mContext, mActivity);
            case VIEW_TYPE_LOADER_AUDIO:
                loadingView = new LoadingFeedItemView(mContext, viewType);
                loadingView.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                );
                return new LoadingAudioFeedViewHolder(loadingView, mContext, mActivity);
            case VIEW_TYPE_LOADER_VIDEO:
                loadingView = new LoadingFeedItemView(mContext, viewType);
                loadingView.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                );
                return new LoadingVideoFeedViewHolder(loadingView, mContext, mActivity);
            case VIEW_TYPE_LOADER_CARD:
                loadingView = new LoadingFeedItemView(mContext, viewType);
                loadingView.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                );
                return new LoadingCardFeedViewHolder(loadingView, mContext, mActivity);

        }

        return null;
    }

    public class LoadingImageFeedViewHolder extends ImageFeedViewHolder {

        LoadingFeedItemView loadingFeedItemView;

        LoadingImageFeedViewHolder(LoadingFeedItemView view, Context context, Activity activity) {
            super(view, activity);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(FeedItem feedItem) {
            super.bindView(feedItem);
        }
    }
    public class LoadingAudioFeedViewHolder extends AudioFeedViewHolder {

        LoadingFeedItemView loadingFeedItemView;

        LoadingAudioFeedViewHolder(LoadingFeedItemView view, Context context, Activity activity) {
            super(view, activity);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(FeedItem feedItem) {
            super.bindView(feedItem);
        }
    }
    public class LoadingVideoFeedViewHolder extends VideoFeedViewHolder {

        LoadingFeedItemView loadingFeedItemView;

        LoadingVideoFeedViewHolder(LoadingFeedItemView view, Context context, Activity activity) {
            super(view, activity);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(FeedItem feedItem) {
            super.bindView(feedItem);
        }
    }
    public class LoadingCardFeedViewHolder extends CardFeedViewHolder {

        LoadingFeedItemView loadingFeedItemView;

        LoadingCardFeedViewHolder(LoadingFeedItemView view, Context context, Activity activity) {
            super(view, activity);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(FeedItem feedItem) {
            super.bindView(feedItem);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Log.i(TAG, "onBindViewHolder");

        switch (getItemViewType(position)){
            case VIEW_TYPE_IMAGE:
                ( (ImageFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                break;
            case VIEW_TYPE_AUDIO:
                ( (AudioFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                break;
            case VIEW_TYPE_VIDEO:
                ( (VideoFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                break;
            case VIEW_TYPE_CARD:
                ( (CardFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                break;
            case VIEW_TYPE_LOADER_IMAGE:
                ((ImageFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                bindLoadingImageFeedItem( (LoadingImageFeedViewHolder) viewHolder);
                break;
            case VIEW_TYPE_LOADER_AUDIO:
                ((AudioFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                bindLoadingAudioFeedItem( (LoadingAudioFeedViewHolder) viewHolder);
                break;
            case VIEW_TYPE_LOADER_VIDEO:
                ((VideoFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                bindLoadingVideoFeedItem( (LoadingVideoFeedViewHolder) viewHolder);
                break;
            case VIEW_TYPE_LOADER_CARD:
                ((CardFeedViewHolder) viewHolder).bindView(feedItems.get(position));
                bindLoadingCardFeedItem( (LoadingCardFeedViewHolder) viewHolder);
                break;
        }

        // Check if Recycler view reach at the end
        if (position == getItemCount() -1){
            onFeedItemClickListener.onBottomReached(position);
        }

    }

    private void bindLoadingImageFeedItem(final LoadingImageFeedViewHolder holder){
        holder.loadingFeedItemView.setOnLoadingFinishedListener(new LoadingFeedItemView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                showLoadingView = false;
                notifyDataSetChanged();
            }
        });
        holder.loadingFeedItemView.startLoading();
    }
    private void bindLoadingAudioFeedItem(final LoadingAudioFeedViewHolder holder){
        holder.loadingFeedItemView.setOnLoadingFinishedListener(new LoadingFeedItemView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                showLoadingView = false;
                notifyDataSetChanged();
            }
        });
        holder.loadingFeedItemView.startLoading();
    }
    private void bindLoadingVideoFeedItem(final LoadingVideoFeedViewHolder holder){
        holder.loadingFeedItemView.setOnLoadingFinishedListener(new LoadingFeedItemView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                showLoadingView = false;
                notifyDataSetChanged();
            }
        });
        holder.loadingFeedItemView.startLoading();
    }
    private void bindLoadingCardFeedItem(final LoadingCardFeedViewHolder holder){
        holder.loadingFeedItemView.setOnLoadingFinishedListener(new LoadingFeedItemView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                showLoadingView = false;
                notifyDataSetChanged();
                //notifyItemChanged(0);
            }
        });
        holder.loadingFeedItemView.startLoading();
    }

    @Override
    public int getItemViewType(int position) {
        //Log.i(TAG, "getItemViewType");
        if (showLoadingView && position == 0) {
            switch (feedItems.get(position).getType()){
                case "IMAGE":
                    return VIEW_TYPE_LOADER_IMAGE;
                case "AUDIO":
                case "TALKING_PHOTO":
                    return VIEW_TYPE_LOADER_AUDIO;
                case "VIDEO":
                case "GIF":
                    return VIEW_TYPE_LOADER_VIDEO;
                case "CARD":
                    return VIEW_TYPE_LOADER_CARD;
            }
        } else {
            switch (feedItems.get(position).getType()){
                case "IMAGE":
                    return VIEW_TYPE_IMAGE;
                case "AUDIO":
                case "TALKING_PHOTO":
                    return VIEW_TYPE_AUDIO;
                case "VIDEO":
                case "GIF":
                    return VIEW_TYPE_VIDEO;
                case "CARD":
                    return VIEW_TYPE_CARD;
            }
        }

        return -1;
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.i(TAG,"onViewDetachedFromWindow" + lastItem + " : " +linearLayoutManager.findLastVisibleItemPosition());
        if (itemName != null) {
            if(lastItem != linearLayoutManager.findLastVisibleItemPosition()) {
                switch (itemName){
                    case "VIDEO":
                        if (videoTempViewHolder != null) videoTempViewHolder.stopMyVideo();
                        break;
                    case "AUDIO":
                        if (audioTempViewHolder != null) audioTempViewHolder.stopMyAudio();
                        break;
                }

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickableForImage(final ImageFeedViewHolder vh){
        vh.tvTotalComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, vh.feedItem.getPostSafeKey(), vh.feedItem.getFeedSafeKey(), vh.feedItem.getUserId(), vh.feedItem.getPostId(),
                        vh.feedItem.getMode(), vh.feedItem.getTaggedMe(), vh.feedItem.getTaggedApproved(), vh.getAdapterPosition());//vh.feedItem.isTagApproved()
            }
        });
        vh.tsLikesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onLikesClick(v, vh.feedItem.getPostSafeKey());
            }
        });
        vh.tvFeedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vh.feedItem.getPostListId() != null) {
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostListId(), true);
                }else{
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostId(), false); //vh.feedItem.getTagId()
                }
            }
        });
        vh.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!vh.feedItem.getStarByMe()) {
                    int adapterPosition = vh.getAdapterPosition();
                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);
                    notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Not like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    vh.feedItem.setStarByMe(true);
                }

            }
        });
        vh.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvUserFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });

        vh.ivFeedCenter.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onDoubleClick(View v) {
                super.onDoubleClick(v);
                int adapterPosition = vh.getAdapterPosition();
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);

                if (!vh.feedItem.getStarByMe()) {
                    vh.feedItem.setStarByMe(true);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Don't like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickableForAudio(final AudioFeedViewHolder vh){
        vh.tvTotalComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, vh.feedItem.getPostSafeKey(), vh.feedItem.getFeedSafeKey(), vh.feedItem.getUserId(), vh.feedItem.getPostId(),
                        vh.feedItem.getMode(), vh.feedItem.getTaggedMe(), vh.feedItem.getTaggedApproved(), vh.getAdapterPosition());//vh.feedItem.isTagApproved()
            }
        });
        vh.tsLikesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onLikesClick(v, vh.feedItem.getPostSafeKey());
            }
        });
        vh.tvFeedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vh.feedItem.getPostListId() != null) {
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostListId(), true);
                }else{
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostId(), false); //vh.feedItem.getTagId()
                }
            }
        });
        vh.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!vh.feedItem.getStarByMe()) {
                    int adapterPosition = vh.getAdapterPosition();
                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);
                    notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Not like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    vh.feedItem.setStarByMe(true);
                }
                /*if (mContext instanceof HomeActivity) {
                    ((HomeActivity) mContext).showLikedSnackbar();
                }*/

            }
        });
        vh.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvUserFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });

        vh.ivFeedCenter.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onClick(View v) {
                super.onClick(v);
                Log.i(TAG+"C", "onClick");
                itemName = "AUDIO";
                vh.startMyAudio();
                audioTempViewHolder = vh;
                lastItem = linearLayoutManager.findLastVisibleItemPosition();
            }

            @Override
            public void onDoubleClick(View v) {
                super.onDoubleClick(v);
                int adapterPosition = vh.getAdapterPosition();
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);

                if (!vh.feedItem.getStarByMe()) {
                    vh.feedItem.setStarByMe(true);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Don't like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickableForVideo(final VideoFeedViewHolder vh){
        vh.tvTotalComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, vh.feedItem.getPostSafeKey(), vh.feedItem.getFeedSafeKey(), vh.feedItem.getUserId(), vh.feedItem.getPostId(),
                        vh.feedItem.getMode(), vh.feedItem.getTaggedMe(), vh.feedItem.getTaggedApproved(), vh.getAdapterPosition());//vh.feedItem.isTagApproved()
            }
        });
        vh.tsLikesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onLikesClick(v, vh.feedItem.getPostSafeKey());
            }
        });
        vh.tvFeedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vh.feedItem.getPostListId() != null) {
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostListId(), true);
                }else{
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostId(), false); //vh.feedItem.getTagId()
                }
            }
        });
        vh.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!vh.feedItem.getStarByMe()) {
                    int adapterPosition = vh.getAdapterPosition();
                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);
                    notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Not like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    vh.feedItem.setStarByMe(true);
                }
                /*if (mContext instanceof HomeActivity) {
                    ((HomeActivity) mContext).showLikedSnackbar();
                }*/

            }
        });
        vh.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvUserFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });

        vh.ivFeedCenter.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onClick(View v) {
                super.onClick(v);
                itemName = "VIDEO";
                Log.i("FirstTesting", "ivFeedCenter click");
                vh.startMyVideo();
                videoTempViewHolder = vh;
                lastItem = linearLayoutManager.findLastVisibleItemPosition();
            }

            @Override
            public void onDoubleClick(View v) {
                super.onDoubleClick(v);
                int adapterPosition = vh.getAdapterPosition();
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);

                if (!vh.feedItem.getStarByMe()) {
                    vh.feedItem.setStarByMe(true);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Don't like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);
                }
            }
        });

        vh.svFeedVideo.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onClick(View v) {
                super.onClick(v);
                itemName = "VIDEO";
                Log.i("FirstTesting", "ivFeedCenter click");
                vh.startMyVideo();
                videoTempViewHolder = vh;
                lastItem = linearLayoutManager.findLastVisibleItemPosition();
            }

            @Override
            public void onDoubleClick(View v) {
                super.onDoubleClick(v);
                int adapterPosition = vh.getAdapterPosition();

                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);

                if (!vh.feedItem.getStarByMe()) {
                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Not like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    vh.feedItem.setStarByMe(true);

                }

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickableForCard(final CardFeedViewHolder vh){
        vh.tvTotalComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, vh.feedItem.getPostId(), vh.feedItem.getPostSafeKey(), vh.feedItem.getMode());
            }
        });
        vh.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, vh.feedItem.getPostSafeKey(), vh.feedItem.getFeedSafeKey(), vh.feedItem.getUserId(), vh.feedItem.getPostId(),
                        vh.feedItem.getMode(), vh.feedItem.getTaggedMe(), vh.feedItem.getTaggedApproved(), vh.getAdapterPosition());//vh.feedItem.isTagApproved()
            }
        });
        vh.tsLikesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onLikesClick(v, vh.feedItem.getPostSafeKey());
            }
        });
        vh.tvFeedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vh.feedItem.getPostListId() != null) {
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostListId(), true);
                }else{
                    onFeedItemClickListener.onListClick(v, vh.feedItem.getPostId(), false); //vh.feedItem.getTagId()
                }
            }
        });
        vh.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!vh.feedItem.getStarByMe()) {
                    int adapterPosition = vh.getAdapterPosition();
                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);
                    notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Not like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    vh.feedItem.setStarByMe(true);
                }
                /*if (mContext instanceof HomeActivity) {
                    ((HomeActivity) mContext).showLikedSnackbar();
                }*/

            }
        });
        vh.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvUserFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivFeedCommentPic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(0).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(0).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(1).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(1).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvCommentUserFullName3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.feedItem.getCommentItems().get(2).getAnonymous()) {
                    onFeedItemClickListener.onProfileClick(v, vh.feedItem.getCommentItems().get(2).getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });

        vh.tvFeedCard.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onDoubleClick(View v) {
                super.onDoubleClick(v);

                int adapterPosition = vh.getAdapterPosition();
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);

                if (!vh.feedItem.getStarByMe()){
                    int likesCount = vh.feedItem.getStarCount(); //++
                    vh.feedItem.setStarCount(++likesCount);

                    if (!myUserId.equals(vh.feedItem.getUserId())) // Not like my own post
                        likedPost(vh.feedItem.getPostSafeKey());

                    vh.feedItem.setStarByMe(true);
                }
                /*
                if (mContext instanceof HomeActivity) {
                    ((HomeActivity) mContext).showLikedSnackbar();
                }*/
            }
        });
    }

    @Override
    public int getItemCount() {
        //Log.i(TAG, "getItemCount");
        return feedItems.size();
    }

    public void updateItemsAtTop(List<FeedItem> newFeedItems){
        feedItems.addAll(0, newFeedItems);
        notifyDataSetChanged();
    }

    public void updateItemsAtEnd(List<FeedItem> newFeedItems){
        feedItems.addAll(newFeedItems);
        notifyDataSetChanged();
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        Log.i(TAG, "setOnFeedItemClickListener");
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public void showLoadingView(FeedItem newItem) {
        feedItems.add(0, newItem);
        showLoadingView = true;
        //notifyItemChanged(0);
        //notifyItemInserted(0);
        //updateItems(true);

        notifyDataSetChanged();
    }

    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, Long postId, String postSafeKey, String postMode);

        void onMoreClick(View v, String postSafeKey, String feedSafeKey, Long userId, Long postId, String postMode, Boolean tagged, Boolean tagApproved, int adapterPosition);

        void onProfileClick(View v, Long profileId);

        void onLikesClick(View v, String postSafeKey);

        void onListClick(View v, Long listId, Boolean list);

        void onBottomReached(int position); // Used to check if recycler view reach at the end
    }

    private void likedPost(String postSafeKey){
        PostTask<Void> postStar = new PostTask<>(myUserId);
        postStar.setPostSafeKey(postSafeKey);
        postStar.execute("postStar");
    }

}
