package app.mycity.mycity.views.fragments.places;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Place;
import app.mycity.mycity.api.model.PlacesResponse;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.Success;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.PlacePagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceFragment extends Fragment implements TabStacker.TabStackInterface {

    @BindView(R.id.toolbarTitle)
    TextView title;

    @BindView(R.id.ratingCount)
    TextView ratingCount;

    @BindView(R.id.backButton)
    ImageView backButton;
    @BindView(R.id.menuButton)
    ImageView menuButton;

    @BindView(R.id.placeViewPager)
    ViewPager viewPager;
    @BindView(R.id.placeTabLayout)
    TabLayout tabLayout;

    @BindView(R.id.delivery)
    View delivery;
    @BindView(R.id.toolbarContent)
    ConstraintLayout toolbar;

    @BindView(R.id.place_image)
    ImageView imageView;

    @BindView(R.id.joinLeaveButton)
    Button joinLeaveButton;

    @BindView(R.id.placeProgressBar)
    ConstraintLayout progressBar;

    @BindView(R.id.simpleRatingBar)
    ScaleRatingBar ratingBar;

    @BindView(R.id.placeSubscribersCount)
    TextView placeSubscribersCount;

    @BindView(R.id.usersInPlace)
    TextView usersInPlace;

    private Place place;

    Storage storage;

    boolean mayRestore;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.place_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public static PlaceFragment createInstance(String name, String placeId, int currentTabPos) {
        PlaceFragment fragment = new PlaceFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putInt("currentTabPos", currentTabPos);
        bundle.putString("placeId", placeId);
        fragment.setArguments(bundle);
        return fragment;
    }

    void createPagerAdapter(Place place){
        PlacePagerAdapter adapter = new PlacePagerAdapter(getChildFragmentManager(), place, getArguments().getString("name"));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(getArguments().getInt("currentTabPos"));
        tabLayout.setupWithViewPager(viewPager);
    }

    @OnClick(R.id.menuButton)
    public void menu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);

        popupMenu.inflate(R.menu.place_menu);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.complain:
                                EventBus.getDefault().post(new EventBusMessages.Claim("place" + place.getId()));
                                return true;
                        }
                        return true;
                    }
                });

        popupMenu.show();
    }

    @OnClick(R.id.rateButton)
    public void setRating(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Оценка заведения");

        View view = getActivity().getLayoutInflater().inflate(R.layout.rating_dialog, null);
        builder.setView(view);

        final ScaleRatingBar serviceRatingBar = view.findViewById(R.id.serviceRatingBar);
        final ScaleRatingBar qualityRatingBar = view.findViewById(R.id.qualityRatingBar);
        final ScaleRatingBar priceRatingBar = view.findViewById(R.id.priceRatingBar);
        final ScaleRatingBar interiorRatingBar = view.findViewById(R.id.interiorRatingBar);

        TextView serviceCount = view.findViewById(R.id.serviceCount);
        serviceCount.setText("(" + place.getRate().getService().getCount() + ")");

        TextView qualityCount = view.findViewById(R.id.qualityCount);
        qualityCount.setText("(" + place.getRate().getQuality().getCount() + ")");

        TextView priceCount = view.findViewById(R.id.priceCount);
        priceCount.setText("(" + place.getRate().getPrice().getCount() + ")");

        TextView interiorCount = view.findViewById(R.id.interiorCount);
        interiorCount.setText("(" + place.getRate().getInterior().getCount() + ")");
        final boolean[] wasChanges = new boolean[4];

        int service = (int) place.getRate().getService().getValue();
        int quality = (int) place.getRate().getQuality().getValue();
        int price = (int) place.getRate().getPrice().getValue();
        int interior = (int) place.getRate().getInterior().getValue();

        serviceRatingBar.setRating(place.getRate().getService().getValue());
        qualityRatingBar.setRating(place.getRate().getQuality().getValue());
        priceRatingBar.setRating(place.getRate().getPrice().getValue());
        interiorRatingBar.setRating(place.getRate().getInterior().getValue());

        BaseRatingBar.OnRatingChangeListener listener = new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar baseRatingBar, float v) {
                switch (baseRatingBar.getId()){
                    case R.id.serviceRatingBar:
                        wasChanges[0] = true;
                        break;
                    case R.id.qualityRatingBar:
                        wasChanges[1] = true;
                        break;
                    case R.id.priceRatingBar:
                        wasChanges[2] = true;
                        break;
                    case R.id.interiorRatingBar:
                        wasChanges[3] = true;
                        break;
                }
            }
        };

        serviceRatingBar.setOnRatingChangeListener(listener);
        qualityRatingBar.setOnRatingChangeListener(listener);
        priceRatingBar.setOnRatingChangeListener(listener);
        interiorRatingBar.setOnRatingChangeListener(listener);


        final Callback callback = new Callback<ResponseContainer<Success>>() {
            @Override
            public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
            }

            @Override
            public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {
            }
        };

        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if(wasChanges[0]){
                    ApiFactory.getApi().rate(App.accessToken(), place.getId(), "service",
                            (int)serviceRatingBar.getRating())
                            .enqueue(callback);
                }
                if(wasChanges[1]){
                    ApiFactory.getApi().rate(App.accessToken(), place.getId(), "quality",
                            (int)qualityRatingBar.getRating())
                            .enqueue(callback);
                }
                if(wasChanges[2]){
                    ApiFactory.getApi().rate(App.accessToken(), place.getId(), "price",
                            (int)priceRatingBar.getRating())
                            .enqueue(callback);
                }
                if(wasChanges[3]){
                    ApiFactory.getApi().rate(App.accessToken(), place.getId(), "interior",
                            (int)interiorRatingBar.getRating())
                            .enqueue(callback);
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }


    @OnClick(R.id.backButton)
    public void back(View v){
        getActivity().onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    void loadPlace(String placeId) {
        if(mayRestore){
            createPagerAdapter(place);
            showInfo(place);
            hideProgressBar();
        } else {
            ApiFactory.getApi().getPlaceByIds(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), placeId).enqueue(new Callback<PlacesResponse>() {
                @Override
                public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                    if (response.body().getResponse() != null) {
                        place = response.body().getResponse().get(0);
                        EventBus.getDefault().postSticky(response.body().getResponse().get(0));
                        createPagerAdapter(response.body().getResponse().get(0));
                        ratingBar.setRating(place.getRate().getAll().getValue());
                        ratingCount.setText("(" + place.getRate().getAll().getCount() + ")");
                        showInfo(response.body().getResponse().get(0));
                        hideProgressBar();
                    }
                }

                @Override
                public void onFailure(Call<PlacesResponse> call, Throwable t) {
                }
            });
        }
    }

    void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
    }

    void showInfo(Place place){
        Picasso.get().load(place.getCover1250()).into(imageView);
        title.setText(place.getName());
        placeSubscribersCount.setText("Подписчики: " + place.getCountMembers());
        usersInPlace.setText("Сейчас в заведении: " + place.getCountMembersInPlace());

        if(place.getIsMember()==1){
            joinLeaveButton.setText("Отписаться");
            joinLeaveButton.setBackgroundResource(R.drawable.places_top_bar_bg);
            joinLeaveButton.setTextColor(Color.parseColor("#009688"));
            joinLeaveButton.setVisibility(View.VISIBLE);
        } else {
            joinLeaveButton.setText("Подписаться");
            joinLeaveButton.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
            joinLeaveButton.setTextColor(Color.WHITE);
            joinLeaveButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.joinLeaveButton)
    public void dfkop(View v){
        if(place.getIsMember()==1){
            ApiFactory.getApi().leaveGroup(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), place.getId()).enqueue(new Callback<ResponseContainer<Success>>() {
                @Override
                public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                    if(response.body()!=null){
                        if(response.body().getResponse().getSuccess()==1){
                            place.setIsMember(0);
                            place.setCountMembers(place.getCountMembers()-1);
                            showInfo(place);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {

                }
            });

        } else {
            ApiFactory.getApi().joinToGroup(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), place.getId()).enqueue(new Callback<ResponseContainer<Success>>() {
                @Override
                public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                    if(response.body()!=null){
                        if(response.body().getResponse().getSuccess()==1){
                            place.setIsMember(1);
                            place.setCountMembers(place.getCountMembers()+1);
                            showInfo(place);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {

                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        joinLeaveButton.setVisibility(View.GONE);

        EventBus.getDefault().post(new EventBusMessages.DefaultStatusBar());

        Util.setNawBarClickListener(view);
        Util.setNawBarIconColor(getContext(), view, -1);

        toolbar.setVisibility(View.VISIBLE);

        AppBarLayout appBarLayout = view.findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    title.setTextColor(Color.BLACK);
                    toolbar.setVisibility(View.VISIBLE);
                    delivery.setVisibility(View.GONE);
                    PlaceFragment.this.toolbar.setBackgroundColor(Color.WHITE);

                    backButton.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
                    menuButton.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
                } else if (verticalOffset == 0) {
                    toolbar.setVisibility(View.VISIBLE);
                    title.setTextColor(Color.WHITE);
                    backButton.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
                    menuButton.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
                } else {
                    delivery.setVisibility(View.VISIBLE);
                    PlaceFragment.this.toolbar.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        loadPlace(getArguments().getString("placeId"));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
        place = (Place) storage.getDate(getArguments().get("name")+ "_place");

        if(place!=null){
            mayRestore = true;
        }
    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason presentReason) {
    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason dismissReason) {
        if(dismissReason == TabStacker.DismissReason.REPLACED){
            storage.setDate(getArguments().get("name") + "_place", place);
        }

        if(dismissReason == TabStacker.DismissReason.BACK){

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    storage.remove(getArguments().get("name")+ "_place");

                    storage.remove(getArguments().get("name")+ "_eventsPostList");
                    storage.remove(getArguments().get("name")+ "_eventsGroups");
                    storage.remove(getArguments().get("name")+ "_eventsScrollPosition");

                    storage.remove(getArguments().get("name")+ "_albumsList");
                    storage.remove(getArguments().get("name")+ "_mapAlbums");

                    storage.remove(getArguments().get("name")+ "_postList");
                    storage.remove(getArguments().get("name")+ "_profiles");
                    Log.d("TAG21", "!!!!!!!!!!  remove " + getArguments().get("name")+ "_postList");
                    Log.d("TAG21", "!!!!!!!!!!  remove " + getArguments().get("name")+ "_profiles");

                    storage.remove(getArguments().get("name") + "_actionsPostList");
                    storage.remove(getArguments().get("name") + "_actionsGroups");

                    storage.remove(getArguments().get("name") + "_servicesPostList");
                    storage.remove(getArguments().get("name") + "_servicesGroups");
                    storage.remove(getArguments().get("name") + "_servicesScrollPosition");
                }
            }, 200);

        }
    }

    @Override
    public View onSaveTabFragmentInstance(Bundle bundle) {
        return null;
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle bundle) {
    }

    @OnClick(R.id.placeSubscribersCount)
    public void openSubscribers(View v){
        EventBus.getDefault().post(new EventBusMessages.OpenPlaceSubscribers(place.getId()));
    }

    @OnClick(R.id.usersInPlace)
    public void openUsersInPlace(View v){
        EventBus.getDefault().post(new EventBusMessages.OpenUsersInPlace(place.getId()));
    }
}
