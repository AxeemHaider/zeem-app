package org.octabyte.zeem.Comment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;


import com.appspot.octabyte_zeem.zeem.model.CommentItem;
import org.octabyte.zeem.API.CommentTask;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.OnSwipeTouchListener;

import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.APP_USER_ID;
import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/1/2017.
 */

class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;
    private static final int VIEW_TYPE_AUDIO = 3;

    private Context mContext;
    private Activity mActivity;
    private int lastAnimatedPosition = -1;

    private boolean animationsLocked = false;

    private OnCommentItemClickListener onCommentItemClickListener;
    public List<CommentItem> commentList;

    private Long mUserId;
    private String postMode;

    public CommentAdapter(Context context, Activity activity, List<CommentItem> commentList, String postMode) {
        this.mContext = context;
        mActivity = activity;
        this.commentList = commentList;
        this.postMode = postMode;

        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType){
            case VIEW_TYPE_TEXT:
                view = LayoutInflater.from(mContext).inflate(R.layout.view_comment_text, parent, false);
                TextViewHolder textViewHolder = new TextViewHolder(view, mActivity);
                setupTextClickable(textViewHolder);
                return textViewHolder;
            case VIEW_TYPE_IMAGE:
                view = LayoutInflater.from(mContext).inflate(R.layout.view_comment_image, parent, false);
                ImageViewHolder imageViewHolder = new ImageViewHolder(view);
                setupImageClickable(imageViewHolder);
                return imageViewHolder;
            case VIEW_TYPE_AUDIO:
                view = LayoutInflater.from(mContext).inflate(R.layout.view_comment_audio, parent, false);
                AudioViewHolder audioViewHolder = new AudioViewHolder(view, mContext);
                setupAudioClickable(audioViewHolder);
                return audioViewHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);

        switch (getItemViewType(position)){
            case VIEW_TYPE_TEXT:
                ( (TextViewHolder) viewHolder ).bindView(commentList.get(position));
                break;
            case VIEW_TYPE_IMAGE:
                ( (ImageViewHolder) viewHolder ).bindView(commentList.get(position));
                break;
            case VIEW_TYPE_AUDIO:
                ( (AudioViewHolder) viewHolder ).bindView(commentList.get(position));
                break;
        }


        if (position == 0){
            onCommentItemClickListener.onTopReached();
        }

    }

    @Override
    public int getItemViewType(int position) {

        switch (commentList.get(position).getType()){
            case "TEXT":
                return VIEW_TYPE_TEXT;
            case "IMAGE":
                return VIEW_TYPE_IMAGE;
            case "AUDIO":
                return VIEW_TYPE_AUDIO;
        }

        return -1;
    }

    public int findItemIndex(CommentItem commentItem){
        return commentList.indexOf(commentItem);
    }

    public void updateCommentView(CommentItem updatedItem, int index){
        commentList.set(index, updatedItem);
    }

    public void removeCommentView(CommentItem commentItem){
        commentList.remove(commentItem);
        notifyDataSetChanged();
    }

    public void addItemsToTop(List<CommentItem> commentItemList){
        commentList.addAll(commentItemList);
        notifyDataSetChanged();
    }
    public void addCommentView(CommentItem newComment){
        commentList.add(0, newComment);
        notifyItemInserted(0);
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(100);
            view.setAlpha(0.f);
            view.animate()
                    .translationY(0).alpha(1.f)
                    .setStartDelay(20 * position)
                    .setInterpolator(new DecelerateInterpolator(2.f))
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationsLocked = true;
                        }
                    })
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    public void setOnCommentItemClickListener(OnCommentItemClickListener onCommentItemClickListener) {
        this.onCommentItemClickListener = onCommentItemClickListener;
    }

    public interface OnCommentItemClickListener {
        void onProfileClick(View v, Long profileId);
        void onTopReached();
        void onReplyClick(String username);
        void onCommentDelete(String commentSafeKey, int position);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTextClickable(final TextViewHolder vh){
        vh.llCommentLayout.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onLongClick(View v) {
                if (vh.commentItem.getUserId().equals(APP_USER_ID)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.delete_comment);
                    builder.setMessage(R.string.delete_comment_msg);
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            onCommentItemClickListener.onCommentDelete(vh.commentItem.getCommentSafeKey(), vh.getAdapterPosition());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
        vh.tvCommentText.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onLongClick(View v) {
                if (vh.commentItem.getUserId().equals(APP_USER_ID)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.delete_comment);
                    builder.setMessage(R.string.delete_comment_msg);
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            onCommentItemClickListener.onCommentDelete(vh.commentItem.getCommentSafeKey(), vh.getAdapterPosition());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
        vh.ivCommentProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onProfileClick(v, vh.commentItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvUserFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onProfileClick(v, vh.commentItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!vh.commentItem.getStarByMe()){ //commentViewHolder.commentItem.isLike()
                    int adapterPosition = vh.getAdapterPosition();
                    commentList.get(adapterPosition).setStarByMe(true);

                    if (!mUserId.equals(vh.commentItem.getUserId())) // Don't like my own comment
                        likeComment(vh.commentItem.getCommentSafeKey(), postMode);

                    commentList.get(adapterPosition).setStarCount(commentList.get(adapterPosition).getStarCount() + 1);
                    vh.ivLike.setImageResource(R.drawable.ic_fill_star);
                    vh.tvLikeCount.setText(String.valueOf(Integer.parseInt(vh.tvLikeCount.getText().toString()) + 1));
                }
            }
        });
        vh.tvReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onReplyClick("@" + vh.commentItem.getUsername());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_reply_msg_in_comment, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupImageClickable(final ImageViewHolder vh){
        vh.llCommentLayout.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onLongClick(View v) {
                if (vh.commentItem.getUserId().equals(APP_USER_ID)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.delete_comment);
                    builder.setMessage(R.string.delete_comment_msg);
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            onCommentItemClickListener.onCommentDelete(vh.commentItem.getCommentSafeKey(), vh.getAdapterPosition());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
        vh.ivCommentProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onProfileClick(v, vh.commentItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvUserFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onProfileClick(v, vh.commentItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!vh.commentItem.getStarByMe()){ //commentViewHolder.commentItem.isLike()
                    int adapterPosition = vh.getAdapterPosition();
                    commentList.get(adapterPosition).setStarByMe(true);

                    if (!mUserId.equals(vh.commentItem.getUserId())) // Don't like my own comment
                        likeComment(vh.commentItem.getCommentSafeKey(), postMode);

                    commentList.get(adapterPosition).setStarCount(commentList.get(adapterPosition).getStarCount() + 1);
                    vh.ivLike.setImageResource(R.drawable.ic_fill_star);
                    vh.tvLikeCount.setText(String.valueOf(Integer.parseInt(vh.tvLikeCount.getText().toString()) + 1));
                }
            }
        });
        vh.tvReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onReplyClick("@" + vh.commentItem.getUsername());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_reply_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupAudioClickable(final AudioViewHolder vh){
        vh.llCommentLayout.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onLongClick(View v) {
                if (vh.commentItem.getUserId().equals(APP_USER_ID)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.delete_comment);
                    builder.setMessage(R.string.delete_comment_msg);
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            onCommentItemClickListener.onCommentDelete(vh.commentItem.getCommentSafeKey(), vh.getAdapterPosition());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
        vh.ivCommentProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onProfileClick(v, vh.commentItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvUserFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onProfileClick(v, vh.commentItem.getUserId());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!vh.commentItem.getStarByMe()){ //commentViewHolder.commentItem.isLike()
                    int adapterPosition = vh.getAdapterPosition();
                    commentList.get(adapterPosition).setStarByMe(true);

                    if (!mUserId.equals(vh.commentItem.getUserId())) // Don't like my own comment
                        likeComment(vh.commentItem.getCommentSafeKey(), postMode);

                    commentList.get(adapterPosition).setStarCount(commentList.get(adapterPosition).getStarCount() + 1);
                    vh.ivLike.setImageResource(R.drawable.ic_fill_star);
                    vh.tvLikeCount.setText(String.valueOf(Integer.parseInt(vh.tvLikeCount.getText().toString()) + 1));
                }
            }
        });
        vh.ivCommentAudioPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    vh.startMyAudio();
            }
        });
        vh.tvReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vh.commentItem.getAnonymous()) {
                    onCommentItemClickListener.onReplyClick("@" + vh.commentItem.getUsername());
                }else{
                    Toast.makeText(mContext, R.string.anonymous_reply_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void likeComment(String commentSafeKey, String mode){
        CommentTask<Void> starComment = new CommentTask<>();
        starComment.setUserId(mUserId);
        starComment.setCommentSafeKey(commentSafeKey);
        starComment.setMode(mode);
        starComment.execute("starComment");

    }

}

