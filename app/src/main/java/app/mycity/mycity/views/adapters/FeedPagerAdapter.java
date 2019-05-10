package app.mycity.mycity.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import app.mycity.mycity.views.fragments.feed.FeedCheckinFragment;

public class FeedPagerAdapter extends FragmentPagerAdapter {

    String tabName;

    FeedCheckinFragment allCheckinFragment;
    FeedCheckinFragment subscriptionsCheckinFragment;

    public FeedPagerAdapter(FragmentManager fm, String tabName) {
        super(fm);
        this.tabName = tabName;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                allCheckinFragment = FeedCheckinFragment.createInstance(tabName, "all");
                return allCheckinFragment;
            case 1:
                subscriptionsCheckinFragment = FeedCheckinFragment.createInstance(tabName, "subscriptions");
                return subscriptionsCheckinFragment;
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
                return "ВСЕ";
            case 1:
                return "ПОДПИСКИ";
        }
        return null;
    }

    public FeedCheckinFragment getAllCheckinFragment() {
        return allCheckinFragment;
    }

    public FeedCheckinFragment getSubscriptionsCheckinFragment() {
        return subscriptionsCheckinFragment;
    }
}
