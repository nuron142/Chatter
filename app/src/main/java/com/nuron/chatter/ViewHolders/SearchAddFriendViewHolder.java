package com.nuron.chatter.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nuron.chatter.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 30/12/15.
 */

public class SearchAddFriendViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.user_name)
    public TextView userName;

    @Bind(R.id.user_email)
    public TextView userEmail;

    @Bind(R.id.add_friend_layout)
    public View addFriendLayout;

    @Bind(R.id.add_friend_image)
    public ImageView addFriendImage;

    @Bind(R.id.friend_request_accpted_image)
    public ImageView friendRequestAcceptedImage;

    @Bind(R.id.add_friend_progress)
    public ProgressWheel addFriendProgress;

    public SearchAddFriendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
