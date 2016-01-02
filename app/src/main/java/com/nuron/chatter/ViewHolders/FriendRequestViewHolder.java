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
 * Created by nuron on 02/01/16.
 */
public class FriendRequestViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.user_name)
    public TextView friendName;

    @Bind(R.id.user_email)
    public TextView friendEmail;

    @Bind(R.id.friend_request_layout)
    public View friendRequestLayout;

    @Bind(R.id.accept_friend_layout)
    public View acceptFriendLayout;

    @Bind(R.id.accept_friend_image)
    public ImageView acceptFriendImage;

    @Bind(R.id.accept_friend_progress)
    public ProgressWheel acceptFriendProgress;


    @Bind(R.id.reject_friend_layout)
    public View rejectFriendLayout;

    @Bind(R.id.reject_friend_image)
    public ImageView rejectFriendImage;

    @Bind(R.id.reject_friend_progress)
    public ProgressWheel rejectFriendProgress;


    public FriendRequestViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
