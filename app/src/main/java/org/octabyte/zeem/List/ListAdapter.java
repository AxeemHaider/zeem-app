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

import com.appspot.octabyte_zeem.zeem.model.UserList;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.OnSwipeTouchListener;

import java.util.List;


/**
 * Created by Azeem on 8/18/2017.
 */

class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int lastAnimatedPosition = -1;

    private boolean animationsLocked = false;

    private OnListItemClickListener onListItemClickListener;
    public List<UserList> listItems;

    public ListAdapter(Context context, List<UserList> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.view_user_list, parent, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        setupClickableViews(listViewHolder);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        ((ListViewHolder) viewHolder).bindView(listItems.get(position));
    }

    @Override
    public int getItemCount() {
        return listItems.size();
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


    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    public void setOnListItemClickListener(OnListItemClickListener onListItemClickListener) {
        this.onListItemClickListener = onListItemClickListener;
    }

    public interface OnListItemClickListener {
        void onListClick(View v, Long listId, String listName, int memberCount);
        void onListDelete(String listSafeKey, int position);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickableViews(final ListViewHolder listViewHolder){
        listViewHolder.llListLayout.setOnTouchListener(new OnSwipeTouchListener(context){
            @Override
            public void onClick(View v) {
                //super.onClick(v);
                onListItemClickListener.onListClick(v, listViewHolder.listItem.getListId(),
                        listViewHolder.listItem.getName(),
                        listViewHolder.listItem.getMemberCount());
            }

            @Override
            public void onSwipeLeft() {
                Log.i("com.azeem.LDA", "onSwipeLeft");
                onListItemClickListener.onListDelete(listViewHolder.listItem.getListSafeKey(), listViewHolder.getAdapterPosition());
            }
        });
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivListPic;
        private TextView tvListName;
        private TextView tvMemberCount;
        LinearLayout llListLayout;

        UserList listItem;

        ListViewHolder(View view) {
            super(view);
            initVariable(view);
        }
        private void initVariable(View view){
            ivListPic = view.findViewById(R.id.ivUserPic);
            tvListName = view.findViewById(R.id.tvUserFullName);
            tvMemberCount = view.findViewById(R.id.tvUsername);
            llListLayout = view.findViewById(R.id.llUserLayout);
        }

        void bindView(UserList listItem){
            this.listItem = listItem;

            //Glide.with(itemView).load(listItem.getListPic()).into(ivListPic);
            ivListPic.setImageResource(R.drawable.ic_circle);
            tvListName.setText(listItem.getName());
            tvMemberCount.setText(llListLayout.getResources().getQuantityString(R.plurals.members_count, listItem.getMemberCount(), listItem.getMemberCount()));
        }
    }
}
