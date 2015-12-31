package com.nuron.chatter.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.nuron.chatter.Fragments.CreateGroupFragment;
import com.nuron.chatter.Model.ChatGroup;
import com.nuron.chatter.R;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;
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

    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    GroupsRecyclerAdapter groupsRecyclerAdapter;
    CompositeSubscription allSubscriptions;
    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupsRecyclerAdapter = new GroupsRecyclerAdapter(this);
        recyclerView.setAdapter(groupsRecyclerAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadChatGroups();
            }
        });

        drawerToggle =
                new ActionBarDrawerToggle(this, drawer, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        setUpNavigationView();
    }

    public void setUpNavigationView(){

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
        showAddGroupFragment();
    }

    public boolean handleBackPressed() {

        CreateGroupFragment createGroupFragment =
                (CreateGroupFragment) getSupportFragmentManager().
                        findFragmentByTag(CreateGroupFragment.TAG);

        if (createGroupFragment == null) {
            return false;
        } else {

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Groups");
            }

            getSupportFragmentManager().beginTransaction().remove(createGroupFragment).commit();
            getSupportFragmentManager().executePendingTransactions();

            setDrawerState(true);
            return true;
        }
    }

    private void showAddGroupFragment() {

        CreateGroupFragment createGroupFragment = new CreateGroupFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, createGroupFragment, CreateGroupFragment.TAG)
                .commit();

        setDrawerState(false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Group");
        }
        getFragmentManager().executePendingTransactions();
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

        if (!handleBackPressed()) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
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

    public void loadChatGroups() {

        final ParseQuery<ChatGroup> groupsQuery = ParseQuery.getQuery(ChatGroup.class);
        groupsQuery.addDescendingOrder(ChatGroup.UPDATED_AT);


        emptyItemsLayout.setVisibility(View.GONE);
        if(!swipeRefreshLayout.isRefreshing()){
            progressWheel.spin();
        }
        groupsRecyclerAdapter.clear();

        allSubscriptions.add(Observable.fromCallable(
                new Callable<List<ChatGroup>>() {
                    @Override
                    public List<ChatGroup> call() throws Exception {
                        return groupsQuery.find();
                    }
                })
                .flatMap(new Func1<List<ChatGroup>, Observable<ChatGroup>>() {
                    @Override
                    public Observable<ChatGroup> call(List<ChatGroup> chatGroupses) {
                        return Observable.from(chatGroupses);
                    }
                })
                .subscribe(new Subscriber<ChatGroup>() {
                    @Override
                    public void onCompleted() {
                        if (groupsRecyclerAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }
                        if(!swipeRefreshLayout.isRefreshing()){
                            progressWheel.stopSpinning();
                        } else {
                            swipeRefreshLayout.setRefreshing(false);
                        }

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
                    public void onNext(ChatGroup chatGroup) {
                        groupsRecyclerAdapter.addData(chatGroup);
                    }
                })
        );

    }

    public void setDrawerState(boolean isEnabled) {

        if (isEnabled) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.syncState();

        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            drawerToggle.syncState();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                drawerToggle.setDrawerIndicatorEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleBackPressed();
                }
            });
        }
    }

}