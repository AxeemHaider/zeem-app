package org.octabyte.zeem.Like;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

/**
 * Created by Azeem on 8/1/2017.
 */

public class LikeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int lastAnimatedPosition = -1;

    private boolean animationsLocked = false;

    private OnLikeItemClickListener onLikeItemClickListener;
    private List<User> likeList;

    public LikeAdapter(Context context, List<User> likeList) {
        this.context = context;
        this.likeList = likeList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.view_user_list, parent, false);
        LikeViewHolder likeViewHolder = new LikeViewHolder(view);
        setupClickableViews(likeViewHolder);
        return likeViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        ((LikeViewHolder) viewHolder).bindView(likeList.get(position));
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(100);
            view.setAlpha(0.f);
            boolean delayEnterAnimation = true;
            view.animate()
                    .translationY(0).alpha(1.f)
                    .setStartDelay(delayEnterAnimation ? 20 * (position) : 0)
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
        return likeList.size();
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    public void setOnLikeItemClickListener(OnLikeItemClickListener onLikeItemClickListener) {
        this.onLikeItemClickListener = onLikeItemClickListener;
    }

    public interface OnLikeItemClickListener {
        void onProfileClick(View v, Long profileId);
    }

    private void setupClickableViews(final LikeViewHolder likeViewHolder){
        likeViewHolder.llUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeItemClickListener.onProfileClick(v, likeViewHolder.likeItem.getUserId());
            }
        });
    }

    static class LikeViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfilePic, ivUserBadge;
        private TextView tvFullName;
        private TextView tvUsername;
        LinearLayout llUserLayout;

        User likeItem;

        LikeViewHolder(View view) {
            super(view);
            initVariable(view);
        }
        private void initVariable(View view){
            ivProfilePic = view.findViewById(R.id.ivUserPic);
            ivUserBadge = view.findViewById(R.id.ivUserBadge);
            tvFullName = view.findViewById(R.id.tvUserFullName);
            tvUsername = view.findViewById(R.id.tvUsername);
            llUserLayout = view.findViewById(R.id.llUserLayout);

        }

        void bindView(User likeItem){
            this.likeItem = likeItem;

            Glide.with(itemView).load(Utils.bucketURL + likeItem.getProfilePic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfilePic);
            ivUserBadge.setImageResource(Utils.getBadgePic(likeItem.getBadge()));

            tvFullName.setText(Utils.capitalize(likeItem.getFullName()));
            tvUsername.setText("@"+likeItem.getUsername());
        }
    }
}
