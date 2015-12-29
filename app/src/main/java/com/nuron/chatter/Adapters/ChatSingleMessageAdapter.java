package com.nuron.chatter.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.nuron.chatter.Model.ChatSingleMessage;
import com.nuron.chatter.R;
import com.nuron.chatter.Utilities;
import com.nuron.chatter.ViewHolders.ChatReceiveViewHolder;
import com.nuron.chatter.ViewHolders.ChatSendViewHolder;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuron on 24/12/15.
 */
public class ChatSingleMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_SENDER = 1;
    private static final int VIEW_RECEIVER = 2;

    List<ChatSingleMessage> chatSingleMessageList;
    Context context;
    private final static String TAG = UsersRecyclerAdapter.class.getSimpleName();
    String senderId;
    int imageWidth, imageHeight;
    RequestManager glide;

    public ChatSingleMessageAdapter(Context context) {
        super();
        Log.d(TAG, "UsersRecyclerAdapter is initialised ");
        this.context = context;
        chatSingleMessageList = new ArrayList<>();
        this.senderId = ParseUser.getCurrentUser().getObjectId();
        this.imageWidth = context.getResources().getDimensionPixelSize(R.dimen.image_width);
        this.imageHeight = context.getResources().getDimensionPixelSize(R.dimen.image_height);
        this.glide = Glide.with(context);
    }

    public void addData(ChatSingleMessage chatSingleMessage) {
        chatSingleMessageList.add(chatSingleMessage);
    }

    public void removeData(int position) {
        chatSingleMessageList.remove(position);
    }

    public void clear() {
        if (chatSingleMessageList != null) {
            chatSingleMessageList.clear();
        }
    }

    @Override
    public int getItemViewType(int position) {

        ChatSingleMessage chatSingleMessage = chatSingleMessageList.get(position);

        if (chatSingleMessage.getSenderId().equals(senderId)) {
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

        final ChatSingleMessage chatSingleMessage = chatSingleMessageList.get(position);
        switch (viewHolder.getItemViewType()) {

            case VIEW_SENDER:
                setUpChatSenderLayout((ChatSendViewHolder) viewHolder, chatSingleMessage);
                break;

            case VIEW_RECEIVER:
                setUpChatReceiverLayout((ChatReceiveViewHolder) viewHolder, chatSingleMessage);
                break;
        }

    }

    private void setUpChatReceiverLayout(ChatReceiveViewHolder chatReceiveViewHolder,
                                         ChatSingleMessage chatSingleMessage) {

        Log.d(TAG, "Receiver Account name : " + chatSingleMessage.getSenderName());

        String imageId = chatSingleMessage.getImageId();
        if (imageId != null && !imageId.isEmpty()) {

            chatReceiveViewHolder.receiverImage.setVisibility(View.VISIBLE);
            chatReceiveViewHolder.receiveText.setVisibility(View.GONE);
            chatReceiveViewHolder.receiveUsername.setVisibility(View.GONE);

            String[] imageUrls = Utilities.getHalfAndFullResolutionUrl(imageHeight / 2,
                    imageWidth / 2, imageHeight, imageWidth, imageId);

            final String imageUrlResolutionHalf = imageUrls[0];
            final String imageUrlResolutionFull = imageUrls[1];

            final DrawableRequestBuilder<String>
                    imageResolutionHalfRequest = glide.load(imageUrlResolutionHalf);

            glide.load(imageUrlResolutionFull)
                    .placeholder(R.drawable.image_placeholder)
                    .thumbnail(imageResolutionHalfRequest)
                    .dontAnimate()
                    .into(chatReceiveViewHolder.receiverImage);
        } else {

            chatReceiveViewHolder.receiverImage.setVisibility(View.GONE);
            chatReceiveViewHolder.receiveText.setVisibility(View.VISIBLE);
            chatReceiveViewHolder.receiveUsername.setVisibility(View.VISIBLE);

            chatReceiveViewHolder.receiveText.setText(chatSingleMessage.getChatText());
            chatReceiveViewHolder.receiveUsername.setText(chatSingleMessage.getSenderName());
        }
    }

    private void setUpChatSenderLayout(ChatSendViewHolder chatSendViewHolder,
                                       ChatSingleMessage chatSingleMessage) {

        String imageId = chatSingleMessage.getImageId();
        if (imageId != null && !imageId.isEmpty()) {

            chatSendViewHolder.sendText.setVisibility(View.GONE);
            chatSendViewHolder.senderImage.setVisibility(View.VISIBLE);
            String[] imageUrls = Utilities.getHalfAndFullResolutionUrl(imageHeight / 2,
                    imageWidth / 2, imageHeight, imageWidth, imageId);

            final String imageUrlResolutionHalf = imageUrls[0];
            final String imageUrlResolutionFull = imageUrls[1];

            final DrawableRequestBuilder<String>
                    imageResolutionHalfRequest = glide.load(imageUrlResolutionHalf);

            glide.load(imageUrlResolutionFull)
                    .placeholder(R.drawable.image_placeholder)
                    .thumbnail(imageResolutionHalfRequest)
                    .dontAnimate()
                    .into(chatSendViewHolder.senderImage);
        } else {

            chatSendViewHolder.senderImage.setVisibility(View.GONE);
            chatSendViewHolder.sendText.setVisibility(View.VISIBLE);
            chatSendViewHolder.sendText.setText(chatSingleMessage.getChatText());
        }
    }

    @Override
    public int getItemCount() {
        return chatSingleMessageList.size();
    }
}
