package com.nuron.chatter.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nuron.chatter.Activities.GroupsActivity;
import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.Model.ChatGroup;
import com.nuron.chatter.Model.ChatGroupMessage;
import com.nuron.chatter.R;
import com.parse.ParseACL;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

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

/**
 * Created by nuron on 26/12/15.
 */
public class CreateGroupFragment extends Fragment {

    public static final String TAG = SearchUsersFragment.class.getSimpleName();

    @Bind(R.id.group_name)
    MaterialEditText groupName;

    @Bind(R.id.group_description)
    MaterialEditText groupDescription;

    @Bind(R.id.group_created_by)
    TextView groupCreatedBy;

    @Bind(R.id.group_creation_date)
    TextView groupCreationDate;

    private Context context;
    CompositeSubscription allSubscriptions;
    String currentUser;

    public CreateGroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_group, container, false);
        ButterKnife.bind(this, rootView);

        currentUser = ParseUser.getCurrentUser().getString(LoginActivity.USER_ACCOUNT_NAME);
        groupCreatedBy.setText("Created by : " + currentUser);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        Date date = new Date();
        groupCreationDate.setText("Date : " + dateFormat.format(date));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        allSubscriptions = new CompositeSubscription();
    }


    @Override
    public void onStop() {
        super.onStop();
        cancelAllSubscriptions();
    }


    @OnClick(R.id.create_group_fab)
    public void createGroup() {

        if (groupName.getText().length() < 1) {
            Toast.makeText(getActivity(), "Group Name can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Creating group");
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelAllSubscriptions();
            }
        });

        //Group
        final ChatGroup chatGroup = new ChatGroup();
        chatGroup.setGroupId(UUID.randomUUID().toString());
        chatGroup.setGroupName(groupName.getText().toString());
        chatGroup.setGroupDescription(groupDescription.getText().toString());
        chatGroup.setGroupCreateBy(
                ParseUser.getCurrentUser().getString(LoginActivity.USER_ACCOUNT_NAME));

        ParseACL groupAcl = new ParseACL();
        groupAcl.setPublicReadAccess(true);
        groupAcl.setWriteAccess(ParseUser.getCurrentUser(), true);
        chatGroup.setACL(groupAcl);

        // Group chat message
        final ChatGroupMessage chatGroupMessage = new ChatGroupMessage();
        chatGroupMessage.setChatText("Group created by " +
                ParseUser.getCurrentUser().getString(LoginActivity.USER_ACCOUNT_NAME));
        chatGroupMessage.setSenderId(ParseUser.getCurrentUser().getObjectId());
        chatGroupMessage.setGroupId(chatGroup.getGroupId());
        chatGroupMessage.setGroupName(chatGroup.getGroupName());
        chatGroupMessage.setSenderName(
                ParseUser.getCurrentUser().getString(LoginActivity.USER_ACCOUNT_NAME));

        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        acl.setWriteAccess(ParseUser.getCurrentUser(), true);
        chatGroupMessage.setACL(acl);

        final ParseQuery<ChatGroup> groupExistsQuery = ParseQuery.getQuery(ChatGroup.class);
        groupExistsQuery.whereEqualTo(ChatGroup.GROUP_ID, chatGroup.getGroupId());
        groupExistsQuery.whereEqualTo(ChatGroup.GROUP_NAME, chatGroup.getGroupName());

        allSubscriptions.add(Observable.fromCallable(
                new Callable<List<ChatGroup>>() {
                    @Override
                    public List<ChatGroup> call() throws Exception {
                        return groupExistsQuery.find();
                    }
                })
                .flatMap(new Func1<List<ChatGroup>, Observable<ChatGroup>>() {
                    @Override
                    public Observable<ChatGroup> call(List<ChatGroup> chatGroups) {
                        if (chatGroups.size() > 0) {
                            throw new SecurityException("Group already registered");
                        } else {
                            return ParseObservable.save(chatGroup);
                        }
                    }
                })
                .flatMap(new Func1<ChatGroup, Observable<ChatGroupMessage>>() {
                    @Override
                    public Observable<ChatGroupMessage> call(ChatGroup chatGroup) {
                        return ParseObservable.save(chatGroupMessage);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ChatGroupMessage>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "New Chat group saved");
                        progressDialog.dismiss();
                        ((GroupsActivity) getActivity()).loadChatGroups();
                        ((GroupsActivity) getActivity()).handleBackPressed();
                    }

                    @Override
                    public void onError(Throwable e) {

                        Log.d(TAG, "Exception during adding group : " + e);
                        Toast.makeText(getActivity(),
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ChatGroupMessage chatSingleMessage) {
                    }
                })
        );
    }

    private void cancelAllSubscriptions() {
        if (allSubscriptions != null && !allSubscriptions.isUnsubscribed()) {
            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
    }
}
