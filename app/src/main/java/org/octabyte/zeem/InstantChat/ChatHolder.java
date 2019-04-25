package org.octabyte.zeem.InstantChat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.octabyte.zeem.R;

public class ChatHolder extends RecyclerView.ViewHolder {
    private TextView tvMessageSend, tvMessageReceive, tvMessageCheck;

    ChatHolder(@NonNull View itemView) {
        super(itemView);

        tvMessageSend = (TextView) itemView.findViewById(R.id.tvMessageSend);
        tvMessageReceive = (TextView) itemView.findViewById(R.id.tvMessageReceive);
        tvMessageCheck = (TextView) itemView.findViewById(R.id.tvMessageCheck);

    }

    public void bindView(MessageModel messageModel){

        if (messageModel.isSender()){
            tvMessageSend.setVisibility(View.VISIBLE);
            tvMessageSend.setText(messageModel.getMessage());
        }else {
            tvMessageReceive.setVisibility(View.VISIBLE);
            tvMessageReceive.setText(messageModel.getMessage());
        }

        if (messageModel.isSend()){
            tvMessageCheck.setVisibility(View.VISIBLE);
            tvMessageCheck.setText(R.string.message_send);
        }

        if (messageModel.isReceived()){
            tvMessageCheck.setVisibility(View.VISIBLE);
            tvMessageCheck.setTextColor(Color.parseColor("#00b896"));
            tvMessageCheck.setText(R.string.message_received);
        }

    }
}
