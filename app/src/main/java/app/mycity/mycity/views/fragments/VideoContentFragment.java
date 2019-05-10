package app.mycity.mycity.views.fragments;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import app.mycity.mycity.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoContentFragment extends android.support.v4.app.Fragment {

    @BindView(R.id.videoView)
    SimpleExoPlayerView playerView;

    SimpleExoPlayer player;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_content_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onPause() {
        player.stop();
        player.release();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(getContext()),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        playerView.setPlayer(player);
        playerView.setShutterBackgroundColor(Color.TRANSPARENT);
        player.setPlayWhenReady(false);
        MediaSource mediaSource = buildMediaSource(Uri.parse(getArguments().getString("link")));
        player.prepare(mediaSource);

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                 if(playWhenReady && playbackState == 2){
                }
                if(playWhenReady && playbackState == 3){
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            }

            @Override
            public void onSeekProcessed() {
            }
        });
    }




    public static VideoContentFragment createInstance(String link) {
        VideoContentFragment fragment = new VideoContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("link", link);
        fragment.setArguments(bundle);
        return fragment;
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    public void stopPlaying() {
        if(player!=null){
            player.stop();
            player.release();
        }
    }

    public void losingVisibility() {
        if(player!=null){
            player.setPlayWhenReady(false);
            player.seekTo(0);
            player.getPlaybackState();
        }
    }

    /**
     * This method is only used by viewpager because the viewpager doesn't call onPause after
     * changing the fragment
     */
    public void gainVisibility() {
        // IMPLEMENT YOUR RESUME CODE HERE
        Log.d("TAG24", " gain visibility " + Uri.parse(getArguments().getString("link")));
        player.seekTo(0);
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
}
