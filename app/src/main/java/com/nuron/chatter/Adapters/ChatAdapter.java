package com.nuron.chatter.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuron.chatter.Activities.ChatActivity;
import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.R;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 24/12/15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<ParseUser> parseUsers;
    Context context;
    private final static String TAG = UsersRecyclerAdapter.class.getSimpleName();

    public ChatAdapter(Context context) {
        super();
        Log.d(TAG, "UsersRecyclerAdapter is initialised ");
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
                .inflate(R.layout.user_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final ParseUser parseUser = parseUsers.get(position);

        viewHolder.userName.setText(
                parseUser.getString(LoginActivity.USER_ACCOUNT_NAME));
        viewHolder.userEmail.setText(
                parseUser.getString(LoginActivity.USER_PERSONAL_EMAIL));
        viewHolder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatActivity.class));
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
        @Bind(R.id.user_layout)
        View userLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
