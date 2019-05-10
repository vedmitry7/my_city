package app.mycity.mycity.views.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Group;
import app.mycity.mycity.api.model.Likes;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponseLike;
import app.mycity.mycity.util.BitmapUtils;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.CheckinSliderAdapter;
import app.mycity.mycity.views.adapters.FeedPlacesCheckinRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileCheckinContent extends android.support.v4.app.Fragment implements TabStacker.TabStackInterface {

    LinearLayoutManager mLayoutManager;

    @BindView(R.id.placesFragmentCheckinRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.feedPhoto)
    ImageView photo;

    @BindView(R.id.commentButton)
    ImageView commentButton;

    @BindView(R.id.likeIcon)
    ImageView likeIcon;

    @BindView(R.id.feedName)
    TextView name;

    @BindView(R.id.feedPostTime)
    TextView time;

    @BindView(R.id.commentsCount)
    TextView commentsCount;

    @BindView(R.id.likesCount)
    TextView likesCount;

    @BindView(R.id.placeName)
    TextView placeName;

    @BindView(R.id.constraintLayout6)
    ConstraintLayout buttonsContainer;

    @BindView(R.id.progressBarPlaceHolder)
    ConstraintLayout placeHolder;

    @BindView(R.id.numeration)
    TextView numeration;

    @BindView(R.id.pager)
    ViewPager viewPager;

    List<Post> postList;
    HashMap<String, Group> groups;

    FeedPlacesCheckinRecyclerAdapter adapter;

    String currentPostId;
    String currentOwnerId;
    String currentPlaceId;
    int currentPostIdPosition = -1;

    EventBusMessages.OpenPlacePhoto2 event;

    boolean isLoading;
    int totalCount;

    Storage storage;
    private boolean mayRestore;

    boolean clearScreen;

    Profile profile;

    public static ProfileCheckinContent createInstance(String name, String postId, String storageKey) {
        ProfileCheckinContent fragment = new ProfileCheckinContent();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("postId", postId);
        bundle.putString("storageKey", storageKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_checkin_content, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        EventBus.getDefault().post(new EventBusMessages.BlackStatusBar());


        View.OnClickListener openPlaceListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenPlace(currentPlaceId));
            }
        };
        name.setOnClickListener(openPlaceListener);
        photo.setOnClickListener(openPlaceListener);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenComments(currentPostId, currentOwnerId, "post"));
            }
        });

        mLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new FeedPlacesCheckinRecyclerAdapter(postList);
        recyclerView.setAdapter(adapter);

        CheckinSliderAdapter checkinSliderAdapter = new CheckinSliderAdapter(getContext(), postList);
        viewPager.setAdapter(checkinSliderAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setNumeration(position);
                openPlace(new EventBusMessages.ShowImage(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        for (int i = 0; i < postList.size(); i++) {
            if(postList.get(i).getId().equals(getArguments().getString("postId"))){
                currentPostIdPosition = i;
                openPlace(new EventBusMessages.ShowImage(i));
            }
        }

        placeHolder.setVisibility(View.GONE);

        placeName.setText(profile.getFirstName() + " " + profile.getLastName());

    }

    @OnClick(R.id.backButton)
    public void back(View v){
        getActivity().onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void clickImage(EventBusMessages.ClickOnSliderImage event) {

        if(clearScreen){
            //    recyclerView.setVisibility(View.VISIBLE);
            buttonsContainer.animate().setDuration(200).translationYBy(-recyclerView.getLayoutParams().height);
            recyclerView.animate().setDuration(200).translationYBy(-recyclerView.getHeight());
            clearScreen = false;
        } else {

            buttonsContainer.animate().setDuration(200).translationY(recyclerView.getLayoutParams().height);
            recyclerView.animate().setDuration(200).translationY(recyclerView.getHeight());
            //recyclerView.setVisibility(View.GONE);
            clearScreen = true;
        }

    }

    private void setNumeration(int current){
        numeration.setText((current+1)+"/"+totalCount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openPlace(EventBusMessages.ShowImage event){
        currentPostIdPosition = event.getPosition();
        viewPager.setCurrentItem(event.getPosition());

        if(groups.containsKey(postList.get(currentPostIdPosition).getPlaceId())){
            name.setText(groups.get(postList.get(currentPostIdPosition).getPlaceId()).getName());
            Picasso.get().load(groups.get(postList.get(currentPostIdPosition).getPlaceId()).getPhoto130()).into(photo);
        }
        likesCount.setText(String.valueOf(postList.get(currentPostIdPosition).getLikes().getCount()));
        setLiked(postList.get(currentPostIdPosition).getLikes().getCount()==1);

        time.setText(Util.getDatePretty(postList.get(currentPostIdPosition).getDate()));

        currentOwnerId = postList.get(event.getPosition()).getOwnerId();
        currentPostId = postList.get(event.getPosition()).getId();
        currentPlaceId = postList.get(event.getPosition()).getPlaceId();

        setLiked(postList.get(event.getPosition()).getLikes().getUserLikes()==1);
        commentsCount.setText(String.valueOf(postList.get(event.getPosition()).getComments().getCount()));
    }

    void setLiked(boolean b){
        if(b){
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_vector_white));
        } else {
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline_vector_white));
        }
    }

    @OnClick(R.id.likeIcon)
    public void like(View v) {
        final Post post;

        if(currentPostIdPosition == -1){
            post = event.getPost();
        } else {
            post = postList.get(currentPostIdPosition);
        }

        if (post.getLikes().getUserLikes() == 1) {
            post.getLikes().setCount(post.getLikes().getCount()-1);
            post.getLikes().setUserLikes(0);
            setLiked(false);
            likesCount.setText(String.valueOf(post.getLikes().getCount()));
            setRightValue(post);
            ApiFactory.getApi().unlike(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "post",
                    post.getId().toString(),
                    post.getOwnerId().toString()
            ).enqueue(new Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseLike>> call, Response<ResponseContainer<ResponseLike>> response) {
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
            return;
        }

        if (post.getLikes().getUserLikes() == 0) {

            post.getLikes().setCount(post.getLikes().getCount()+1);
            post.getLikes().setUserLikes(1);
            setLiked(true);
            likesCount.setText(String.valueOf(post.getLikes().getCount()));
            setRightValue(post);
            ApiFactory.getApi().like(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "post",
                    post.getId().toString(),
                    post.getOwnerId().toString()
            ).enqueue(new Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseLike>> call, Response<ResponseContainer<ResponseLike>> response) {
                    if (response != null && response.body() != null) {
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }
    }

    private void setRightValue(Post post) {
        if(currentPostIdPosition == -1){
            for (Post p:postList
                    ) {
                if(p.getId() == post.getId()){
                    Likes likes = new Likes();
                    likes.setCount(post.getLikes().getCount());
                    p.getLikes().setCount(post.getLikes().getCount());
                    p.getLikes().setUserLikes(post.getLikes().getUserLikes());
                }
            }
            adapter.update(postList);
        }
    }

    @OnClick(R.id.menuButton)
    public void menu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.content_menu);
        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.complain:
                                return true;
                            case R.id.copy:
                                return true;
                            case R.id.share:
                                return true;
                            case R.id.save:
                                BitmapUtils.downloadFile(postList.get(currentPostIdPosition).getAttachments().get(0).getPhotoOrig(), getContext());
                                return true;
                        }
                        return true;
                    }
                });

        popupMenu.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
        postList = (List<Post>) storage.getDate(getArguments().get("storageKey")+ "checkins");
        totalCount = postList.size();
        groups = (HashMap<String, Group>) storage.getDate(getArguments().getString("storageKey") + "groups");
        profile = (Profile) storage.getDate(getArguments().getString("storageKey") + "profile");
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason presentReason) {

    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason dismissReason) {
        if(dismissReason == TabStacker.DismissReason.REPLACED){
            storage.setDate(getArguments().get("name") + "_postList", postList);
        }
        if(dismissReason == TabStacker.DismissReason.BACK){
            storage.remove("");
        }
    }

    @Override
    public View onSaveTabFragmentInstance(Bundle bundle) {
        return null;
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle bundle) {

    }
}
