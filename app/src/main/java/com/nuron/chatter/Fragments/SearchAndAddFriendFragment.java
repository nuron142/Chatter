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

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.nuron.chatter.Activities.HomeActivity;
import com.nuron.chatter.Activities.LoginActivity;
import com.nuron.chatter.Adapters.SearchAddFriendAdapter;
import com.nuron.chatter.LocalModel.SearchUser;
import com.nuron.chatter.Model.ParseFriend;
import com.nuron.chatter.Model.ParseFriendRequest;
import com.nuron.chatter.R;
import com.nuron.chatter.ViewHolders.SearchAddFriendViewHolder;
import com.parse.ParseACL;
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
import rx.functions.Func1;
import rx.functions.Func2;
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
    SearchAddFriendAdapter searchAddFriendAdapter;
    Subscription searchQuerySub;

    List<SearchUser> allUsers = new ArrayList<>();

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

        searchAddFriendAdapter = new SearchAddFriendAdapter(getActivity(), this);
        usersRecyclerView.setAdapter(searchAddFriendAdapter);

        searchTextListener();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (allSubscriptions == null) {
            allSubscriptions = new CompositeSubscription();
        }

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
        loadCachedUsers();
    }


    @OnClick(R.id.home_back_button)
    public void closeSearch() {
        ((HomeActivity) getActivity()).handleBackPressed();
    }


    private void loadCachedUsers() {

        if (allUsers.size() > 0) {

            searchResultText.setText("Showing all users");
            searchAddFriendAdapter.clear();
            for (SearchUser searchUser : allUsers) {
                searchAddFriendAdapter.addData(searchUser);
            }
            searchAddFriendAdapter.notifyDataSetChanged();
        }
    }

    private void loadAllUsers() {

        progressWheel.setVisibility(View.VISIBLE);
        allUsers.clear();
        searchAddFriendAdapter.clear();
        searchResultText.setText("Loading all users");

        ParseQuery<ParseFriendRequest> friendRequestSentQuery = ParseQuery.getQuery(ParseFriendRequest.class);
        friendRequestSentQuery.whereEqualTo(ParseFriendRequest.USER_ID, ParseUser.getCurrentUser().getObjectId());

        ParseQuery<ParseFriendRequest> friendRequestReceivedQuery = ParseQuery.getQuery(ParseFriendRequest.class);
        friendRequestReceivedQuery.whereEqualTo(ParseFriend.FRIEND_ID, ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ParseFriendRequest>> queries = new ArrayList<>();
        queries.add(friendRequestSentQuery);
        queries.add(friendRequestReceivedQuery);

        final ParseQuery<ParseFriendRequest> allFriendsQuery = ParseQuery.or(queries);
        allFriendsQuery.addAscendingOrder(ParseFriend.FRIEND_ID);

        Observable friendQueryObservable = Observable.fromCallable(
                new Callable<List<ParseFriendRequest>>() {
                    @Override
                    public List<ParseFriendRequest> call() throws Exception {
                        return allFriendsQuery.find();
                    }
                });

        final ParseQuery<ParseUser> allUsersQuery = ParseUser.getQuery();
        allUsersQuery.addAscendingOrder(ParseFriend.OBJECT_ID);

        Observable allUsersQueryObservable = Observable.fromCallable(
                new Callable<List<ParseUser>>() {
                    @Override
                    public List<ParseUser> call() throws Exception {
                        return allUsersQuery.find();
                    }
                });


        allSubscriptions.add(Observable.zip(allUsersQueryObservable, friendQueryObservable,
                new Func2<List<ParseUser>, List<ParseFriendRequest>, List<SearchUser>>() {

                    @Override
                    public List<SearchUser> call(List<ParseUser> parseUsers,
                                                 List<ParseFriendRequest> parseFriendRequests) {
                        return getSearchUsers(parseUsers, parseFriendRequests);
                    }
                })
                .flatMap(new Func1<List<SearchUser>, Observable<SearchUser>>() {

                    @Override
                    public Observable<SearchUser> call(List<SearchUser> searchUsers) {
                        return Observable.from(searchUsers);
                    }
                })
                .filter(new Func1<SearchUser, Boolean>() {
                    @Override
                    public Boolean call(SearchUser searchUser) {
                        return !searchUser.getParseUser().getObjectId()
                                .equals(ParseUser.getCurrentUser().getObjectId());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SearchUser>() {
                    @Override
                    public void onCompleted() {
                        if (searchAddFriendAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }

                        searchResultText.setText("Showing all users");

                        progressWheel.setVisibility(View.GONE);
                        searchAddFriendAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SearchUser searchUser) {

                        allUsers.add(searchUser);
                        searchAddFriendAdapter.addData(searchUser);

                    }
                })
        );

    }

    private void searchTextListener() {

        if (allSubscriptions == null) {
            allSubscriptions = new CompositeSubscription();
        }

        allSubscriptions.add(RxTextView.textChangeEvents(searchUsersText)
                .debounce(100, TimeUnit.MILLISECONDS)
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

                        if (searchQuery.isEmpty()) {
                            loadCachedUsers();
                        }

                        if (searchQuery.length() < 2) {
                            return;
                        }

                        searchResultText.setText("Searching for '" + searchQuery + "'");

                        searchUsers(searchQuery);
                    }
                })
        );
    }

    private void searchUsers(final String searchQuery) {

        if (searchAddFriendAdapter.getItemCount() > 0) {
            searchAddFriendAdapter.clear();
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
        messageQuery.addAscendingOrder(ParseFriend.OBJECT_ID);
        messageQuery.setLimit(50);

        if (searchQuerySub != null && !searchQuerySub.isUnsubscribed()) {

            searchQuerySub.unsubscribe();
            searchQuerySub = null;
        }


        ParseQuery<ParseFriendRequest> friendRequestSentQuery = ParseQuery.getQuery(ParseFriendRequest.class);
        friendRequestSentQuery.whereEqualTo(ParseFriendRequest.USER_ID, ParseUser.getCurrentUser().getObjectId());

        ParseQuery<ParseFriendRequest> friendRequestReceivedQuery = ParseQuery.getQuery(ParseFriendRequest.class);
        friendRequestReceivedQuery.whereEqualTo(ParseFriend.FRIEND_ID, ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ParseFriendRequest>> queries1 = new ArrayList<>();
        queries1.add(friendRequestSentQuery);
        queries1.add(friendRequestReceivedQuery);

        final ParseQuery<ParseFriendRequest> allFriendsQuery = ParseQuery.or(queries1);
        allFriendsQuery.addAscendingOrder(ParseFriend.FRIEND_ID);


        Observable friendQueryObservable = Observable.fromCallable(
                new Callable<List<ParseFriendRequest>>() {
                    @Override
                    public List<ParseFriendRequest> call() throws Exception {
                        return allFriendsQuery.find();
                    }
                });

        Observable searchUsersObservable = Observable.fromCallable(
                new Callable<List<ParseUser>>() {
                    @Override
                    public List<ParseUser> call() throws Exception {
                        return messageQuery.find();
                    }
                });


        searchQuerySub = Observable.zip(searchUsersObservable, friendQueryObservable,
                new Func2<List<ParseUser>, List<ParseFriendRequest>, List<SearchUser>>() {

                    @Override
                    public List<SearchUser> call(List<ParseUser> parseUsers,
                                                 List<ParseFriendRequest> parseFriendRequests) {
                        return getSearchUsers(parseUsers, parseFriendRequests);
                    }
                })
                .flatMap(new Func1<List<SearchUser>, Observable<SearchUser>>() {

                    @Override
                    public Observable<SearchUser> call(List<SearchUser> searchUsers) {
                        return Observable.from(searchUsers);
                    }
                })
                .filter(new Func1<SearchUser, Boolean>() {
                    @Override
                    public Boolean call(SearchUser searchUser) {
                        return !searchUser.getParseUser().getObjectId()
                                .equals(ParseUser.getCurrentUser().getObjectId());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SearchUser>() {
                    @Override
                    public void onCompleted() {
                        if (searchAddFriendAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyItemsLayout.setVisibility(View.GONE);
                        }

                        searchResultText.setText(searchAddFriendAdapter.getItemCount() +
                                " users found for '" + searchQuery + "'");

                        progressWheel.setVisibility(View.GONE);
                        searchAddFriendAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(SearchUser searchUser) {
                        searchAddFriendAdapter.addData(searchUser);
                    }
                });

        allSubscriptions.add(searchQuerySub);
    }

    private List<SearchUser> getSearchUsers(List<ParseUser> parseUsers,
                                            List<ParseFriendRequest> parseFriendRequests) {

        List<SearchUser> searchUserList = new ArrayList<>();

        for (ParseUser parseUser : parseUsers) {

            SearchUser searchUser = new SearchUser(parseUser);
            String searchUserObjectId = parseUser.getObjectId();

            for (ParseFriendRequest parseFriendRequest : parseFriendRequests) {

                if (parseFriendRequest.getFriendId().equals(searchUserObjectId) || parseFriendRequest.getUserId().equals(searchUserObjectId)) {

                    searchUser.setIsRequestSent(parseFriendRequest.getRequestSent());
                    searchUser.setIsRequestAccepted(parseFriendRequest.getRequestAccepted());
                    break;
                }
            }

            searchUserList.add(searchUser);
        }

        return searchUserList;

    }

    public void sendFriendRequest(final SearchAddFriendViewHolder searchAddFriendViewHolder,
                                  int position) {

        final ParseUser friendUser = searchAddFriendAdapter.getItemAtPos(position).getParseUser();
        if (friendUser != null) {

            searchAddFriendViewHolder.addFriendImage.setVisibility(View.GONE);
            searchAddFriendViewHolder.addFriendProgress.spin();

            ParseFriendRequest parseFriendRequest = new ParseFriendRequest();

            parseFriendRequest.setUserId(ParseUser.getCurrentUser().getObjectId());
            parseFriendRequest.setUserName(ParseUser.getCurrentUser().
                    getString(LoginActivity.USER_ACCOUNT_NAME));
            parseFriendRequest.setUserNameLowercase(ParseUser.getCurrentUser().
                    getString(LoginActivity.USER_ACCOUNT_NAME).toLowerCase());
            parseFriendRequest.setUserEmail(ParseUser.getCurrentUser().
                    getString(LoginActivity.USER_PERSONAL_EMAIL));

            parseFriendRequest.setFriendId(friendUser.getObjectId());
            parseFriendRequest.setFriendName(friendUser.
                    getString(LoginActivity.USER_ACCOUNT_NAME));
            parseFriendRequest.setFriendNameLowerCase(friendUser.
                    getString(LoginActivity.USER_ACCOUNT_NAME).toLowerCase());
            parseFriendRequest.setFriendEmail(friendUser.
                    getString(LoginActivity.USER_PERSONAL_EMAIL));


            parseFriendRequest.setRequestSent(ParseFriendRequest.STRING_TRUE);
            parseFriendRequest.setRequestAccepted(ParseFriendRequest.STRING_FALSE);

            ParseACL acl = new ParseACL();
            acl.setReadAccess(ParseUser.getCurrentUser(), true);
            acl.setReadAccess(friendUser.getObjectId(), true);
            acl.setWriteAccess(ParseUser.getCurrentUser(), true);
            acl.setWriteAccess(friendUser.getObjectId(), true);

            parseFriendRequest.setACL(acl);

            allSubscriptions.add(ParseObservable.save(parseFriendRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<ParseFriendRequest>() {
                        @Override
                        public void onCompleted() {

                            searchAddFriendViewHolder.addFriendProgress.stopSpinning();
                            searchAddFriendViewHolder.addFriendImage.setVisibility(View.GONE);
                            searchAddFriendViewHolder.friendRequestAcceptedImage.setVisibility(View.VISIBLE);

                            Log.d(TAG, "ParseFriendRequest sent");
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(ParseFriendRequest parseFriendRequest) {
                        }
                    })
            );
        }
    }
}
