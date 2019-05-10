package app.mycity.mycity.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.views.fragments.UniversalUserListFragment;


public class PlaceSubscribersPagerAdapter extends FragmentStatePagerAdapter {

    String tabName;
    String groupId;

    public PlaceSubscribersPagerAdapter(FragmentManager fm, String name, String groupId) {
        super(fm);
        tabName = name;
        this.groupId = groupId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return UniversalUserListFragment.createInstance(tabName, groupId, Constants.KEY_PLACE_SUBSCRIBERS);
            case 1:
                return UniversalUserListFragment.createInstance(tabName, groupId, Constants.KEY_PLACE_ONLINE_SUBSCRIBERS);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Все";
            case 1:
                return "Мои подписки";
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
