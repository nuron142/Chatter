package com.nuron.chatter.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.nuron.chatter.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 24/12/15.
 */
public class ChatSendViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.chat_send_text)
    public TextView sendText;

    public ChatSendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
