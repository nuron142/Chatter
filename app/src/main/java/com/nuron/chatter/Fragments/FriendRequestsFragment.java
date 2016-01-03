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

import com.nuron.chatter.Adapters.FriendRequestsAdapter;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.Model.ParseFriendRequest;
import com.nuron.chatter.R;
import com.nuron.chatter.ViewHolders.FriendRequestViewHolder;
import com.parse.ParseACL;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
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
 * Created by nuron on 02/01/16.
 */
public class FriendRequestsFragment extends Fragment {

    public static final String TAG = FriendRequestsFragment.class.getSimpleName();

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    CompositeSubscription allSubscriptions;
    FriendRequestsAdapter friendRequestsAdapter;

    public FriendRequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        ButterKnife.bind(this, rootView);

        friendRequestsAdapter = new FriendRequestsAdapter(getActivity(), this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(friendRequestsAdapter);
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFriendRequests();
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

        loadFriendRequests();
    }


    @Override
    public void onStop() {
        super.onStop();

        if (allSubscriptions != null && !allSubscriptions.isUnsubscribed()) {

            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
    }

    private void loadFriendRequests() {

        Log.d(TAG, "Loading friend requests");

        emptyItemsLayout.setVisibility(View.GONE);
        friendRequestsAdapter.clear();

        final ParseQuery<ParseFriendRequest> friendRequestReceivedQuery =
                ParseQuery.getQuery(ParseFriendRequest.class);
        friendRequestReceivedQuery.whereEqualTo(ParseFriend.FRIEND_ID,
                ParseUser.getCurrentUser().getObjectId());
        friendRequestReceivedQuery.addAscendingOrder(ParseFriendRequest.UPDATED_AT);

        allSubscriptions.add(Observable.fromCallable(
                new Callable<List<ParseFriendRequest>>() {
                    @Override
                    public List<ParseFriendRequest> call() throws Exception {
                        return friendRequestReceivedQuery.find();
                    }
                })
                .flatMap(new Func1<List<ParseFriendRequest>, Observable<ParseFriendRequest>>() {
                    @Override
                    public Observable<ParseFriendRequest> call(
                            List<ParseFriendRequest> parseFriendRequests) {
                        return Observable.from(parseFriendRequests);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseFriendRequest>() {
                    @Override
                    public void onCompleted() {

                        if (friendRequestsAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }

                        if (!swipeRefreshLayout.isRefreshing()) {
                            progressWheel.stopSpinning();
                        } else {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        friendRequestsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ParseFriendRequest parseFriendRequest) {

                        friendRequestsAdapter.addData(parseFriendRequest);
                    }
                })
        );
    }


    public void handleFriendRequest(final FriendRequestViewHolder friendRequestViewHolder,
                                    final int position, boolean accept) {

        final ParseFriendRequest parseFriendRequest = friendRequestsAdapter.getItemAtPos(position);
        if (parseFriendRequest != null) {

            friendRequestViewHolder.friendRequestLayout.setClickable(false);

            ParseACL acl = new ParseACL();
            acl.setReadAccess(parseFriendRequest.getUserId(), true);
            acl.setReadAccess(parseFriendRequest.getFriendId(), true);
            acl.setWriteAccess(parseFriendRequest.getUserId(), true);
            acl.setWriteAccess(parseFriendRequest.getFriendId(), true);

            if (accept) {

                friendRequestViewHolder.acceptFriendImage.setVisibility(View.INVISIBLE);
                friendRequestViewHolder.acceptFriendProgress.spin();

                parseFriendRequest.setRequestAccepted(ParseFriendRequest.STRING_TRUE);

                ParseFriend user = new ParseFriend(parseFriendRequest, true);
                user.setACL(acl);

                ParseFriend friend = new ParseFriend(parseFriendRequest, false);
                friend.setACL(acl);

                final List<ParseFriend> newFriend = new ArrayList<>();
                newFriend.add(user);
                newFriend.add(friend);

                parseFriendRequest.setRequestAccepted(ParseFriendRequest.STRING_TRUE);

                allSubscriptions.add(Observable.fromCallable(
                        new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                ParseFriend.saveAll(newFriend);
                                return null;
                            }
                        })
                        .flatMap(new Func1<Void, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(Void aVoid) {
                                return Observable.fromCallable(new Callable<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        parseFriendRequest.save();
                                        return null;
                                    }
                                });
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Void>() {
                            @Override
                            public void onCompleted() {

                                Log.d(TAG, "ParseFriendRequest accepted");

                                friendRequestViewHolder.acceptFriendProgress.stopSpinning();
                                friendRequestsAdapter.removeData(position);
                                friendRequestsAdapter.notifyItemRemoved(position);

                            }

                            @Override
                            public void onError(Throwable e) {
                                friendRequestViewHolder.acceptFriendProgress.stopSpinning();
                                Toast.makeText(getActivity(), "Could accept request",
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(Void aVoid) {

                            }
                        })
                );

            } else {

                friendRequestViewHolder.rejectFriendImage.setVisibility(View.INVISIBLE);
                friendRequestViewHolder.rejectFriendProgress.spin();

                allSubscriptions.add(Observable.fromCallable(
                        new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                parseFriendRequest.delete();
                                return null;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Void>() {
                            @Override
                            public void onCompleted() {

                                Log.d(TAG, "ParseFriendRequest rejected");

                                friendRequestViewHolder.acceptFriendProgress.stopSpinning();
                                friendRequestsAdapter.removeData(position);
                                friendRequestsAdapter.notifyItemRemoved(position);
                            }

                            @Override
                            public void onError(Throwable e) {
                                friendRequestViewHolder.acceptFriendProgress.stopSpinning();
                                Toast.makeText(getActivity(), "Could reject request",
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(Void aVoid) {

                            }
                        })
                );
            }

        }
    }
}
