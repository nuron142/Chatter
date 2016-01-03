package com.nuron.chatter.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nuron.chatter.Fragments.FriendRequestsFragment;
import com.nuron.chatter.Model.ParseFriendRequest;
import com.nuron.chatter.R;
import com.nuron.chatter.ViewHolders.FriendRequestViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuron on 02/01/16.
 */
public class FriendRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = SearchAddFriendAdapter.class.getSimpleName();

    List<ParseFriendRequest> parseFriendRequests;
    Context context;
    FriendRequestsFragment friendRequestsFragment;

    public FriendRequestsAdapter(Context context, FriendRequestsFragment fragment) {
        super();
        this.context = context;
        this.friendRequestsFragment = fragment;
        parseFriendRequests = new ArrayList<>();
    }

    public void addData(ParseFriendRequest parseFriendRequest) {
        parseFriendRequests.add(parseFriendRequest);
    }

    public void removeData(int position) {
        parseFriendRequests.remove(position);
    }

    public void clear() {
        if (parseFriendRequests != null) {
            parseFriendRequests.clear();
        }
    }

    public ParseFriendRequest getItemAtPos(int position) {
        if (parseFriendRequests != null) {
            return parseFriendRequests.get(position);
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
        final ParseFriendRequest parseFriendRequest = parseFriendRequests.get(position);

        final FriendRequestViewHolder friendRequestViewHolder =
                (FriendRequestViewHolder) viewHolder;

        friendRequestViewHolder.friendName.setText(parseFriendRequest.getUserName());
        friendRequestViewHolder.friendEmail.setText(parseFriendRequest.getUserEmail());

        friendRequestViewHolder.acceptFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friendRequestsFragment.
                        handleFriendRequest(friendRequestViewHolder, position, true);

            }
        });

        friendRequestViewHolder.rejectFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friendRequestsFragment.
                        handleFriendRequest(friendRequestViewHolder, position, false);

            }
        });

    }

    @Override
    public int getItemCount() {
        return parseFriendRequests.size();
    }

}