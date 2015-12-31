package com.nuron.chatter.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.Fragments.SearchAndAddFriendFragment;
import com.nuron.chatter.Model.SearchUser;
import com.nuron.chatter.R;
import com.nuron.chatter.ViewHolders.SearchAddFriendViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuron on 29/12/15.
 */
public class SearchAddFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = SearchAddFriendAdapter.class.getSimpleName();

    List<SearchUser> searchUsers;
    Context context;
    SearchAndAddFriendFragment searchAndAddFriendFragment;

    public SearchAddFriendAdapter(Context context, SearchAndAddFriendFragment fragment) {
        super();
        this.context = context;
        this.searchAndAddFriendFragment = fragment;
        searchUsers = new ArrayList<>();
    }

    public void addData(SearchUser searchUser) {
        searchUsers.add(searchUser);
    }

    public void removeData(int position) {
        searchUsers.remove(position);
    }

    public void clear() {
        if (searchUsers != null) {
            searchUsers.clear();
        }
    }

    public SearchUser getItemAtPos(int position) {
        if (searchUsers != null) {
            return searchUsers.get(position);
        }

        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_add_friends_item_layout, viewGroup, false);
        return new SearchAddFriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final SearchUser searchUser = searchUsers.get(position);

        final SearchAddFriendViewHolder searchAddFriendViewHolder = (SearchAddFriendViewHolder) viewHolder;

        searchAddFriendViewHolder.userName.setText(
                searchUser.getParseUser().getString(LoginActivity.USER_ACCOUNT_NAME));
        searchAddFriendViewHolder.userEmail.setText(
                searchUser.getParseUser().getString(LoginActivity.USER_PERSONAL_EMAIL));

        boolean disableClick = false;

        if (searchUser.getIsRequestSent() != null &&
                searchUser.getIsRequestSent().equals(SearchUser.STRING_TRUE)) {
            disableClick = true;

            searchAddFriendViewHolder.addFriendImage.setVisibility(View.VISIBLE);
            searchAddFriendViewHolder.friendRequestAcceptedImage.setVisibility(View.GONE);

            searchAddFriendViewHolder.addFriendImage.setImageResource(R.drawable.ic_done_black_24dp);

            if (searchUser.getIsRequestAccepted() != null &&
                    searchUser.getIsRequestAccepted().equals(SearchUser.STRING_TRUE)) {
                disableClick = true;

                searchAddFriendViewHolder.addFriendImage.setVisibility(View.GONE);
                searchAddFriendViewHolder.friendRequestAcceptedImage.setVisibility(View.VISIBLE);
            }

        } else {
            searchAddFriendViewHolder.addFriendImage.setImageResource(R.drawable.ic_add_black_24dp);
        }

        final boolean disableClick1 = disableClick;
        searchAddFriendViewHolder.addFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (disableClick1) {
                    return;
                }

                searchAndAddFriendFragment.addFriend(searchAddFriendViewHolder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchUsers.size();
    }

}
