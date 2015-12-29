package com.nuron.chatter.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.nuron.chatter.Activities.HomeActivity;
import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.Adapters.SearchAndAddFriendAdapter;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.R;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by nuron on 26/12/15.
 */
public class SearchAndAddFriendFragment extends Fragment {

    public static final String TAG = SearchAndAddFriendFragment.class.getSimpleName();

    @Bind(R.id.search_users_edit_text)
    EditText searchUsersText;

    @Bind(R.id.users_recycler_view)
    RecyclerView usersRecyclerView;

    @Bind(R.id.search_result_text)
    TextView searchResultText;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    CompositeSubscription allSubscriptions;
    SearchAndAddFriendAdapter searchAndAddFriendAdapter;
    Subscription searchQuerySub;

    List<ParseUser> allUsers = new ArrayList<>();
    List<ParseUser> searchUsers = new ArrayList<>();

    public SearchAndAddFriendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_and_add_friends,
                container, false);
        ButterKnife.bind(this, rootView);

        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchAndAddFriendAdapter = new SearchAndAddFriendAdapter(getActivity(), this);
        usersRecyclerView.setAdapter(searchAndAddFriendAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (allSubscriptions == null) {
            allSubscriptions = new CompositeSubscription();
        }

        searchTextListener();
        loadAllUsers();
    }


    @Override
    public void onStop() {
        super.onStop();

        if (allSubscriptions != null && !allSubscriptions.isUnsubscribed()) {

            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
    }

    @OnClick(R.id.search_clear_button)
    public void clearSearch() {
        searchUsersText.setText("");
        searchResultText.setText("Showing all users");
        loadCachedUsers();
    }


    @OnClick(R.id.home_back_button)
    public void closeSearch() {
        ((HomeActivity) getActivity()).handleBackPressed();
    }


    private void loadCachedUsers() {

        if (allUsers.size() > 0) {
            searchAndAddFriendAdapter.clear();
            for (ParseUser parseUser : allUsers) {
                searchAndAddFriendAdapter.addData(parseUser);
            }
            searchAndAddFriendAdapter.notifyDataSetChanged();
        }
    }

    private void loadAllUsers() {

        progressWheel.setVisibility(View.VISIBLE);
        allUsers.clear();
        searchAndAddFriendAdapter.clear();
        searchResultText.setText("Loading all users");
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

                        if (searchAndAddFriendAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }

                        searchResultText.setText("Showing all users");

                        progressWheel.setVisibility(View.GONE);

                        searchAndAddFriendAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Exception during loading users : " + e);
                        progressWheel.setVisibility(View.GONE);
                        Toast.makeText(getActivity(),
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ParseUser parseUser) {
                        allUsers.add(parseUser);
                        searchAndAddFriendAdapter.addData(parseUser);
                    }

                })
        );
    }

    private void searchTextListener() {

        allSubscriptions.add(RxTextView.textChangeEvents(searchUsersText)
                .debounce(100, TimeUnit.MILLISECONDS)
                .filter(new Func1<TextViewTextChangeEvent, Boolean>() {
                    @Override
                    public Boolean call(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return textViewTextChangeEvent.text().length() > 2;
                    }
                })
                .map(new Func1<TextViewTextChangeEvent, String>() {
                    @Override
                    public String call(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return textViewTextChangeEvent.text().toString().toLowerCase();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String searchQuery) {

                        searchResultText.setText("Searching for " + searchQuery);

                        searchUsers(searchQuery);
                    }
                })
        );
    }

    private void searchUsers(String searchQuery) {

        Log.d(TAG, "Firing search query for : " + searchQuery);

        if (searchAndAddFriendAdapter.getItemCount() > 0) {
            searchAndAddFriendAdapter.clear();
        }

        if (progressWheel.getVisibility() == View.GONE) {
            progressWheel.setVisibility(View.VISIBLE);
        }

        ParseQuery<ParseUser> nameQuery = ParseUser.getQuery();
        nameQuery.whereContains(LoginActivity.USER_ACCOUNT_NAME_LOWER_CASE, searchQuery);

        ParseQuery<ParseUser> emailQuery = ParseUser.getQuery();
        emailQuery.whereContains(LoginActivity.USER_PERSONAL_EMAIL, searchQuery);

        List<ParseQuery<ParseUser>> queries = new ArrayList<>();
        queries.add(nameQuery);
        queries.add(emailQuery);

        final ParseQuery<ParseUser> messageQuery = ParseQuery.or(queries);
        messageQuery.setLimit(50);

        if (searchQuerySub != null && !searchQuerySub.isUnsubscribed()) {

            searchQuerySub.unsubscribe();
            searchQuerySub = null;
        }

        searchQuerySub = Observable.fromCallable(
                new Func0<List<ParseUser>>() {
                    @Override
                    public List<ParseUser> call() {
                        try {
                            return messageQuery.find();
                        } catch (ParseException e) {
                            e.printStackTrace();
                            throw new SecurityException("Query failed : " + e.getMessage());
                        }
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseUser>() {
                    @Override
                    public void onCompleted() {

                        if (searchAndAddFriendAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }

                        searchResultText.setText(searchAndAddFriendAdapter.getItemCount() +
                                " users found");

                        progressWheel.setVisibility(View.GONE);
                        searchAndAddFriendAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                        Log.d(TAG, "Exception during searching users : " + e);
                        progressWheel.setVisibility(View.GONE);
                        Toast.makeText(getActivity(),
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ParseUser parseUser) {
                        searchAndAddFriendAdapter.addData(parseUser);
                    }
                });
        allSubscriptions.add(searchQuerySub);
    }


    public void addFriend(int position) {

        ParseUser friendUser = searchAndAddFriendAdapter.getItemAtPos(position);
        if (friendUser != null) {

            ParseUser currentUser = ParseUser.getCurrentUser();

            ParseFriend parseFriend = new ParseFriend();

            parseFriend.setUserId(currentUser.getObjectId());
            parseFriend.setUserName(currentUser.getString(LoginActivity.USER_ACCOUNT_NAME));
            parseFriend.setUserNameLowercase(currentUser.
                    getString(LoginActivity.USER_ACCOUNT_NAME).toLowerCase());


            parseFriend.setFriendId(friendUser.getObjectId());
            parseFriend.setFriendName(friendUser.getString(LoginActivity.USER_ACCOUNT_NAME));
            parseFriend.setFriendNameLowerCase(friendUser.
                    getString(LoginActivity.USER_ACCOUNT_NAME).toLowerCase());


            ParseACL acl = new ParseACL();
            acl.setReadAccess(ParseUser.getCurrentUser(), true);
            acl.setReadAccess(friendUser.getObjectId(), true);
            acl.setWriteAccess(ParseUser.getCurrentUser(), true);
            acl.setWriteAccess(friendUser.getObjectId(), true);

            parseFriend.setACL(acl);

            allSubscriptions.add(ParseObservable.save(parseFriend)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<ParseFriend>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "ParseFriend added");
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(ParseFriend parseFriend) {
                        }
                    })
            );
        }
    }
}
