package com.nuron.chatter.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nuron.chatter.Adapters.ChatSingleAdapter;
import com.nuron.chatter.Model.ChatSingle;
import com.nuron.chatter.R;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    @Bind(R.id.chat_recycler_view)
    RecyclerView chatRecyclerView;

    @Bind(R.id.chat_editText)
    EditText chatEditText;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    Context context;
    CompositeSubscription allSubscriptions;
    String senderId, receiveId, receiverName;
    ChatSingleAdapter chatSingleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        context = this;
        receiveId = getIntent().getExtras().getString(ChatSingle.RECEIVER_ID);
        receiverName = getIntent().getExtras().getString(LoginActivity.USER_ACCOUNT_NAME);
        senderId = ParseUser.getCurrentUser().getObjectId();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatSingleAdapter = new ChatSingleAdapter(this, receiveId);
        chatRecyclerView.setAdapter(chatSingleAdapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    @OnClick(R.id.send_message_button)
    public void sendMessage() {

        if (chatEditText.getText().length() < 1) {
            Toast.makeText(this, "Message can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatSingle chatSingle = new ChatSingle();
        chatSingle.setChatText(chatEditText.getText().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        chatSingle.setSentDate(dateFormat.format(date));
        chatSingle.setSenderId(senderId);
        chatSingle.setReceiverId(receiveId);
        chatSingle.setReceiverName(receiverName);

        ParseACL acl = new ParseACL();
        acl.setReadAccess(ParseUser.getCurrentUser(), true);
        acl.setReadAccess(receiveId, true);
        acl.setWriteAccess(ParseUser.getCurrentUser(), true);

        chatSingle.setACL(acl);

        chatSingleAdapter.addData(chatSingle);
        chatSingleAdapter.notifyItemInserted(chatSingleAdapter.getItemCount());
        chatEditText.setText("");

        allSubscriptions.add(ParseObservable.save(chatSingle)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(getApplicationContext(),
                                "Successfully saved", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context,
                                "Couldn't save. Please try again", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ParseObject parseObject) {

                    }
                })
        );

    }

    @Override
    public void onStart() {
        super.onStart();
        allSubscriptions = new CompositeSubscription();
        loadChatMessages();

    }

    @Override
    public void onStop() {
        super.onStop();

        if (allSubscriptions != null && !allSubscriptions.isUnsubscribed()) {

            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
    }

    public void loadChatMessages() {

        emptyItemsLayout.setVisibility(View.GONE);
        progressWheel.spin();
        chatSingleAdapter.clear();
        ParseQuery<ChatSingle> senderQuery = ParseQuery.getQuery(ChatSingle.class);
        senderQuery.whereEqualTo(ChatSingle.SENDER_ID,
                ParseUser.getCurrentUser().getObjectId());
        senderQuery.whereEqualTo(ChatSingle.RECEIVER_ID, receiveId);

        ParseQuery<ChatSingle> receiverQuery = ParseQuery.getQuery(ChatSingle.class);
        receiverQuery.whereEqualTo(ChatSingle.SENDER_ID, receiveId);
        receiverQuery.whereEqualTo(ChatSingle.RECEIVER_ID,
                ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ChatSingle>> queries = new ArrayList<>();
        queries.add(senderQuery);
        queries.add(receiverQuery);

        final ParseQuery<ChatSingle> messageQuery = ParseQuery.or(queries);
        messageQuery.setLimit(50);
        messageQuery.addAscendingOrder("createdAt");

        allSubscriptions.add(Observable.interval(0, 30, TimeUnit.SECONDS, Schedulers.newThread())
                .map(new Func1<Long, List<ChatSingle>>() {
                    @Override
                    public List<ChatSingle> call(Long aLong) {
                        Log.d(TAG, "Starting polling");
                        chatSingleAdapter.clear();
                        try {
                            return messageQuery.find();
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ChatSingle>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Exception during getting messages : " + e);
                        Toast.makeText(ChatActivity.this,
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<ChatSingle> chatSingleList) {
                        Log.d(TAG, "Polling finished");
                        if (chatSingleList.size() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                            progressWheel.stopSpinning();
                        } else {

                            for (ChatSingle chatSingle : chatSingleList) {
                                chatSingleAdapter.addData(chatSingle);
                            }

                            progressWheel.stopSpinning();
                            chatSingleAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(chatSingleList.size() - 1);
                        }
                    }

                })
        );

    }

}
