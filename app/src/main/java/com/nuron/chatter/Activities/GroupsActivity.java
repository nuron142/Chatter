package com.nuron.chatter.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nuron.chatter.Adapters.GroupsRecyclerAdapter;
import com.nuron.chatter.Model.ChatGroupMessage;
import com.nuron.chatter.Model.ChatGroups;
import com.nuron.chatter.R;
import com.parse.ParseACL;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

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
public class GroupsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = HomeActivity.class.getSimpleName();

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    GroupsRecyclerAdapter groupsRecyclerAdapter;
    CompositeSubscription allSubscriptions;

    String senderId, groupId, groupName;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupsRecyclerAdapter = new GroupsRecyclerAdapter(this);
        recyclerView.setAdapter(groupsRecyclerAdapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView userNameTextView = (TextView) header.findViewById(R.id.userName);
        TextView userEmailTextView = (TextView) header.findViewById(R.id.userEmail);

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            String userName = parseUser.getString(LoginActivity.USER_ACCOUNT_NAME);
            String userEmail = parseUser.getString(LoginActivity.USER_PERSONAL_EMAIL);
            if (userName != null) {
                userNameTextView.setText(userName);
            }

            if (userEmail != null) {
                userEmailTextView.setText(userEmail);
            }
        }
    }

    @OnClick(R.id.fab)
    public void addNewGroup() {

        ChatGroups chatGroup = new ChatGroups();
        chatGroup.setGroupId(UUID.randomUUID().toString());
        chatGroup.setGroupName("Test Group");
        ParseACL groupAcl = new ParseACL();
        groupAcl.setPublicReadAccess(true);
        groupAcl.setWriteAccess(ParseUser.getCurrentUser(), true);
        chatGroup.setACL(groupAcl);

        final ChatGroupMessage chatGroupMessage = new ChatGroupMessage();
        chatGroupMessage.setChatText("Group created by " +
                ParseUser.getCurrentUser().getString(LoginActivity.USER_ACCOUNT_NAME));
        chatGroupMessage.setSenderId(ParseUser.getCurrentUser().getObjectId());
        chatGroupMessage.setGroupId(chatGroup.getGroupId());
        chatGroupMessage.setGroupName(chatGroup.getGroupName());
        chatGroupMessage.setSenderName(
                ParseUser.getCurrentUser().getString(LoginActivity.USER_ACCOUNT_NAME));

        ParseACL acl = new ParseACL();
        acl.setReadAccess(ParseUser.getCurrentUser(), true);
        acl.setWriteAccess(ParseUser.getCurrentUser(), true);
        chatGroupMessage.setACL(acl);

        allSubscriptions.add(ParseObservable.save(chatGroup)
                .flatMap(new Func1<ChatGroups, Observable<ChatGroupMessage>>() {
                    @Override
                    public Observable<ChatGroupMessage> call(ChatGroups chatGroups) {
                        return ParseObservable.save(chatGroupMessage);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ChatGroupMessage>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "New Chat group saved");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(ChatGroupMessage chatSingleMessage) {
                    }
                })
        );

    }

    @Override
    public void onStart() {
        super.onStart();
        allSubscriptions = new CompositeSubscription();
        loadChatGroups();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (allSubscriptions != null && !allSubscriptions.isUnsubscribed()) {
            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.logout) {
            logOutUser();
        } else if (id == R.id.users_activity) {
            launchHomeActivity();
        }
        return true;
    }

    private void launchHomeActivity() {
        Log.d(TAG, "Launching home activity");
        Intent intent = new Intent(GroupsActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void logOutUser() {

        allSubscriptions.add(ParseObservable.logOut()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Logged out successfully");
                        Intent intent = new Intent(GroupsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Logout failed : " + e);
                        Toast.makeText(GroupsActivity.this, "Logout failed : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                })
        );
    }


    private void loadChatGroups() {

        final ParseQuery<ChatGroups> groupsQuery = ParseQuery.getQuery(ChatGroups.class);
        groupsQuery.addDescendingOrder(ChatGroups.UPDATED_AT);

        progressWheel.spin();
        groupsRecyclerAdapter.clear();

        allSubscriptions.add(Observable.fromCallable(
                new Callable<List<ChatGroups>>() {
                    @Override
                    public List<ChatGroups> call() throws Exception {
                        return groupsQuery.find();
                    }
                })
                .flatMap(new Func1<List<ChatGroups>, Observable<ChatGroups>>() {
                    @Override
                    public Observable<ChatGroups> call(List<ChatGroups> chatGroupses) {
                        return Observable.from(chatGroupses);
                    }
                })
                .subscribe(new Subscriber<ChatGroups>() {
                    @Override
                    public void onCompleted() {
                        progressWheel.stopSpinning();
                        groupsRecyclerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Exception during loading groups : " + e);
                        progressWheel.stopSpinning();
                        Toast.makeText(GroupsActivity.this,
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ChatGroups chatGroups) {
                        groupsRecyclerAdapter.addData(chatGroups);
                    }
                })
        );

    }

}