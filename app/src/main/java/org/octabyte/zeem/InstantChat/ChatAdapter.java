package org.octabyte.zeem.InstantChat;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.octabyte.zeem.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<MessageModel> messages;

    public ChatAdapter(Context context, List<MessageModel> messages){
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_message, viewGroup, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ( (ChatHolder) viewHolder ).bindView(messages.get(i));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public List<MessageModel> getMessages(){
        return messages;
    }


    public void insertItem(MessageModel message){
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateItem(int index, MessageModel message){
        messages.set(index, message);
        notifyDataSetChanged();
    }


}
