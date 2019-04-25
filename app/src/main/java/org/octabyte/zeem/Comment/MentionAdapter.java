package org.octabyte.zeem.Comment;

import android.content.Context;
import android.view.View;

import com.appspot.octabyte_zeem.zeem.model.User;

import org.octabyte.zeem.List.SingleListAdapter;

import java.util.List;

/**
 * Created by Azeem on 8/11/2017.
 */

public class MentionAdapter extends SingleListAdapter {

    private OnMentionItemClickListener onMentionItemClickListener;

    public MentionAdapter(Context context, List<User> mentionList) {
        super(context, mentionList);
    }

    public void setOnMentionItemClickListener(OnMentionItemClickListener onMentionItemClickListener) {
        this.onMentionItemClickListener = onMentionItemClickListener;
    }

    public interface OnMentionItemClickListener {
        void onMentionClick(String username, Long userId);
    }

    @Override
    protected void setupClickableViews(final UserViewHolder vh) {
        vh.llUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMentionItemClickListener.onMentionClick(vh.member.getUsername(), vh.member.getUserId());
            }
        });
    }

}
