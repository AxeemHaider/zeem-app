package org.octabyte.zeem.Comment;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.CommentItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

class TextViewHolder extends RecyclerView.ViewHolder {

    public ImageView ivCommentProfilePic;
    public ImageView ivLike;
    private ImageView ivCommentUserBadge;
    public TextView tvUserFullName;
    private TextView tvCommentTime;
    public TextView tvReply;
    public TextView tvLikeCount;
    public TextView tvCommentText;
    public LinearLayout llCommentLayout;

    public CommentItem commentItem;

    private Activity mActivity;

    public TextViewHolder(View itemView, Activity activity) {
        super(itemView);
        mActivity = activity;
        initVariable(itemView);
    }

    private void initVariable(View view){
        llCommentLayout = view.findViewById(R.id.llCommentLayout);
        ivCommentProfilePic = view.findViewById(R.id.ivCommentProfilePic);
        ivCommentUserBadge = view.findViewById(R.id.ivCommentUserBadge);
        tvUserFullName = view.findViewById(R.id.tvCommentUserFullName);
        tvCommentTime = view.findViewById(R.id.tvCommentTime);
        tvCommentText = view.findViewById(R.id.tvCommentText);
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
        tvCommentTime.setText(" - "+commentItem.getPostedOnHumanReadable());
        //tvCommentText.setText(commentItem.getSource());
        Utils.applyClickableSpan(mActivity, tvCommentText, commentItem.getSource());
        tvLikeCount.setText(String.valueOf(commentItem.getStarCount()));

        if(commentItem.getStarByMe())
            ivLike.setImageResource(R.drawable.ic_fill_star);
        else
            ivLike.setImageResource(R.drawable.ic_hollo_star);

    }


}
