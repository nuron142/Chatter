package com.nuron.chatter.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nuron.chatter.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 03/01/16.
 */
public class AllFriendsViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.user_name)
    public TextView friendName;

    @Bind(R.id.user_email)
    public TextView friendEmail;

    @Bind(R.id.friend_all_layout)
    public View friendStartChatLayout;

    @Bind(R.id.friend_start_chat_image)
    public ImageView friendStartChatImage;

    public AllFriendsViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}

