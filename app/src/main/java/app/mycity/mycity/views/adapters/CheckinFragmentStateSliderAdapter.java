package app.mycity.mycity.views.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.views.fragments.PhotoContentFragment;
import app.mycity.mycity.views.fragments.VideoContentFragment;

public class CheckinFragmentStateSliderAdapter extends FragmentPagerAdapter {


    private SparseArray<VideoContentFragment> mFragmentsHolded = new SparseArray<>();

    Context mContext;
    List<Post> postList;

    public CheckinFragmentStateSliderAdapter(FragmentManager fm, Context context, List<Post> postList) {
        super(fm);
        mContext = context;
        this.postList = postList;
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    @Override
    public Fragment getItem(int position) {
        if(postList.get(position).getAttachments().get(0).getType().equals("video")){
            return VideoContentFragment.createInstance(postList.get(position).getAttachments().get(0).getVideoOrig());
        } else {
            return PhotoContentFragment.createInstance(postList.get(position).getAttachments().get(0).getPhoto780());
        }
    }

    public void update(List<Post> postList){
        this.postList = postList;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        if(fragment instanceof VideoContentFragment) {
            mFragmentsHolded.append(position, (VideoContentFragment) fragment);
        }
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mFragmentsHolded.delete(position);
        super.destroyItem(container, position, object);
    }

    public VideoContentFragment getCachedItem(int position) {
        return mFragmentsHolded.get(position, null);
    }

    public void stopAllVideo(){
        for (int i = 0; i < mFragmentsHolded.size(); i++) {
            if(mFragmentsHolded.get(i)!=null){
                mFragmentsHolded.get(i).stopPlaying();
            }
        }
    }
}
