package com.nuron.chatter.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nuron.chatter.Model.ChatSingle;
import com.nuron.chatter.R;
import com.nuron.chatter.ViewHolders.ChatReceiveViewHolder;
import com.nuron.chatter.ViewHolders.ChatSendViewHolder;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuron on 24/12/15.
 */
public class ChatSingleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_SENDER = 1;
    private static final int VIEW_RECEIVER = 2;

    List<ChatSingle> chatSingleList;
    Context context;
    private final static String TAG = UsersRecyclerAdapter.class.getSimpleName();
    String senderId, receiverId;

    public ChatSingleAdapter(Context context, String receiverId) {
        super();
        Log.d(TAG, "UsersRecyclerAdapter is initialised ");
        this.context = context;
        chatSingleList = new ArrayList<>();
        this.receiverId = receiverId;
        this.senderId = ParseUser.getCurrentUser().getObjectId();
    }

    public void addData(ChatSingle chatSingle) {
        chatSingleList.add(chatSingle);
    }

    public void removeData(int position) {
        chatSingleList.remove(position);
    }

    public void clear() {
        if (chatSingleList != null) {
            chatSingleList.clear();
        }
    }

    @Override
    public int getItemViewType(int position) {

        ChatSingle chatSingle = chatSingleList.get(position);

        if (chatSingle.getSenderId().equals(senderId)) {
            return VIEW_SENDER;
        } else {
            return VIEW_RECEIVER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view;
        switch (viewType) {

            case VIEW_SENDER:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.chat_sender_item_layout, viewGroup, false);
                return new ChatSendViewHolder(view);
            case VIEW_RECEIVER:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.chat_recieve_item_layout, viewGroup, false);
                return new ChatReceiveViewHolder(view);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        final ChatSingle chatSingle = chatSingleList.get(position);
        switch (viewHolder.getItemViewType()) {

            case VIEW_SENDER:
                setUpChatSenderLayout((ChatSendViewHolder) viewHolder, chatSingle);
                break;

            case VIEW_RECEIVER:
                setUpChatReceiverLayout((ChatReceiveViewHolder) viewHolder, chatSingle);
                break;
        }

    }

    private void setUpChatReceiverLayout(ChatReceiveViewHolder chatReceiveViewHolder,
                                         ChatSingle chatSingle) {
        chatReceiveViewHolder.receiveText.setText(chatSingle.getChatText());
        chatReceiveViewHolder.receiveUsername.setText(chatSingle.getReceiverName());
    }

    private void setUpChatSenderLayout(ChatSendViewHolder chatSendViewHolder,
                                       ChatSingle chatSingle) {
        chatSendViewHolder.sendText.setText(chatSingle.getChatText());
    }

    @Override
    public int getItemCount() {
        return chatSingleList.size();
    }
}
