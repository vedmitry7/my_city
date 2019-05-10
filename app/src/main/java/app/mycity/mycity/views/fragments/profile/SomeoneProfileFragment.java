package app.mycity.mycity.views.fragments.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Group;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseLike;
import app.mycity.mycity.api.model.ResponsePlaces;
import app.mycity.mycity.api.model.ResponseWall;
import app.mycity.mycity.api.model.Success;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.decoration.ImagesSpacesItemDecoration;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.UsersContainer;
import app.mycity.mycity.views.adapters.CheckinRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SomeoneProfileFragment extends Fragment implements TabStacker.TabStackInterface {

    @BindView(R.id.profileFragRoundImage)
    de.hdodenhof.circleimageview.CircleImageView imageView;

    @BindView(R.id.profileFragProgressBarContainer)
    ConstraintLayout progressBar;

    @BindView(R.id.accessLimitedContainer)
    ConstraintLayout accessLimitedContainer;

    @BindView(R.id.photoAbsentContainer)
    ConstraintLayout photoAbsentContainer;

    @BindView(R.id.profileFragToolbarTitle)
    TextView title;

    @BindView(R.id.profileFragName)
    TextView name;

    @BindView(R.id.someoneFragAdd)
    Button subscribeButton;

    @BindView(R.id.someoneFragChat)
    Button someoneFragChat;

    @BindView(R.id.someOneProfileFragRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.profilePlaceHolder)
    ConstraintLayout placeHolder;

    @BindView(R.id.profileSubscriptionInfoContainer)
    LinearLayout profileSubscriptionInfoContainer;

    @BindView(R.id.profileFragSubscriberTv)
    TextView subscribersCount;

    @BindView(R.id.profileFragSubscriptionTv)
    TextView subscriptionsCount;

    @BindView(R.id.profileFragPlacesCount)
    TextView placesCount;

    @BindView(R.id.profileNestedScrollView)
    NestedScrollView scrollView;

    @BindView(R.id.profileAbout)
    TextView profileAbout;

    @BindView(R.id.onlineIndicator)
    ImageView onlineIndicator;

    @BindView(R.id.placeContainer)
    RelativeLayout placeContainer;

    @BindView(R.id.userPlace)
    TextView userPlace;

    @BindView(R.id.goingToVisit)
    TextView goingToVisit;


    CheckinRecyclerAdapter adapter;

    List<Post> postList;

    private RecyclerView.ItemDecoration spaceDecoration;

    RecyclerView.LayoutManager mLayoutManager;

    ProgressDialog progressDialog;

    boolean friendLoad, checkinLoad, infoLoad, isSubscription;

    String profileId;

    Storage storage;
    boolean mayRestore;

    Profile profile;
    Map groups;
    private View fragmentView;
    private int subscribersCountValue;


    public void showContent() {

        if (friendLoad && checkinLoad && infoLoad) {
            placeHolder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    public static SomeoneProfileFragment createInstance(String name, String userId) {
        SomeoneProfileFragment fragment = new SomeoneProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("profileId", userId);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.someone_profile_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentView = view;

        placeHolder.setVisibility(View.VISIBLE);

        profileId = getArguments().getString("profileId");

        Util.setNawBarClickListener(view);
        Util.setNawBarIconColor(getContext(), view, -1);
        Util.setUnreadCount(view);



        someoneFragChat.setBackgroundResource(R.drawable.places_top_bar_bg);
        someoneFragChat.setTextColor(Color.parseColor("#009688"));

        spaceDecoration = new ImagesSpacesItemDecoration(3, Util.dpToPx(getActivity(), 4), false);

        mLayoutManager = new GridLayoutManager(this.getActivity(), 3);
        recyclerView.addItemDecoration(spaceDecoration);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        if(postList == null){
            postList = new ArrayList<>();
        }

        adapter = new CheckinRecyclerAdapter(postList, groups);

        recyclerView.setAdapter(adapter);

        getInfo();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.UnreadCountUpdate event){
        Util.setUnreadCount(fragmentView);
    }

    @OnClick(R.id.backButton)
    public void back(View v){
        getActivity().onBackPressed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        storage = (Storage) context;
        postList = (List<Post>)storage.getDate((String) getArguments().get("name") + "_posts");
        groups = (Map) storage.getDate(getArguments().get("name")+ "_groups");


        if(postList!=null) {
            mayRestore = true;
        }
        else{
            mayRestore = false;
        }

        if(groups==null){
            groups = new HashMap();
        }

        String[] mass = (String[]) storage.getDate((String) getArguments().get("name")+"_info");
        if(mass!=null){
            name.setText(mass[0]);
            subscribersCount.setText(mass[1]);
            subscriptionsCount.setText(mass[2]);
            scrollView.setVerticalScrollbarPosition(Integer.valueOf(mass[3]));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openPlace(EventBusMessages.ShowImage event){
        storage.setDate(getArguments().getString("name") + "checkins", postList);
        storage.setDate(getArguments().getString("name") + "groups", groups);
        storage.setDate(getArguments().getString("name") + "profile", profile);

        EventBus.getDefault().post(new EventBusMessages.OpenCheckinProfileContent(postList.get(event.getPosition()).getId(), getArguments().getString("name")));
    }


    @OnClick(R.id.someoneFragAdd)
    public void addToFriends(View v) {

        if(isSubscription){
            ApiFactory.getApi().deleteSubscription(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), profileId).enqueue(new Callback<ResponseContainer<Success>>() {
                @Override
                public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                    if(response.body()!=null){
                        if(response.body().getResponse().getSuccess()==1){
                            isSubscription = false;
                            subscribeButton.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
                            subscribeButton.setTextColor(Color.WHITE);
                            subscribeButton.setText("Подписаться");
                            subscribersCount.setText("" + --subscribersCountValue);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {
                }
            });
        } else {
            ApiFactory.getApi().addSubscription(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), profileId).enqueue(new Callback<ResponseContainer<Success>>() {
                @Override
                public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {

                    if(response.body()!=null){
                        if(response.body().getResponse().getSuccess()==1){
                            isSubscription = true;
                            subscribeButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                            subscribeButton.setTextColor(Color.parseColor("#009688"));
                            subscribeButton.setText("Отписаться");
                            subscribersCount.setText("" + ++subscribersCountValue);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {
                }
            });
        }
    }

    private void getInfo() {
        ApiFactory.getApi().getUserById(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), profileId, "about,photo_550,photo_130,is_subscription,is_subscriber,place").enqueue(new retrofit2.Callback<ResponseContainer<Profile>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<Profile>> call, retrofit2.Response<ResponseContainer<Profile>> response) {
                profile = response.body().getResponse();
                if (profile != null) {
                    if(profile.getCanAccessClosed()==1){
                        profileSubscriptionInfoContainer.setVisibility(View.GONE);
                        subscribeButton.setVisibility(View.GONE);
                        someoneFragChat.setVisibility(View.GONE);
                        accessLimitedContainer.setVisibility(View.VISIBLE);
                        placeHolder.setVisibility(View.GONE);
                    } else {
                        getCounts();
                        getCheckins();
                    }

                    if(profile.getIsSubscription()==1){
                        isSubscription = true;
                        subscribeButton.setText("Отписаться");

                        subscribeButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                        subscribeButton.setTextColor(Color.parseColor("#009688"));
                    } else {

                        subscribeButton.setText("Подписаться");
                        subscribeButton.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
                        subscribeButton.setTextColor(Color.WHITE);
                    }

                    if(profile.getOnline()==1){
                        onlineIndicator.setVisibility(View.VISIBLE);
                    } else {
                        onlineIndicator.setVisibility(View.GONE);
                    }

                    name.setText(profile.getFirstName() + " " + profile.getLastName());

                    infoLoad = true;
                    showContent();
                    Picasso.get().load(profile.getPhoto550()).into(imageView);
                    if(profile.getAbout()==null || profile.getAbout().length()==0){
                        profileAbout.setVisibility(View.GONE);
                    } else {
                        profileAbout.setText(profile.getAbout());
                    }

                    if (progressDialog != null) {
                        progressDialog.hide();
                    }
                }

                if(profile.getPlace()!=null){
                    placeContainer.setVisibility(View.VISIBLE);
                    userPlace.setText(profile.getPlace().getName());
                    placeContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventBus.getDefault().post(new EventBusMessages.OpenPlace(profile.getPlace().getId()));
                        }
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<Profile>> call, Throwable t) {
            }
        });
    }

    private void getCounts(){

        ApiFactory.getApi().getSubscribers(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), 0, getArguments().getString("profileId"), 0, "").enqueue(new Callback<ResponseContainer<UsersContainer>>() {
            @Override
            public void onResponse(Call<ResponseContainer<UsersContainer>> call, Response<ResponseContainer<UsersContainer>> response) {

                if(response.body()!= null && response.body().getResponse()!= null){
                    subscribersCountValue = response.body().getResponse().getCount();
                    subscribersCount.setText("" + response.body().getResponse().getCount());
                }
                friendLoad = true;
                showContent();
            }

            @Override
            public void onFailure(Call<ResponseContainer<UsersContainer>> call, Throwable t) {

            }
        });

        ApiFactory.getApi().getSubscriptions(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), 0, getArguments().getString("profileId"), 0, "").enqueue(new Callback<ResponseContainer<UsersContainer>>() {
            @Override
            public void onResponse(Call<ResponseContainer<UsersContainer>> call, Response<ResponseContainer<UsersContainer>> response) {

                if(response.body()!= null && response.body().getResponse()!= null){
                    subscriptionsCount.setText("" + response.body().getResponse().getCount());
                }
                friendLoad = true;
                showContent();
            }

            @Override
            public void onFailure(Call<ResponseContainer<UsersContainer>> call, Throwable t) {

            }
        });

        ApiFactory.getApi().getPlacesByUserId(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), 0, 0, profileId).enqueue(new retrofit2.Callback<ResponseContainer<ResponsePlaces>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<ResponsePlaces>> call, retrofit2.Response<ResponseContainer<ResponsePlaces>> response) {
                if(response.body()!=null){
                    placesCount.setText("" + response.body().getResponse().getCount());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<ResponsePlaces>> call, Throwable t) {

            }
        });


        ApiFactory.getApi().getAllEventsByUserId(App.accessToken(), App.chosenCity(),"","1", 0, profileId).enqueue(new retrofit2.Callback<ResponseContainer<ResponseWall>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<ResponseWall>> call, retrofit2.Response<ResponseContainer<ResponseWall>> response) {
                if(response!=null && response.body().getResponse()!=null){
                    if(response.body().getResponse().getCount()>0){
                        goingToVisit.setVisibility(View.VISIBLE);
                        goingToVisit.setPaintFlags(goingToVisit.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        goingToVisit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EventBus.getDefault().post(new EventBusMessages.OpenProfileEvents(profileId));
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<ResponseWall>> call, Throwable t) {
            }
        });
    }

    private void getCheckins() {



        if(!mayRestore){
            ApiFactory.getApi().getWallById(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), profileId, "1").enqueue(new Callback<ResponseContainer<ResponseWall>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseWall>> call, Response<ResponseContainer<ResponseWall>> response) {
                    postList.addAll(response.body().getResponse().getItems());
                    if(response.body().getResponse().getCount()==0){
                        photoAbsentContainer.setVisibility(View.VISIBLE);
                    }
                    if(response.body().getResponse().getGroups()!=null)
                    for (Group g:response.body().getResponse().getGroups()
                            ) {
                        groups.put(g.getId(), g);
                    }

                    adapter.notifyDataSetChanged();
                    checkinLoad = true;
                    showContent();
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseWall>> call, Throwable t) {
                }
            });
        } else {
            adapter.notifyDataSetChanged();
            checkinLoad = true;
            showContent();
        }
    }


    @OnClick(R.id.someoneFragChat)
    public void chat(View v) {
        EventBus.getDefault().post(new EventBusMessages.OpenChat(profileId));
    }


    @OnClick(R.id.profileFragSubscribersButton)
    public void subscribers(View v){
        EventBus.getDefault().post(new EventBusMessages.OpenSubscribers(profileId));

    }

    @OnClick(R.id.profileFragSubscriptionsButton)
    public void subscriptions(View v){
        EventBus.getDefault().post(new EventBusMessages.OpenSubscriptions(getArguments().getString("profileId")));
    }

    @OnClick(R.id.profilePlaces)
    public void places(View v) {
        EventBus.getDefault().post(new EventBusMessages.OpenUserPlace(profileId));
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final EventBusMessages.LikePost event) {
        if (postList.get(event.getAdapterPosition()).getLikes().getUserLikes() == 1) {
            ApiFactory.getApi().unlike(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "post",
                    postList.get(event.getAdapterPosition()).getId().toString(),
                    postList.get(event.getAdapterPosition()).getOwnerId().toString()
            ).enqueue(new Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseLike>> call, Response<ResponseContainer<ResponseLike>> response) {
                    if (response != null && response.body() != null) {
                        postList.get(event.getAdapterPosition()).getLikes().setCount(response.body().getResponse().getLikes());
                        postList.get(event.getAdapterPosition()).getLikes().setUserLikes(0);
                        adapter.notifyItemChanged(event.getAdapterPosition());
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }

        if (postList.get(event.getAdapterPosition()).getLikes().getUserLikes() == 0) {
            ApiFactory.getApi().like(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "post",
                    postList.get(event.getAdapterPosition()).getId().toString(),
                    postList.get(event.getAdapterPosition()).getOwnerId().toString()
            ).enqueue(new Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseLike>> call, Response<ResponseContainer<ResponseLike>> response) {
                    if (response != null && response.body() != null) {
                        postList.get(event.getAdapterPosition()).getLikes().setCount(response.body().getResponse().getLikes());
                        postList.get(event.getAdapterPosition()).getLikes().setUserLikes(1);
                        adapter.notifyItemChanged(event.getAdapterPosition());
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }
    }

    @OnClick(R.id.menuButton)
    public void menu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.profile_menu);
        MenuItem ban = popupMenu.getMenu().findItem(R.id.ban);
        if(profile.getIsClosed()==1){
            ban.setTitle("Разблокировать");
        }
        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.complain:
                                EventBus.getDefault().post(new EventBusMessages.Claim("profile"+ profileId));
                                return true;
                            case R.id.share:
                                return true;
                            case R.id.ban:
                                if(profile.getIsClosed()==1){
                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                    alertDialog.setMessage("Разблокировать " + profile.getFirstName() + " " + profile.getLastName() + "?");
                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ок",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ApiFactory.getApi().unban(App.accessToken(), profileId).enqueue(new Callback<ResponseContainer<Success>>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                                                            if(response.body().getResponse()!=null && response.body().getResponse().getSuccess()==1){
                                                                profile.setIsClosed(0);
                                                                Toast.makeText(getContext(), "Пользователь разблокирован", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {

                                                        }
                                                    });
                                                    dialog.dismiss();
                                                }
                                            });

                                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    alertDialog.show();

                                } else {

                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                    alertDialog.setMessage("Заблокировать " + profile.getFirstName() + " " + profile.getLastName() + "?");
                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ок",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ApiFactory.getApi().ban(App.accessToken(), profileId).enqueue(new Callback<ResponseContainer<Success>>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                                                            if(response.body().getResponse()!=null && response.body().getResponse().getSuccess()==1){
                                                                profile.setIsClosed(1);
                                                                Toast.makeText(getContext(), "Пользователь заблокирован", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                        @Override
                                                        public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {

                                                        }
                                                    });
                                                    dialog.dismiss();
                                                }
                                            });

                                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    alertDialog.show();
                                }


                                return true;
                        }
                        return true;
                    }
                });

        popupMenu.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason presentReason) {

    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason dismissReason) {

        if(dismissReason != TabStacker.DismissReason.BACK){
            storage.setDate((String) getArguments().get("name")+"_posts", postList);
            storage.setDate((String) getArguments().get("name")+"_groups", groups);
            String[] mass = new String[4];
            mass[0] = name.getText().toString();
            mass[1] = subscribersCount.getText().toString();
            mass[2] = subscriptionsCount.getText().toString();
            mass[3] = String.valueOf(scrollView.getScrollY());
            storage.setDate((String) getArguments().get("name")+"_info", mass);
        } else {
            storage.remove((String) getArguments().get("name")+"_posts");
            storage.remove((String) getArguments().get("name")+"_info");
            storage.remove(getArguments().getString("name") + "checkins");
            storage.remove(getArguments().getString("name") + "groups");
            storage.remove(getArguments().getString("name") + "profile");
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