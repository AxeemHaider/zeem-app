package org.octabyte.zeem.Comment;

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

class ImageViewHolder extends RecyclerView.ViewHolder {

    public ImageView ivCommentProfilePic;
    public ImageView ivLike;
    private ImageView ivImageComment;
    private ImageView ivCommentUserBadge;
    public TextView tvUserFullName;
    private TextView tvCommentTime;
    public TextView tvReply;
    public TextView tvLikeCount;
    public LinearLayout llCommentLayout;

    public CommentItem commentItem;

    public ImageViewHolder(View itemView) {
        super(itemView);
        initVariable(itemView);
    }

    private void initVariable(View view){
        llCommentLayout = view.findViewById(R.id.llCommentLayout);
        ivCommentProfilePic = view.findViewById(R.id.ivCommentProfilePic);
        ivCommentUserBadge = view.findViewById(R.id.ivCommentUserBadge);
        tvUserFullName = view.findViewById(R.id.tvCommentUserFullName);
        tvCommentTime = view.findViewById(R.id.tvCommentTime);
        tvReply = view.findViewById(R.id.tvCommentReply);
        tvLikeCount = view.findViewById(R.id.tvCommentLikeCount);
        ivLike = view.findViewById(R.id.ivCommentLike);
        ivImageComment = view.findViewById(R.id.ivImageComment);
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

        String commentSrc = commentItem.getSource();
        if (commentSrc.substring(0,7).equals("COMMENT")){
            commentSrc = Utils.bucketURL + commentSrc;
        }

        Glide.with(itemView).load(commentSrc)
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_image))
                .into(ivImageComment);


        tvCommentTime.setText(" - "+commentItem.getPostedOnHumanReadable());
        tvLikeCount.setText(String.valueOf(commentItem.getStarCount()));

        if(commentItem.getStarByMe())
            ivLike.setImageResource(R.drawable.ic_fill_star);
        else
            ivLike.setImageResource(R.drawable.ic_hollo_star);

    }

}
