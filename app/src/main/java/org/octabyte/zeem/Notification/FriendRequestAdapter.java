package org.octabyte.zeem.Notification;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.UserFriendRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FriendRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<UserFriendRequest> friendRequests;
    private OnFriendRequestClick onFriendRequestClick;

    public FriendRequestAdapter(Context mContext, List<UserFriendRequest> friendRequests) {
        this.mContext = mContext;
        this.friendRequests = friendRequests;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_friend_request, parent, false);
        FriendRequestViewHolder friendRequestViewHolder = new FriendRequestViewHolder(view);
        setupClickableView(friendRequestViewHolder);

        return friendRequestViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ( (FriendRequestViewHolder) holder).bindView(friendRequests.get(position));
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public void removeItem(int position){
        friendRequests.remove(position);
        notifyDataSetChanged();
    }

    private void setupClickableView(final FriendRequestViewHolder vh){
        vh.requestAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFriendRequestClick.onFriendRequestClick(vh.request.getRequestId(), vh.request.getFriendId(), vh.request.getFriendPhone(), vh.name, vh.getAdapterPosition());
            }
        });
    }

    private static class FriendRequestViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivUserPic, ivUserBadge;
        private TextView tvRequestText;
        private Button requestAction;
        private boolean firstMatch = true;
        String name;

        private UserFriendRequest request;

        FriendRequestViewHolder(View itemView) {
            super(itemView);
            firstMatch = true;
            initVariable(itemView);
        }

        private void initVariable(View view){
            ivUserPic = view.findViewById(R.id.ivNotificationProfilePic);
            ivUserBadge = view.findViewById(R.id.ivNotificationUserBadge);
            tvRequestText = view.findViewById(R.id.tvNotificationText);
            requestAction = view.findViewById(R.id.btnFriendAction);
        }

        private void bindView(UserFriendRequest friendRequest){
            request = friendRequest;

            Glide.with(itemView).load(friendRequest.getFriendPic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserPic);
            ivUserBadge.setImageResource(Utils.getBadgePic(friendRequest.getFriendBadge()));
            applyRequestMessage(tvRequestText, friendRequest.getRequestMessage());
        }

        private void applyRequestMessage(TextView textView,final String str){
            SpannableStringBuilder ss = new SpannableStringBuilder(str);
            Pattern userNamePattern = Pattern.compile("\\[(.*?)\\]");
            Matcher userNameMatcher = userNamePattern.matcher(ss);
            int delta = 0;
            while (userNameMatcher.find()) {
                final int start = userNameMatcher.start() - delta;
                final int end = userNameMatcher.end() - delta;

                if (firstMatch){
                    name = str.substring(start+1,end-1);
                    firstMatch = false;
                }

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

    public void setOnFriendRequestClick(OnFriendRequestClick onFriendRequestClick) {
        this.onFriendRequestClick = onFriendRequestClick;
    }

    public interface OnFriendRequestClick{
        void onFriendRequestClick(Long requestId, Long friendId, Long phone, String name, int adapterPosition);
    }



}
