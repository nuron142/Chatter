package com.nuron.chatter.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nuron.chatter.Fragments.AllFriendsFragment;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.R;
import com.nuron.chatter.ViewHolders.FriendRequestViewHolder;

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
                .inflate(R.layout.friend_requests_item_layout, viewGroup, false);
        return new FriendRequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final ParseFriend parseFriend = parseFriends.get(position);

        final FriendRequestViewHolder searchAddFriendViewHolder =
                (FriendRequestViewHolder) viewHolder;

        searchAddFriendViewHolder.friendName.setText(parseFriend.getFriendName());
        searchAddFriendViewHolder.friendEmail.setText(
                parseFriend.getFriendEmail());

        boolean disableClick = false;

//        if (parseFriendRequest.getRequestSent() != null &&
//                parseFriendRequest.getRequestSent().equals(SearchUser.STRING_TRUE)) {
//            disableClick = true;
//
//            searchAddFriendViewHolder.addFriendImage.setVisibility(View.GONE);
//            searchAddFriendViewHolder.friendRequestAcceptedImage.setVisibility(View.VISIBLE);
//
//            if (parseFriendRequest.getRequestAccepted() != null &&
//                    parseFriendRequest.getRequestAccepted().equals(SearchUser.STRING_TRUE)) {
//                disableClick = true;
//
//                searchAddFriendViewHolder.friendRequestAcceptedImage.setVisibility(View.VISIBLE);
//            }
//
//        } else {
//            searchAddFriendViewHolder.addFriendImage.setVisibility(View.VISIBLE);
//            searchAddFriendViewHolder.friendRequestAcceptedImage.setVisibility(View.GONE);
//        }
//
//        final boolean disableClick1 = disableClick;
//        searchAddFriendViewHolder.addFriendLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (disableClick1) {
//                    return;
//                }
//
//                //friendRequestsFragment.sendFriendRequest(searchAddFriendViewHolder, position);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return parseFriends.size();
    }
}