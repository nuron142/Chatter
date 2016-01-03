package com.nuron.chatter.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nuron.chatter.Activities.ChatSingleActivity;
import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.Fragments.AllFriendsFragment;
import com.nuron.chatter.Model.ChatSingleMessage;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.R;
import com.nuron.chatter.ViewHolders.AllFriendsViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuron on 02/01/16.
 */
public class AllFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = SearchAddFriendAdapter.class.getSimpleName();

    List<ParseFriend> parseFriends;
    Context context;
    AllFriendsFragment friendRequestsFragment;

    public AllFriendsAdapter(Context context, AllFriendsFragment fragment) {
        super();
        this.context = context;
        this.friendRequestsFragment = fragment;
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

    public ParseFriend getItemAtPos(int position) {
        if (parseFriends != null) {
            return parseFriends.get(position);
        }

        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friend_all_item_layout, viewGroup, false);
        return new AllFriendsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final ParseFriend parseFriend = parseFriends.get(position);

        final AllFriendsViewHolder allFriendsViewHolder =
                (AllFriendsViewHolder) viewHolder;

        allFriendsViewHolder.friendName.setText(parseFriend.getFriendName());
        allFriendsViewHolder.friendEmail.setText(parseFriend.getFriendEmail());

        allFriendsViewHolder.friendStartChatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ChatSingleActivity.class);
                intent.putExtra(ChatSingleMessage.RECEIVER_ID, parseFriend.getFriendId());
                intent.putExtra(LoginActivity.USER_ACCOUNT_NAME, parseFriend.getFriendName());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return parseFriends.size();
    }
}