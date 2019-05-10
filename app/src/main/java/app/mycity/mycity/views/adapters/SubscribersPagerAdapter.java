package app.mycity.mycity.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.views.fragments.UniversalUserListFragment;


public class SubscribersPagerAdapter extends FragmentStatePagerAdapter {

    String tabName;
    String userId;

    public SubscribersPagerAdapter(FragmentManager fm, String name, String userId) {
        super(fm);
        tabName = name;
        this.userId = userId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return UniversalUserListFragment.createInstance(tabName, userId, Constants.KEY_SUBSCRIBERS);
            case 1:
                return UniversalUserListFragment.createInstance(tabName, userId, Constants.KEY_SUBSCRIBERS_ONLINE);
        }
        return null;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Все";
            case 1:
                return "Online";
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
