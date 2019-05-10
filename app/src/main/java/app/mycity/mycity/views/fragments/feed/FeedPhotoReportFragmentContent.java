package app.mycity.mycity.views.fragments.feed;

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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Album;
import app.mycity.mycity.api.model.Group;
import app.mycity.mycity.api.model.Photo;
import app.mycity.mycity.api.model.PhotoContainer;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponseLike;
import app.mycity.mycity.util.BitmapUtils;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.AlbumSliderAdapter;
import app.mycity.mycity.views.adapters.FeedPhotoReportContentAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedPhotoReportFragmentContent extends android.support.v4.app.Fragment implements TabStacker.TabStackInterface {

    LinearLayoutManager mLayoutManager;

    @BindView(R.id.placesFragmentCheckinRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.feedPhoto)
    ImageView photo;
    @BindView(R.id.pager)
    ViewPager viewPager;
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
    @BindView(R.id.numeration)
    TextView numeration;
    @BindView(R.id.progressBarPlaceHolder)
    ConstraintLayout placeHolder;
    @BindView(R.id.constraintLayout6)
    ConstraintLayout buttonsContainer;
    List<Photo> photoList;
    Group group;
    FeedPhotoReportContentAdapter adapter;
    String placeId;
    String currentPostId;
    int currentPostIdPosition;
    boolean isLoading;
    int totalCount;
    Storage storage;
    private boolean mayRestore;
    boolean clearScreen;

    public static FeedPhotoReportFragmentContent createInstance(String name, String photoId) {
        FeedPhotoReportFragmentContent fragment = new FeedPhotoReportFragmentContent();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("photoId", photoId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @OnClick(R.id.backButton)
    public void backButton(View v){
        getActivity().onBackPressed();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_photo_report_content, container, false);
        placeId = getArguments().getString("placeId");
        ButterKnife.bind(this, view);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void clickImage(EventBusMessages.ClickOnSliderImage event) {
        if(clearScreen){
            buttonsContainer.animate().setDuration(200).translationYBy(-recyclerView.getLayoutParams().height);
            recyclerView.animate().setDuration(200).translationYBy(-recyclerView.getHeight());
            clearScreen = false;
        } else {
            buttonsContainer.animate().setDuration(200).translationY(recyclerView.getLayoutParams().height);
            recyclerView.animate().setDuration(200).translationY(recyclerView.getHeight());
            clearScreen = true;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().post(new EventBusMessages.BlackStatusBar());

        time.setText(Util.getDatePretty(getArguments().getLong("albumDate")));
        placeName.setText(getArguments().getString("albumName"));

        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(horizontalLayoutManager);

        adapter = new FeedPhotoReportContentAdapter(photoList);
        recyclerView.setAdapter(adapter);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = mLayoutManager.getItemCount();
                int lastVisibleItems = mLayoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        isLoading = true;
                        if(totalCount >= photoList.size()){
                            loadMedia(photoList.size());
                        }
                    }
                }
            }
        };
        loadMedia(photoList.size());


        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenComments(currentPostId, photoList.get(currentPostIdPosition).getOwnerId(), "photo"));
            }
        });
    }

    private void loadMedia(final int offset) {

        if(mayRestore){
            placeHolder.setVisibility(View.GONE);
            initPagerAdapter(photoList);
            adapter.update(photoList);
            setInfo(new EventBusMessages.ShowImage(currentPostIdPosition));
        } else {
            ApiFactory.getApi().getAlbumByPhotoId(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), getArguments().getString("photoId"), "1")
                    .enqueue(new Callback<ResponseContainer<PhotoContainer>>() {
                @Override
                public void onResponse(Call<ResponseContainer<PhotoContainer>> call, Response<ResponseContainer<PhotoContainer>> response) {
                    if(response.body().getResponse()!=null){
                        photoList.addAll(response.body().getResponse().getPhotos());
                        adapter.update(photoList);
                        for (int i = 0; i < photoList.size(); i++) {
                            if(photoList.get(i).getId().equals(getArguments().getString("photoId"))){
                                currentPostIdPosition = i;
                            }
                        }
                        recyclerView.scrollToPosition(currentPostIdPosition);
                        placeHolder.setVisibility(View.GONE);
                        totalCount = response.body().getResponse().getCount();
                        initPagerAdapter(photoList);

                        if(response.body().getResponse().getGroups()!=null){
                            group = response.body().getResponse().getGroups().get(0);
                            name.setText(group.getName());
                            Picasso.get().load(group.getPhoto130()).into(photo);
                        }
                        setInfo(new EventBusMessages.ShowImage(currentPostIdPosition));
                        loadAlbumInfo(String.valueOf(response.body().getResponse().getPhotos().get(0).getAlbumId()));
                    }
                }
                @Override
                public void onFailure(Call<ResponseContainer<PhotoContainer>> call, Throwable t) {
                }
            });
        }
    }

    private void loadAlbumInfo(String albumId) {
       ApiFactory.getApi().getAlbumById(App.accessToken(), albumId).enqueue(new Callback<ResponseContainer<Album>>() {
           @Override
           public void onResponse(Call<ResponseContainer<Album>> call, Response<ResponseContainer<Album>> response) {
               if(response.body().getResponse()!=null){
                   placeName.setText(response.body().getResponse().getTitle());
                   time.setText(Util.getDatePretty(response.body().getResponse().getDateCreated()));
               }
           }

           @Override
           public void onFailure(Call<ResponseContainer<Album>> call, Throwable t) {
           }
       });
    }

    private void initPagerAdapter(List<Photo> photoList) {
        final AlbumSliderAdapter albumSliderAdapter = new AlbumSliderAdapter(getContext(), photoList);
        viewPager.setAdapter(albumSliderAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                setNumeration(position);
                setInfo(new EventBusMessages.ShowImage(position));
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    @OnClick(R.id.likeIcon)
    public void like(View v) {
        final Photo photo = photoList.get(currentPostIdPosition);
        if (photo.getLikes().getUserLikes() == 1) {

            photo.getLikes().setCount(photo.getLikes().getCount()-1);
            photo.getLikes().setUserLikes(0);
            setLiked(false);
            likesCount.setText(String.valueOf(photo.getLikes().getCount()));

            ApiFactory.getApi().unlike(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "photo",
                    photo.getId().toString(),
                    photo.getOwnerId().toString()
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
        } else
        if (photo.getLikes().getUserLikes() == 0) {
            photo.getLikes().setCount(photo.getLikes().getCount()+1);
            photo.getLikes().setUserLikes(1);
            setLiked(true);
            likesCount.setText(String.valueOf(photo.getLikes().getCount()));
            ApiFactory.getApi().like(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "photo",
                    photo.getId().toString(),
                    photo.getOwnerId().toString()
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

    void setLiked(boolean b){
        if(b){
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_vector_white));
        } else {
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline_vector_white));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        storage = (Storage) context;

        photoList = (List<Photo>) storage.getDate(getArguments().get("name")+ "_photoList");
        group = (Group) storage.getDate(getArguments().get("name")+ "_group");

        if(photoList ==null){
            photoList = new ArrayList<>();
        } else {
            currentPostIdPosition = (int) storage.getDate(getArguments().get("name") + "_currentPostIdPosition");
            mayRestore = true;
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
                                EventBus.getDefault().post(new EventBusMessages.Claim(Util.createAtt("photo", photoList.get(currentPostIdPosition).getOwnerId(),photoList.get(currentPostIdPosition).getId())));
                                return true;
                            case R.id.copy:
                                Toast.makeText(getContext(),
                                        Util.createLink("photo", photoList.get(currentPostIdPosition).getOwnerId(),photoList.get(currentPostIdPosition).getId()),
                                        Toast.LENGTH_SHORT).show();

                                Util.copyTextToClipboard(
                                        getContext(),
                                        Util.createLink("photo", photoList.get(currentPostIdPosition).getOwnerId(),photoList.get(currentPostIdPosition).getId()));
                                return true;
                            case R.id.share:
                                EventBus.getDefault().post(new EventBusMessages.Share(Util.createAtt("photo", photoList.get(currentPostIdPosition).getOwnerId(),photoList.get(currentPostIdPosition).getId())));
                                return true;
                            case R.id.save:
                                BitmapUtils.downloadFile(photoList.get(currentPostIdPosition).getPhotoOrig(), getActivity());
                                return true;
                        }
                        return true;
                    }
                });

        popupMenu.show();
    }

    private void setNumeration(int current){
        currentPostIdPosition = current;
        numeration.setText((current+1)+"/"+totalCount);
        recyclerView.smoothScrollToPosition(current);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setInfo(EventBusMessages.ShowImage event){

        setNumeration(event.getPosition());

        currentPostId = photoList.get(event.getPosition()).getId();
        viewPager.setCurrentItem(event.getPosition());
        currentPostIdPosition = event.getPosition();
       likesCount.setText(String.valueOf(photoList.get(event.getPosition()).getLikes().getCount()));
       if(photoList.get(event.getPosition()).getLikes().getUserLikes()==1){
           setLiked(true);
       } else {
           setLiked(false);
       }
       commentsCount.setText(String.valueOf(photoList.get(event.getPosition()).getComments().getCount()));
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
            storage.setDate(getArguments().get("name") + "_photoList", photoList);
            storage.setDate(getArguments().get("name") + "_group", group);
            storage.setDate(getArguments().get("name") + "_currentPostIdPosition", currentPostIdPosition);

        }
        if(dismissReason == TabStacker.DismissReason.BACK){
            storage.remove(getArguments().get("name") + "_photoList");
            storage.remove(getArguments().get("name") + "_group");
            storage.remove(getArguments().get("name") + "_currentPostIdPosition");
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
