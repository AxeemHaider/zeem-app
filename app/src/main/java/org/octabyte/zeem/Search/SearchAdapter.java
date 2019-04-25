package org.octabyte.zeem.Search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by Azeem on 8/29/2017.
 */

class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<User> searchList;
    private OnSearchListClickListener onSearchListClickListener;

    public SearchAdapter(Context context, List<User> searchList){
        this.mContext = context;
        this.searchList = searchList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_list, parent, false);
        SearchViewHolder searchViewHolder = new SearchViewHolder(view);
        setupClickableViews(searchViewHolder);
        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SearchViewHolder) holder).bindView(searchList.get(position));
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public void updateList(List<User> searchList){
        this.searchList = searchList;
        notifyDataSetChanged();
    }

    public interface OnSearchListClickListener{
        void onProfileClick(View v, Long userId);
        void onPreformAction(Boolean follow, Long userId);
    }

    public void setOnSearchListClickListener(OnSearchListClickListener onSearchListClickListener){
        this.onSearchListClickListener = onSearchListClickListener;
    }

    private void setupClickableViews(final SearchViewHolder searchViewHolder){
        searchViewHolder.llUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchListClickListener.onProfileClick(v, searchViewHolder.result.getUserId());
            }
        });
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout llUserLayout;
        ImageView ivUserPic;
        ImageView ivUserBadge;
        private TextView tvUserFullName, tvUsername;
        User result;

        SearchViewHolder(View itemView) {
            super(itemView);
            initVariable(itemView);
        }
        private void initVariable(View v){
            llUserLayout = v.findViewById(R.id.llUserLayout);
            ivUserPic = v.findViewById(R.id.ivUserPic);
            ivUserBadge = v.findViewById(R.id.ivUserBadge);
            tvUserFullName = v.findViewById(R.id.tvUserFullName);
            tvUsername = v.findViewById(R.id.tvUsername);
        }

        void bindView(User result){
            this.result = result;

            Glide.with(itemView).load(Utils.bucketURL + result.getProfilePic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserPic);
            ivUserBadge.setImageResource(Utils.getBadgePic(result.getBadge()));

            tvUserFullName.setText(Utils.capitalize(result.getFullName()));
            tvUsername.setText("@"+result.getUsername());

        }
    }
}
