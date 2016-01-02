package com.nuron.chatter.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.nuron.chatter.Fragments.AllFriendsFragment;
import com.nuron.chatter.Fragments.FriendRequestsFragment;

/**
 * Created by nuron on 02/01/16.
 */
public class FriendsFragmentPagerAdapter extends FragmentPagerAdapter {

    public static final String TAG = FriendsFragmentPagerAdapter.class.getSimpleName();

    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"All", "Requests",};

    public FriendsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {

        if(position == 0){
            return new AllFriendsFragment();
        } else {
            return new FriendRequestsFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
