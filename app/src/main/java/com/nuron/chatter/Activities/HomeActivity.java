package com.nuron.chatter.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
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

import com.nuron.chatter.Adapters.UsersRecyclerAdapter;
import com.nuron.chatter.Fragments.SearchAndAddFriendFragment;
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

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = HomeActivity.class.getSimpleName();

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.search_and_add_users)
    FloatingActionButton floatingActionButton;

    UsersRecyclerAdapter usersRecyclerAdapter;
    CompositeSubscription allSubscriptions;

    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersRecyclerAdapter = new UsersRecyclerAdapter(this);
        recyclerView.setAdapter(usersRecyclerAdapter);
        recyclerView.setHasFixedSize(true);


        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUsers();
            }
        });

        drawerToggle =
                new ActionBarDrawerToggle(this, drawer, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        setUpNavigationView();
    }

    @OnClick(R.id.search_and_add_users)
    public void searchAndAddUser() {
        SearchAndAddFriendFragment createGroupFragment = new SearchAndAddFriendFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, createGroupFragment, SearchAndAddFriendFragment.TAG)
                .commit();

        setDrawerState(false);
        setToolbarState(false, null);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search");
        }

        floatingActionButton.hide();
        getFragmentManager().executePendingTransactions();
    }

    public void setUpNavigationView() {

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


    @Override
    public void onStart() {
        super.onStart();
        allSubscriptions = new CompositeSubscription();
        loadUsers();
    }

    @Override
    public void onStop() {
        super.onStop();
        drawer.closeDrawer(GravityCompat.START);
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

        if (id == R.id.logout) {

            logOutUser();
        } else if (id == R.id.groups_activity) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    launchGroupsActivity();
                }
            }, 300);

        } else if (id == R.id.friends_activity) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    launchFriendsActivity();
                }
            }, 300);

        }
        return true;
    }

    private void launchGroupsActivity() {

        Log.d(TAG, "Launching groups activity");
        Intent intent = new Intent(HomeActivity.this, GroupsActivity.class);
        startActivity(intent);
    }


    private void launchFriendsActivity() {

        Log.d(TAG, "Launching groups activity");
        Intent intent = new Intent(HomeActivity.this, FriendsActivity.class);
        startActivity(intent);
    }

    public void logOutUser() {

        allSubscriptions.add(ParseObservable.logOut()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Logged out successfully");
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Logout failed : " + e);
                        Toast.makeText(HomeActivity.this, "Logout failed : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                })
        );
    }

    private void loadUsers() {

        emptyItemsLayout.setVisibility(View.GONE);
        usersRecyclerAdapter.clear();
        allSubscriptions.add(Observable.fromCallable(
                new Callable<List<ParseUser>>() {
                    @Override
                    public List<ParseUser> call() throws Exception {
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        return query.find();
                    }
                })
                .flatMap(new Func1<List<ParseUser>, Observable<ParseUser>>() {
                    @Override
                    public Observable<ParseUser> call(List<ParseUser> parseUsers) {
                        return Observable.from(parseUsers);
                    }
                })
                .filter(new Func1<ParseUser, Boolean>() {
                    @Override
                    public Boolean call(ParseUser parseUser) {
                        return !parseUser.getObjectId()
                                .equals(ParseUser.getCurrentUser().getObjectId());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseUser>() {
                    @Override
                    public void onCompleted() {

                        if (usersRecyclerAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }

                        if (!swipeRefreshLayout.isRefreshing()) {
                            progressWheel.stopSpinning();
                        } else {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        usersRecyclerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Exception during SignUp : " + e);
                        progressWheel.stopSpinning();
                        Toast.makeText(HomeActivity.this,
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ParseUser parseUser) {
                        usersRecyclerAdapter.addData(parseUser);
                    }

                })
        );
    }

    public boolean handleBackPressed() {

        SearchAndAddFriendFragment searchAndAddFriendFragment =
                (SearchAndAddFriendFragment) getSupportFragmentManager().
                        findFragmentByTag(SearchAndAddFriendFragment.TAG);

        if (searchAndAddFriendFragment == null) {
            return false;
        } else {

            getSupportFragmentManager().beginTransaction()
                    .remove(searchAndAddFriendFragment).commit();
            getSupportFragmentManager().executePendingTransactions();

            setDrawerState(true);
            setToolbarState(true, "Chatter");
            floatingActionButton.show();
            return true;
        }
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

    private void setToolbarState(boolean isEnabled, String title) {

        if (getSupportActionBar() != null) {

            if (isEnabled) {
                getSupportActionBar().setTitle(title);
                getSupportActionBar().show();
            } else {
                getSupportActionBar().hide();
            }
        }
    }

}
