package org.octabyte.zeem.Profile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.appspot.octabyte_zeem.zeem.model.FeedItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

/**
 * Created by Azeem on 8/1/2017.
 */

public class PostGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "com.azeem.DProfile";

    private static final int PHOTO_ANIMATION_DELAY = 600;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    private int GRID_LAYOUT_MANAGER = 0;
    private OnGridImageClickListener onGridImageClickListener;

    private final Context context;
    private final int cellSize;

    private int layoutType;

    private List<FeedItem> postItems;

    private boolean lockedAnimations = false;

    public PostGridAdapter(Context context, List<FeedItem> feedItems) {
        this.context = context;
        this.postItems = feedItems;
        this.cellSize = Utils.getScreenWidth(context) / 3;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_grid_photo, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;
            layoutParams.setFullSpan(false);
            view.setLayoutParams(layoutParams);
            GridViewHolder gridViewHolder = new GridViewHolder(view);
            setupGridClickable(gridViewHolder);
            return gridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((GridViewHolder) holder).bindView(postItems.get(position));
    }

    /*private void bindPhoto(final GridViewHolder holder, int position) {
        Picasso.with(context)
                .load(photos.get(position))
                .resize(cellSize, cellSize)
                .centerCrop()
                .into(holder.ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess");
                        animatePhoto(holder);
                    }

                    @Override
                    public void onError() {
                        Log.i(TAG, "onError");
                    }
                });
        if (lastAnimatedItem < position) lastAnimatedItem = position;
    }*/

    private void animatePhoto(GridViewHolder viewHolder) {
        if (!lockedAnimations) {
            int lastAnimatedItem = -1;
            if (lastAnimatedItem == viewHolder.getPosition()) {
                setLockedAnimations(true);
            }

            long animationDelay = PHOTO_ANIMATION_DELAY + viewHolder.getPosition() * 30;

            viewHolder.flRoot.setScaleY(0);
            viewHolder.flRoot.setScaleX(0);

            viewHolder.flRoot.animate()
                    .scaleY(1)
                    .scaleX(1)
                    .setDuration(200)
                    .setInterpolator(INTERPOLATOR)
                    .setStartDelay(animationDelay)
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return postItems.size();
    }

    private void setupGridClickable(final GridViewHolder gridViewHolder){
        gridViewHolder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGridImageClickListener.onImageClick(gridViewHolder.getAdapterPosition());
            }
        });
    }

    public void setOnGridImageClickListener(OnGridImageClickListener onGridImageClickListener){
        this.onGridImageClickListener = onGridImageClickListener;
    }

    public interface OnGridImageClickListener{
        void onImageClick(int position);
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flRoot;
        ImageView ivPhoto;

        FeedItem postItem;
        private int lastAnimatedItem = -1;

        GridViewHolder(View view) {
            super(view);
            initVariable(view);
        }
        private void initVariable(View view){
            flRoot = view.findViewById(R.id.flRoot);
            ivPhoto = view.findViewById(R.id.ivPhoto);
        }

        void bindView(FeedItem postItem){
            this.postItem = postItem;
            int position = getAdapterPosition();

            switch (postItem.getType()){
                case "CARD":
                    ivPhoto.setImageResource(R.drawable.placeholder_card);
                    break;
                case "IMAGE":
                case "TALKING_PHOTO":
                    Glide.with(itemView).load(postItem.getSource()).apply(RequestOptions.placeholderOf(R.drawable.placeholder_image)).into(ivPhoto);
                    break;
                case "AUDIO":
                    ivPhoto.setImageResource(R.drawable.placeholder_audio);
                    break;
                case "VIDEO":
                case "GIF":
                    Glide.with(itemView).load(postItem.getCover()).apply(RequestOptions.placeholderOf(R.drawable.placeholder_video)).into(ivPhoto);
                    break;
            }

            if (lastAnimatedItem < position) lastAnimatedItem = position;
        }
    }

    public void setLockedAnimations(boolean lockedAnimations) {
        this.lockedAnimations = lockedAnimations;
    }
}
