package org.octabyte.zeem.Notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.NotificationItem;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Azeem on 8/30/2017.
 */

class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private int lastAnimatedPosition = -1;

    private boolean animationsLocked = false;
    private boolean delayEnterAnimation = true;

    private OnNotificationClickListener onNotificationClickListener;
    private List<NotificationItem> notificationList;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.mContext = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_notification, parent, false);
        NotificationViewHolder notificationViewHolder = new NotificationViewHolder(view);
        setupClickableViews(notificationViewHolder);
        return notificationViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        ((NotificationViewHolder) viewHolder).bindView(notificationList.get(position));

        if (position == getItemCount() - 1){
            onNotificationClickListener.onBottomReached(position);
        }

    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(100);
            view.setAlpha(0.f);
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
        return notificationList.size();
    }

    public List<NotificationItem> getNotificationList(){
        return notificationList;
    }

    public void updateItemsAtEnd(List<NotificationItem> notificationItemList){
        notificationList.addAll(notificationItemList);
        notifyDataSetChanged();
    }

    public long getLastNotificationId(){
        return notificationList.get(getItemCount() - 1).getNotificationId();
    }

    public void itemRemoved(int position){
        notificationList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItems() {
        //itemsCount = 10;
        notifyDataSetChanged();
    }

    public void addItem() {
        /*itemsCount++;
        notifyItemInserted(itemsCount - 1);*/
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    public void setDelayEnterAnimation(boolean delayEnterAnimation) {
        this.delayEnterAnimation = delayEnterAnimation;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener onNotificationClickListener) {
        this.onNotificationClickListener = onNotificationClickListener;
    }

    public interface OnNotificationClickListener {
        void onProfileClick(View v, Long profileId);
        void onStatusClick(View v, String postSafeKey);
        void onBottomReached(int position);
    }

    private void setupClickableViews(final NotificationViewHolder vh){
        vh.ivUserPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.anonymous) {
                    onNotificationClickListener.onProfileClick(v, vh.notificationItem.getUserId());
                } else {
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.tvNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vh.anonymous) {
                    onNotificationClickListener.onProfileClick(v, vh.notificationItem.getUserId());
                } else {
                    Toast.makeText(mContext, R.string.anonymous_profile_msg_in_comment, Toast.LENGTH_SHORT).show();
                }
            }
        });
        vh.ivPostPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNotificationClickListener.onStatusClick(view, vh.notificationItem.getPostSafeKey());
            }
        });
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView ivUserPic, ivUserBadge;
        TextView tvNotification;
        ImageView ivPostPic;
        private boolean anonymous = false;

        NotificationItem notificationItem;

        NotificationViewHolder(View view) {
            super(view);
            initVariable(view);
        }

        private void initVariable(View view){
            ivUserPic = view.findViewById(R.id.ivNotificationProfilePic);
            ivUserBadge = view.findViewById(R.id.ivNotificationUserBadge);
            tvNotification = view.findViewById(R.id.tvNotificationText);
            ivPostPic = view.findViewById(R.id.ivNotificationPostPic);
        }

        void bindView(NotificationItem notificationItem){
            this.notificationItem = notificationItem;

            if (notificationItem.getUserPic().equals("Anonymous")){
                anonymous = true;
            }


            if (!anonymous) {
                Glide.with(itemView).load(notificationItem.getUserPic())
                        .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivUserPic);
                ivUserBadge.setImageResource(Utils.getBadgePic(notificationItem.getUserBadge()));
            }else{
                ivUserPic.setImageResource(R.drawable.ic_anonymous);
            }

            tvNotification.setText(notificationItem.getNotification());

            if (notificationItem.getType().equals("NEW_FRIEND")
                    || notificationItem.getType().equals("FRIEND_REQUEST")
                    || notificationItem.getType().equals("COMMENT_LIKE")
                    || notificationItem.getType().equals("FOLLOWER")){
                ivPostPic.setVisibility(View.GONE);
            }else{

                if (notificationItem.getPostPic() != null) {
                    if (notificationItem.getPostPic().equals("AUDIO")){
                        ivPostPic.setImageResource(R.drawable.placeholder_audio);
                    }else if (notificationItem.getPostPic().equals("CARD")){
                        ivPostPic.setImageResource(R.drawable.placeholder_card);
                    }else {
                        Glide.with(itemView).load(notificationItem.getPostPic()).into(ivPostPic);
                    }
                }
            }

            applyUserName(tvNotification, notificationItem.getNotification());
        }

        private void applyUserName(TextView textView,final String str){
            SpannableStringBuilder ss = new SpannableStringBuilder(str);
            Pattern userNamePattern = Pattern.compile("\\[(.*?)\\]");
            Matcher userNameMatcher = userNamePattern.matcher(ss);
            int delta = 0;
            while (userNameMatcher.find()) {
                final int start = userNameMatcher.start() - delta;
                final int end = userNameMatcher.end() - delta;
                //final String clickString = str.substring(start,end);
                //ss.replace(start, end, Utils.capitalize(clickString));
                ss.setSpan(new ClickableSpan() {
                    final String clickString = str.substring(start,end);
                    @Override
                    public void onClick(View widget) {
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(Color.parseColor("#000000"));
                        ds.setTypeface(Typeface.DEFAULT_BOLD);
                        ds.setUnderlineText(false);
                    }
                }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ss.delete(end - 1, end);
                ss.delete(start, start + 1);
                delta += 2;

            }
            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}