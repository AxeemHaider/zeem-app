package org.octabyte.zeem.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

/**
 * Created by Azeem on 8/19/2017.
 */

class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<User> friendsList;
    private OnFriendListClickListener onFriendListClickListener;

    public FriendAdapter(Context context, List<User> friendsList){
        this.mContext = context;
        this.friendsList = friendsList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_friend_list, parent, false);
        FriendViewHolder friendViewHolder = new FriendViewHolder(view);
        setupClickableViews(friendViewHolder);
        return friendViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((FriendViewHolder) holder).bindView(friendsList.get(position));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public interface OnFriendListClickListener{
        void onFriendSelect(Long userId);
        void onFriendDeSelect(Long userId);
    }

    public void setOnFriendListClickListener(OnFriendListClickListener onFriendListClickListener){
        this.onFriendListClickListener = onFriendListClickListener;
    }

    private void setupClickableViews(final FriendViewHolder friendViewHolder){
        friendViewHolder.selectFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(friendViewHolder.selectFriend.isChecked()) {
                    onFriendListClickListener.onFriendSelect(friendViewHolder.friend.getUserId());
                }else{
                    onFriendListClickListener.onFriendDeSelect(friendViewHolder.friend.getUserId());
                }
            }
        });
    }

    public void searchFilter(List<User> list){
        friendsList = list;
        notifyDataSetChanged();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder{

        ImageView ivUserPic;
        ImageView ivUserBadge;
        TextView tvUserFullName;
        TextView tvUsername;
        CheckBox selectFriend;
        User friend;

        FriendViewHolder(View itemView) {
            super(itemView);
            initVariable(itemView);
        }
        private void initVariable(View v){
            ivUserPic = v.findViewById(R.id.ivUserPic);
            ivUserBadge = v.findViewById(R.id.ivUserBadge);
            tvUserFullName = v.findViewById(R.id.tvUserFullName);
            tvUsername = v.findViewById(R.id.tvUsername);
            selectFriend = v.findViewById(R.id.selectFriend);
        }

        void bindView(User friend){
            this.friend = friend;

            Glide.with(itemView).load(friend.getProfilePic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserPic);
            ivUserBadge.setImageResource(Utils.getBadgePic(friend.getBadge()));

            tvUserFullName.setText(Utils.capitalize(friend.getFullName()));
            tvUsername.setText("@"+friend.getUsername());
        }
    }
}
