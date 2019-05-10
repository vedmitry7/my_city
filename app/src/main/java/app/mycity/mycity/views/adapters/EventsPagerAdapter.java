package app.mycity.mycity.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import app.mycity.mycity.views.fragments.events.AllActions;
import app.mycity.mycity.views.fragments.events.AllEvents;
import app.mycity.mycity.views.fragments.feed.FeedPhotoAlbumFragment;

public class EventsPagerAdapter extends FragmentPagerAdapter {

    String tabName;
    AllEvents allEventsFragment;
    AllActions allActionsFragment;

    public EventsPagerAdapter(FragmentManager fm, String tabName) {
        super(fm);
        this.tabName = tabName;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                allEventsFragment = AllEvents.createInstance(tabName, 4);
                return allEventsFragment;
            case 1:
                allActionsFragment = AllActions.createInstance(tabName, 4);
                return allActionsFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "События";
            case 1:
                return "Акции";
        }
        return null;
    }

    public AllEvents getAllEventsFragment() {
        return allEventsFragment;
    }

    public AllActions getAllActionsFragment() {
        return allActionsFragment;
    }
}
