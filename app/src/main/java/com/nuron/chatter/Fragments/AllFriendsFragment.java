package com.nuron.chatter.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nuron.chatter.Adapters.SearchAddFriendAdapter;
import com.nuron.chatter.Adapters.UsersRecyclerAdapter;
import com.nuron.chatter.LocalModel.LocalFriend;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.R;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by nuron on 29/12/15.
 */
public class AllFriendsFragment extends Fragment {

    public static final String TAG = AllFriendsFragment.class.getSimpleName();

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    CompositeSubscription allSubscriptions;
    SearchAddFriendAdapter searchAddFriendAdapter;

    UsersRecyclerAdapter usersRecyclerAdapter;

    String currentUserId = ParseUser.getCurrentUser().getObjectId();

    public AllFriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_friends, container, false);

        ButterKnife.bind(this, rootView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        usersRecyclerAdapter = new UsersRecyclerAdapter(getActivity());
        recyclerView.setAdapter(usersRecyclerAdapter);
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loadAllFriends();
                //loadUsers();
            }
        });

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (allSubscriptions == null) {
            allSubscriptions = new CompositeSubscription();
        }
        //loadUsers();

        loadAllFriends();
    }


    @Override
    public void onStop() {
        super.onStop();

        if (allSubscriptions != null && !allSubscriptions.isUnsubscribed()) {

            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
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
                        Toast.makeText(getActivity(),
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ParseUser parseUser) {
                        usersRecyclerAdapter.addData(parseUser);
                    }

                })
        );
    }

    public void loadAllFriends() {

        ParseQuery<ParseFriend> friendRequestSentQuery = ParseQuery.getQuery(ParseFriend.class);
        friendRequestSentQuery.whereEqualTo(ParseFriend.USER_ID, ParseUser.getCurrentUser().getObjectId());

        ParseQuery<ParseFriend> friendRequestReceivedQuery = ParseQuery.getQuery(ParseFriend.class);
        friendRequestReceivedQuery.whereEqualTo(ParseFriend.FRIEND_ID, ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ParseFriend>> queries = new ArrayList<>();
        queries.add(friendRequestSentQuery);
        queries.add(friendRequestReceivedQuery);

        final ParseQuery<ParseFriend> allFriendsQuery = ParseQuery.or(queries);
        allFriendsQuery.addAscendingOrder("createdAt");

        allSubscriptions.add(Observable.fromCallable(
                new Callable<List<ParseFriend>>() {
                    @Override
                    public List<ParseFriend> call() throws Exception {
                        return allFriendsQuery.find();
                    }
                })
                .flatMap(new Func1<List<ParseFriend>, Observable<LocalFriend>>() {
                    @Override
                    public Observable<LocalFriend> call(List<ParseFriend> parseFriends) {
                        return Observable.from(getLocalFriends(parseFriends));
                    }
                })
                .subscribe(new Subscriber<LocalFriend>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(LocalFriend localFriend) {
                        Log.d(TAG, "Friend : " + localFriend.getFriendName());
                    }
                })
        );
    }


    private List<LocalFriend> getLocalFriends(List<ParseFriend> parseFriends) {

        List<LocalFriend> localFriendList = new ArrayList<>();

        for (ParseFriend parseFriend : parseFriends) {
            LocalFriend localFriend = new LocalFriend(parseFriend);

            if (parseFriend.getUserId().equals(currentUserId)) {
                localFriend.setFriendId(parseFriend.getFriendId());
                localFriend.setFriendName(parseFriend.getFriendName());
                localFriend.setFriendNameLowerCase(
                        parseFriend.getFriendNameLowerCase());
            } else {
                localFriend.setFriendId(parseFriend.getUserId());
                localFriend.setFriendName(parseFriend.getUserName());
                localFriend.setFriendNameLowerCase(
                        parseFriend.getUserNameLowercase());
            }

            localFriendList.add(localFriend);
        }

        Collections.sort(localFriendList, new Comparator<LocalFriend>() {
            @Override
            public int compare(final LocalFriend localFriend1,
                               final LocalFriend localFriend2) {
                return localFriend1.getFriendNameLowerCase().
                        compareTo(localFriend2.getFriendNameLowerCase());
            }
        });

        return localFriendList;
    }

}
