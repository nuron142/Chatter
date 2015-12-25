package com.nuron.chatter.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuron.chatter.Activities.ChatGroupActivity;
import com.nuron.chatter.Model.ChatGroup;
import com.nuron.chatter.Model.ChatGroupMessage;
import com.nuron.chatter.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 26/12/15.
 */
public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.ViewHolder> {

    List<ChatGroup> chatGroups;
    Context context;
    private final static String TAG = GroupsRecyclerAdapter.class.getSimpleName();

    public GroupsRecyclerAdapter(Context context) {
        super();
        this.context = context;
        chatGroups = new ArrayList<>();
    }

    public void addData(ChatGroup chatGroup) {
        chatGroups.add(chatGroup);
    }

    public void removeData(int position) {
        chatGroups.remove(position);
    }

    public void clear() {
        if (chatGroups != null) {
            chatGroups.clear();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final ChatGroup chatGroup = chatGroups.get(position);

        viewHolder.userName.setText(chatGroup.getString(ChatGroup.GROUP_NAME));

        viewHolder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ChatGroupActivity.class);
                intent.putExtra(ChatGroupMessage.GROUP_ID,
                        chatGroup.getString(ChatGroupMessage.GROUP_ID));
                intent.putExtra(ChatGroupMessage.GROUP_NAME,
                        chatGroup.getString(ChatGroupMessage.GROUP_NAME));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatGroups.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.group_name)
        TextView userName;

        @Bind(R.id.user_layout)
        View userLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
