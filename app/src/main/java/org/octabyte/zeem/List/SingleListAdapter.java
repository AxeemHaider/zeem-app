package org.octabyte.zeem.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import org.octabyte.zeem.Utils.OnSwipeTouchListener;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

/**
 * Created by Azeem on 9/8/2017.
 */

public class SingleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DSingleListAdapter";

    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;

    private Context mContext;

    public List<User> membersList;

    private OnUserClickListener onUserClickListener;

    public SingleListAdapter(Context c, List<User> membersList){
        this.mContext = c;
        this.membersList = membersList;
        Log.i(TAG, String.valueOf(getItemCount()));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_list, parent, false);
        Log.i(TAG, "on create view holder");
        UserViewHolder userViewHolder = new UserViewHolder(view);
        setupClickableViews(userViewHolder);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);
        ((UserViewHolder) holder).bindView(membersList.get(position));
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    public void updateList(List<User> userList){
        this.membersList = userList;
        notifyDataSetChanged();
    }

    public void setOnUserClickListener(OnUserClickListener onUserClickListener){
        this.onUserClickListener = onUserClickListener;
    }

    public interface OnUserClickListener{
        void onUserClick(View v, Long userId);
        void onUserDelete(Long userId, int position);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout llUserLayout;
        private ImageView ivProfilePic, ivUserBadge;
        private TextView tvFullName;
        private TextView tvUsername;

        public User member;

        UserViewHolder(View itemView) {
            super(itemView);
            initVariable(itemView);
        }

        private void initVariable(View view){
            llUserLayout = view.findViewById(R.id.llUserLayout);
            ivProfilePic = view.findViewById(R.id.ivUserPic);
            ivUserBadge = view.findViewById(R.id.ivUserBadge);
            tvFullName = view.findViewById(R.id.tvUserFullName);
            tvUsername = view.findViewById(R.id.tvUsername);
        }

        void bindView(User member){
            this.member = member;

            String profilePicSrc = member.getProfilePic();

            if (!profilePicSrc.substring(0,4).equals("http"))
                profilePicSrc = Utils.bucketURL + member.getProfilePic();

            Glide.with(itemView).load(profilePicSrc)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform()).into(ivProfilePic);

            ivUserBadge.setImageResource(Utils.getBadgePic(member.getBadge()));

            tvFullName.setText(Utils.capitalize(member.getFullName()));
            tvUsername.setText("@"+member.getUsername());
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    protected void setupClickableViews(final UserViewHolder userViewHolder){
        userViewHolder.llUserLayout.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onClick(View v) {
                onUserClickListener.onUserClick(v, userViewHolder.member.getUserId());
            }

            @Override
            public void onSwipeLeft() {
                onUserClickListener.onUserDelete(userViewHolder.member.getUserId(), userViewHolder.getAdapterPosition());
            }
        });
    }

    //######### Only For Animation use ########

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


    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

}
