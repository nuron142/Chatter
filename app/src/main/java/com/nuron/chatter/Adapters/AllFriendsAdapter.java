package com.nuron.chatter.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuron.chatter.Activities.ChatSingleActivity;
import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.Model.ChatSingleMessage;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 02/01/16.
 */
public class AllFriendsAdapter extends RecyclerView.Adapter<AllFriendsAdapter.ViewHolder> {

    List<ParseFriend> parseFriends;
    Context context;
    private final static String TAG = AllFriendsAdapter.class.getSimpleName();

    public AllFriendsAdapter(Context context) {
        super();
        this.context = context;
        parseFriends = new ArrayList<>();
    }

    public void addData(ParseFriend parseFriend) {
        parseFriends.add(parseFriend);
    }

    public void removeData(int position) {
        parseFriends.remove(position);
    }

    public void clear() {
        if (parseFriends != null) {
            parseFriends.clear();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final ParseFriend parseFriend = parseFriends.get(position);

        viewHolder.userName.setText(
                parseFriend.getFriendName());
        viewHolder.userEmail.setText(
                parseFriend.getString(LoginActivity.USER_PERSONAL_EMAIL));
        viewHolder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ChatSingleActivity.class);
                intent.putExtra(ChatSingleMessage.RECEIVER_ID, parseFriend.getObjectId());
                intent.putExtra(LoginActivity.USER_ACCOUNT_NAME,
                        parseFriend.getString(LoginActivity.USER_ACCOUNT_NAME));
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return parseFriends.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.user_name)
        TextView userName;
        @Bind(R.id.user_email)
        TextView userEmail;
        @Bind(R.id.user_layout)
        View userLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}