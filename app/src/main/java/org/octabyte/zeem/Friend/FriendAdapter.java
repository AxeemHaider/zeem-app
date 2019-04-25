package org.octabyte.zeem.Friend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.User;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * Created by Azeem on 8/27/2017.
 */

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<User> friendsList;
    private OnFriendListClickListener onFriendListClickListener;
    private static RelationType RELATION_TYPE;
    public enum RelationType{
        FRIEND,
        FOLLOWER,
        FOLLOWING,
        BLOCK_FRIEND,
        BLOCK_FOLLOWER,
        UNFOLLOW
    }

    public FriendAdapter(Context context, List<User> friendsList, RelationType RELATION_TYPE){
        this.mContext = context;
        this.friendsList = friendsList;
        FriendAdapter.RELATION_TYPE = RELATION_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_friend, parent, false);
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

    public void searchFilter(List<User> list){
        friendsList = list;
        notifyDataSetChanged();
    }

    public void setNewList(List<User> newList){
        friendsList = newList;
        notifyDataSetChanged();
    }

    public interface OnFriendListClickListener{
        void onBlock(Long friendId);
        void onUnBlock(Long friendId);
        void onProfileClick(View v, Long userId);
    }

    public void setOnFriendListClickListener(OnFriendListClickListener onFriendListClickListener){
        this.onFriendListClickListener = onFriendListClickListener;
    }

    private void setupClickableViews(final FriendViewHolder vh){
        vh.llUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFriendListClickListener.onProfileClick(v, vh.friend.getUserId());
            }
        });

        vh.btnFriendAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (vh.btnFriendAction.getText().toString().equals(mContext.getResources().getString(R.string.un_block))) {
                    onFriendListClickListener.onUnBlock(vh.friend.getUserId());
                    vh.btnFriendAction.setText(R.string.block);
                }else {
                    onFriendListClickListener.onBlock(vh.friend.getUserId());
                    vh.btnFriendAction.setText(R.string.un_block);
                }
            }
        });
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout llUserLayout;
        ImageView ivUserPic;
        ImageView ivUserBadge;
        TextView tvUserFullName;
        TextView tvUserName;
        Button btnFriendAction;
        User friend;

        FriendViewHolder(View itemView) {
            super(itemView);
            initVariable(itemView);
        }
        private void initVariable(View v){
            llUserLayout = v.findViewById(R.id.llUserLayout);
            ivUserPic = v.findViewById(R.id.ivUserPic);
            ivUserBadge = v.findViewById(R.id.ivUserBadge);
            tvUserFullName = v.findViewById(R.id.tvUserFullName);
            tvUserName = v.findViewById(R.id.tvUsername);
            btnFriendAction = v.findViewById(R.id.btnFriendAction);
        }

        void bindView(User friend){
            this.friend = friend;

            Glide.with(itemView).load(Utils.bucketURL + friend.getProfilePic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform()).into(ivUserPic);
            ivUserBadge.setImageResource(Utils.getBadgePic(friend.getBadge()));

            tvUserFullName.setText(Utils.capitalize(friend.getFullName()));
            tvUserName.setText("@"+friend.getUsername());
            if (RELATION_TYPE != RelationType.BLOCK_FRIEND){
                btnFriendAction.setVisibility(View.GONE);
            }


        }
    }
}
