package org.octabyte.zeem.Home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.octabyte_zeem.zeem.model.StoryFeed;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<StoryFeed> storyItems;
    private Context mContext;

    private OnStoryItemClickListener onStoryItemClickListener;

    public StoryAdapter(Context mContext, List<StoryFeed> storyItems) {
        this.mContext = mContext;
        this.storyItems = storyItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_story, parent, false);
        StoryViewHolder storyViewHolder = new StoryViewHolder(view);
        setupClickableViews(storyViewHolder);
        return storyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((StoryViewHolder) holder).bindView(storyItems.get(position));
    }

    @Override
    public int getItemCount() {
        return storyItems.size();
    }

    public void setOnStoryItemClickListener(OnStoryItemClickListener onStoryItemClickListener) {
        this.onStoryItemClickListener = onStoryItemClickListener;
    }

    public interface OnStoryItemClickListener {
        void onStoryClick(View v, StoryFeed storyItem);
    }

    private void setupClickableViews(final StoryViewHolder storyViewHolder){
        storyViewHolder.flStoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("StoryAdapter Debug", "running...");
                Log.i("StoryAdapter Debug", String.valueOf(storyViewHolder.storyItem.getStory().getStories().size()));
                onStoryItemClickListener.onStoryClick(v, storyViewHolder.storyItem);
            }
        });
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivsProfilePic, ivsUserBadge;
        private TextView tvsUsername;
        private FrameLayout flStoryLayout;

        private StoryFeed storyItem;

        StoryViewHolder(View itemView) {
            super(itemView);
            initVariable(itemView);
        }

        private void initVariable(View view){
            ivsProfilePic = view.findViewById(R.id.ivsProfilePic);
            ivsUserBadge = view.findViewById(R.id.ivsUserBadge);
            tvsUsername = view.findViewById(R.id.tvsUsername);
            //llStoryLayout = (LinearLayout) view.findViewById(R.id.llStoryLayout);
            flStoryLayout = view.findViewById(R.id.flStoryLayout);
        }

        void bindView(StoryFeed storyItem){
            this.storyItem = storyItem;

            Glide.with(itemView).load(storyItem.getUser().getProfilePic())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform()).into(ivsProfilePic);
            ivsUserBadge.setImageResource(Utils.getBadgePic(storyItem.getUser().getBadge()));
            tvsUsername.setText(Utils.capitalize(storyItem.getUser().getFullName()));
        }

    }

}
