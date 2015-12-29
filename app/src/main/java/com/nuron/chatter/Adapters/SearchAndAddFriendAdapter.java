package com.nuron.chatter.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.R;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 29/12/15.
 */
public class SearchAndAddFriendAdapter extends RecyclerView.Adapter<SearchAndAddFriendAdapter.ViewHolder> {
    List<ParseUser> parseUsers;
    Context context;
    private final static String TAG = SearchAndAddFriendAdapter.class.getSimpleName();

    public SearchAndAddFriendAdapter(Context context) {
        super();
        this.context = context;
        parseUsers = new ArrayList<>();
    }

    public void addData(ParseUser parseUser) {
        parseUsers.add(parseUser);
    }

    public void removeData(int position) {
        parseUsers.remove(position);
    }

    public void clear() {
        if (parseUsers != null) {
            parseUsers.clear();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_add_friends_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final ParseUser parseUser = parseUsers.get(position);

        viewHolder.userName.setText(
                parseUser.getString(LoginActivity.USER_ACCOUNT_NAME));
        viewHolder.userEmail.setText(
                parseUser.getString(LoginActivity.USER_PERSONAL_EMAIL));
        viewHolder.addFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(context, ChatSingleActivity.class);
//                intent.putExtra(ChatSingleMessage.RECEIVER_ID, parseUser.getObjectId());
//                intent.putExtra(LoginActivity.USER_ACCOUNT_NAME,
//                        parseUser.getString(LoginActivity.USER_ACCOUNT_NAME));
//                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return parseUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.user_name)
        TextView userName;
        @Bind(R.id.user_email)
        TextView userEmail;
        @Bind(R.id.add_friend_layout)
        View addFriendLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
