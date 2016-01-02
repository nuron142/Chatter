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

import com.nuron.chatter.Adapters.AllFriendsAdapter;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.R;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

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
    AllFriendsAdapter allFriendsAdapter;

    public AllFriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_friends, container, false);

        ButterKnife.bind(this, rootView);

        allFriendsAdapter = new AllFriendsAdapter(getActivity(), this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(allFriendsAdapter);
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loadAllFriends();
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

    private void loadAllFriends() {

        Log.d(TAG, "Loading all friends");

        emptyItemsLayout.setVisibility(View.GONE);
        allFriendsAdapter.clear();

        final ParseQuery<ParseFriend> allFriendsQuery = ParseQuery.getQuery(ParseFriend.class);
        allFriendsQuery.whereEqualTo(ParseFriend.USER_ID,
                ParseUser.getCurrentUser().getObjectId());
        allFriendsQuery.addAscendingOrder(ParseFriend.FRIEND_NAME_LOWER_CASE);

        allSubscriptions.add(Observable.fromCallable(
                new Callable<List<ParseFriend>>() {
                    @Override
                    public List<ParseFriend> call() throws Exception {
                        return allFriendsQuery.find();
                    }
                })
                .flatMap(new Func1<List<ParseFriend>, Observable<ParseFriend>>() {
                    @Override
                    public Observable<ParseFriend> call(List<ParseFriend> parseFriends) {
                        Log.d(TAG, "parseFriends size : " + parseFriends.size());
                        return Observable.from(parseFriends);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseFriend>() {
                    @Override
                    public void onCompleted() {

                        if (allFriendsAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }

                        if (!swipeRefreshLayout.isRefreshing()) {
                            progressWheel.stopSpinning();
                        } else {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        allFriendsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ParseFriend parseFriendRequest) {

                        allFriendsAdapter.addData(parseFriendRequest);
                    }
                })
        );
    }

}
