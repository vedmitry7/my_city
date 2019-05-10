package app.mycity.mycity.views.fragments.events;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponseEventVisitors;
import app.mycity.mycity.api.model.ResponseEvents;
import app.mycity.mycity.api.model.ResponseLike;
import app.mycity.mycity.api.model.ResponseVisit;
import app.mycity.mycity.util.BitmapUtils;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.EventVisitorsRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventContentFragment extends android.support.v4.app.Fragment implements TabStacker.TabStackInterface {

    LinearLayoutManager mLayoutManager;

    @BindView(R.id.placesFragmentCheckinRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.commentButton)
    ImageView commentButton;

    @BindView(R.id.likeIcon)
    ImageView likeIcon;

    @BindView(R.id.commentsCount)
    TextView commentsCount;

    @BindView(R.id.likesCount)
    TextView likesCount;

    @BindView(R.id.eventConfirm)
    TextView eventConfirm;

    @BindView(R.id.placeName)
    TextView placeName;

    @BindView(R.id.visitorsCount)
    TextView visitorsCount;

    @BindView(R.id.constraintLayout6)
    ConstraintLayout buttonsContainer;

    @BindView(R.id.progressBarPlaceHolder)
    ConstraintLayout placeHolder;

    @BindView(R.id.numeration)
    TextView numeration;

    @BindView(R.id.eventPhoto)
    PhotoView eventPhoto;

    List<Profile> profileList;
    Post event;
    EventVisitorsRecyclerAdapter adapter;
    String currentPlaceId;
    boolean isLoading;
    int totalCount;
    Storage storage;
    private boolean mayRestore;
    boolean clearScreen;

    public static EventContentFragment createInstance(String name, String eventId, String ownerId, boolean backToPlace) {
        EventContentFragment fragment = new EventContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("eventId", eventId);
        bundle.putBoolean("backToPlace", backToPlace);
        bundle.putString("ownerId", ownerId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_content, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        EventBus.getDefault().post(new EventBusMessages.BlackStatusBar());

        View.OnClickListener openUserListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenPlace(currentPlaceId));
            }
        };

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenComments(event.getId(), event.getOwnerId(), "event"));
            }
        });

        mLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);

        profileList= new ArrayList<>();
        adapter = new EventVisitorsRecyclerAdapter(profileList);
        loadVisitors();

        recyclerView.setAdapter(adapter);

        loadContent(0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
    }

    private void loadContent(int offset) {

        if(mayRestore){
        } else {
            ApiFactory.getApi().getEventsById(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), getArguments().getString("eventId"), "1").enqueue(new retrofit2.Callback<ResponseContainer<ResponseEvents>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseEvents>> call, retrofit2.Response<ResponseContainer<ResponseEvents>> response) {

                    if(response!=null && response.body().getResponse()!=null){
                        placeHolder.setVisibility(View.GONE);
                        if(response.body().getResponse().getGroups()!=null)
                        placeName.setText(response.body().getResponse().getGroups().get(0).getName());
                        event = response.body().getResponse().getItems().get(0);
                        show();
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseEvents>> call, Throwable t) {
                }
            });
        }
    }

    void loadVisitors(){
        ApiFactory.getApi().getVisitors(App.accessToken(), getArguments().getString("eventId"), getArguments().getString("ownerId"), "photo_360").enqueue(new Callback<ResponseContainer<ResponseEventVisitors>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseEventVisitors>> call, Response<ResponseContainer<ResponseEventVisitors>> response) {
                if(response.body().getResponse().getCount()!=0){
                    profileList.addAll(response.body().getResponse().getItems());
                    adapter.update(profileList);
                    totalCount = response.body().getResponse().getCount();
                    isLoading = false;
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseEventVisitors>> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.backButton)
    public void back(View v){
        if(getArguments().getBoolean("backToPlace")) {
            EventBusMessages.OpenPlace openPlace = new EventBusMessages.OpenPlace(getArguments().getString("ownerId"));
            openPlace.setCloseCurrent(true);
            openPlace.setTabPos(3);
            EventBus.getDefault().post(openPlace);
        } else {
            getActivity().onBackPressed();
        }
    }

    public void show(){
        Picasso.get().load(event.getAttachments().get(0).getPhoto780()).into(eventPhoto);
        commentsCount.setText(String.valueOf(event.getComments().getCount()));
        setLiked();
        setConfirmation();
    }

    @OnClick(R.id.eventPhoto)
    public void clickImage(View v) {
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

    void setLiked(){
        if(event.getLikes().getUserLikes()==1){
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_vector_white));
        } else {
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline_vector_white));
        }
        likesCount.setText(String.valueOf(event.getLikes().getCount()));
    }

    void setConfirmation(){
        if(event.getVisits().getUserVisit()==1){
            eventConfirm.setBackgroundResource(R.drawable.confirm_event_on);
            eventConfirm.setTextColor(Color.WHITE);
        } else {
            eventConfirm.setBackgroundResource(R.drawable.confirm_event_off);
            eventConfirm.setTextColor(Color.parseColor("#009688"));
        }
        visitorsCount.setText(String.valueOf(event.getVisits().getCount()));
    }

    @OnClick(R.id.likeIcon)
    public void like(View view) {
        if (event.getLikes().getUserLikes() == 1) {
            Log.d("TAG21", "unlike");
            event.getLikes().setCount(event.getLikes().getCount());
            event.getLikes().setUserLikes(0);
            setLiked();
            ApiFactory.getApi().unlike(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "event",
                    event.getId().toString(),
                    event.getOwnerId().toString()
            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseLike>> call, retrofit2.Response<ResponseContainer<ResponseLike>> response) {
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }

        if (event.getLikes().getUserLikes() == 0) {

            event.getLikes().setCount(event.getLikes().getCount()+1);
            event.getLikes().setUserLikes(1);
            setLiked();
            ApiFactory.getApi().like(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "event",
                    event.getId().toString(),
                    event.getOwnerId().toString()
            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseLike>> call, retrofit2.Response<ResponseContainer<ResponseLike>> response) {
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }
    }

    @OnClick(R.id.eventConfirm)
    public void confirmation(View view) {
        if (event.getVisits().getUserVisit() == 1) {
            ApiFactory.getApi().removeVisit(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    event.getId().toString(),
                    event.getOwnerId().toString()
            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseVisit>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseVisit>> call, retrofit2.Response<ResponseContainer<ResponseVisit>> response) {
                    if (response != null && response.body() != null) {
                        event.getVisits().setCount(response.body().getResponse().getVisitors());
                        event.getVisits().setUserVisit(0);
                        setConfirmation();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseVisit>> call, Throwable t) {
                }
            });
        }

        if (event.getVisits().getUserVisit() == 0) {
            ApiFactory.getApi().addVisit(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    event.getId().toString(),
                    event.getOwnerId().toString()
            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseVisit>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseVisit>> call, retrofit2.Response<ResponseContainer<ResponseVisit>> response) {
                    if (response != null && response.body() != null) {
                        event.getVisits().setCount(response.body().getResponse().getVisitors());
                        event.getVisits().setUserVisit(1);
                        setConfirmation();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseVisit>> call, Throwable t) {
                }
            });
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
                                EventBus.getDefault().post(new EventBusMessages.Share(Util.createAtt("event", event.getOwnerId(),event.getId())));
                                return true;
                            case R.id.copy:
                                Toast.makeText(getContext(),
                                        Util.createLink("event", event.getOwnerId(),event.getId()),
                                        Toast.LENGTH_SHORT).show();

                                Util.copyTextToClipboard(
                                        getContext(),
                                        Util.createLink("event",  event.getOwnerId(),event.getId()));
                                return true;
                            case R.id.share:
                                EventBus.getDefault().post(new EventBusMessages.Share(Util.createAtt("event", event.getOwnerId(),event.getId())));
                                return true;
                            case R.id.save:
                                BitmapUtils.downloadFile(event.getAttachments().get(0).getPhotoOrig(), getActivity());
                                return true;
                        }
                        return true;
                    }
                });

        popupMenu.show();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason presentReason) {
    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason dismissReason) {
        if(dismissReason == TabStacker.DismissReason.REPLACED){
        }
        if(dismissReason == TabStacker.DismissReason.BACK){
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
