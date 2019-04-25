package org.octabyte.zeem.Home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import static org.octabyte.zeem.Home.HomeActivity.APP_USER_ID;

/**
 * Created by Azeem on 8/1/2017.
 */

public class FeedContextMenu extends LinearLayout implements View.OnClickListener {
    private static final int CONTEXT_MENU_WIDTH = Utils.dpToPx(240);

    private String postSafeKey = null;
    private String feedSafeKey = null;
    private Long postId;
    private Boolean isTagged = false;
    private Boolean isTagApproved = false;
    private Boolean isPublic = false;

    private OnFeedContextMenuItemClickListener onItemClickListener;

    public FeedContextMenu(Context context, Boolean tagged, Boolean tagApproved, Long userId) {
        super(context);
        isTagged = tagged;
        isTagApproved = tagApproved;

        init();
        initVariable(userId);
    }
    private void initVariable(Long userId){
        Button btnReport = findViewById(R.id.btnReport);
        Button btnShowOnProfile = findViewById(R.id.btnShowOnProfile);
        Button btnRemoveTag = findViewById(R.id.btnRemoveTag);
        Button btnHidePost = findViewById(R.id.btnHidePost);
        Button btnSavePost = findViewById(R.id.btnSavePost);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnDeletePost = findViewById(R.id.btnDeletePost);

        if(isTagged && !isTagApproved)
            btnShowOnProfile.setVisibility(VISIBLE);

        /*else if(isTagged)
            btnRemoveTag.setVisibility(VISIBLE);*/

        if (APP_USER_ID.equals(userId)) { // This is my post, Remove Save post visible Delete post
            btnSavePost.setVisibility(GONE);
            btnDeletePost.setVisibility(VISIBLE);
        }


        btnReport.setOnClickListener(this);
        btnShowOnProfile.setOnClickListener(this);
        btnRemoveTag.setOnClickListener(this);
        btnHidePost.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSavePost.setOnClickListener(this);
        btnDeletePost.setOnClickListener(this);
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_context_menu, this, true);
        setBackgroundResource(R.drawable.bg_container_shadow);
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(CONTEXT_MENU_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void bindToItem(String postSafeKey, String feedSafeKey, Long postId, Boolean isPublic) {
        this.feedSafeKey = feedSafeKey;
        this.postSafeKey = postSafeKey;
        this.postId = postId;
        this.isPublic = isPublic;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void dismiss() {
        ((ViewGroup) getParent()).removeView(FeedContextMenu.this);
    }


    private void onReportClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onReportClick(postSafeKey);
        }
    }

    private void onTagApprovedClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onApproveTagClick(postId, isPublic);
        }
    }

    private void onRemoveTagClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onRemoveTagClick(postSafeKey);
        }
    }

    private void onHidePostClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onHidePostClick(feedSafeKey);
        }
    }

    private void onCancelClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onCancelClick(postSafeKey);
        }
    }

    private void onSavePostClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onSavePostClick(postSafeKey);
        }
    }

    private void onDeletePostClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onDeletePostClick(postSafeKey);
        }
    }

    public void setOnFeedMenuItemClickListener(OnFeedContextMenuItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnReport:
                    onReportClick();
                break;
            case R.id.btnShowOnProfile:
                    onTagApprovedClick();
                break;
            case R.id.btnRemoveTag:
                    onRemoveTagClick();
                break;
            case R.id.btnHidePost:
                    onHidePostClick();
                break;
            case R.id.btnCancel:
                    onCancelClick();
                break;
            case R.id.btnSavePost:
                onSavePostClick();
                break;
            case R.id.btnDeletePost:
                onDeletePostClick();
                break;
        }
    }

    public interface OnFeedContextMenuItemClickListener {
        void onReportClick(String postSafeKey);

        void onApproveTagClick(Long postId, Boolean isPublic);

        void onRemoveTagClick(String postSafeKey);

        void onHidePostClick(String feedSafeKey);

        void onCancelClick(String postSafeKey);

        void onSavePostClick(String postSafeKey);

        void onDeletePostClick(String postSafeKey);
    }
}