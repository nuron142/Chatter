package com.nuron.chatter.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nuron.chatter.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 24/12/15.
 */
public class ChatReceiveViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.chat_receive_text)
    public TextView receiveText;

    @Bind(R.id.chat_receive_username)
    public TextView receiveUsername;

    @Bind(R.id.chat_receiver_image)
    public ImageView receiverImage;

    public ChatReceiveViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
