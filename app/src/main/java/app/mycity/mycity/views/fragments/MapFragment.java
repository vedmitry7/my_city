package app.mycity.mycity.views.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.willy.ratingbar.ScaleRatingBar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Place;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponsePlaces;
import app.mycity.mycity.map.AbstractMarker;
import app.mycity.mycity.map.OwnIconRendered;
import app.mycity.mycity.util.BitmapUtils;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements TabStacker.TabStackInterface, OnMapReadyCallback {

    @BindView(R.id.placeImage)
    ImageView placeImage;

    @BindView(R.id.placeName)
    TextView placeName;

    @BindView(R.id.countCheckin)
    TextView countCheckin;

    @BindView(R.id.placeOnline)
    TextView placeOnline;

    @BindView(R.id.limitOneHour)
    TextView limitOneHourButton;

    @BindView(R.id.limitThreeHour)
    TextView limitThreeHourButton;

    @BindView(R.id.limitTwelveHour)
    TextView limitTwelveHourButton;

    @BindView(R.id.placeRatingBar)
    ScaleRatingBar placeRatingBar;

    @BindView(R.id.placeCard)
    CardView placeCard;

    @BindView(R.id.mapTabLayout)
    TabLayout tabLayout;

    private View fragmentView;

    private int period = 1;

    private GoogleMap mMap;

    List<Place> placeList = new ArrayList<>();
    Map<String, Place> placeMap = new HashMap<>();

    private ClusterManager<AbstractMarker> mClusterManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentView = view;

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("Места");
        tabLayout.addTab(firstTab,0);

        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("Чекины");
        tabLayout.addTab(secondTab,1);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        mClusterManager.clearItems();
                        mMap.clear();
                        showLimitButton(false);
                        loadPlaces();
                        break;
                    case 1:
                        mClusterManager.clearItems();
                        mMap.clear();
                        setSelectedPeriod();
                        showLimitButton(true);
                        loadPlaces();
                        placeCard.setVisibility(View.GONE);
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        showLimitButton(false);

        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        limitOneHourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClusterManager.cluster();
            }
        });
        limitTwelveHourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                period = 12;
                setSelectedPeriod();
                clearAndUpdateMap();

            }
        });
        limitThreeHourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                period = 3;
                setSelectedPeriod();
                clearAndUpdateMap();
            }
        });
    }

    void clearAndUpdateMap(){
        placeCard.setVisibility(View.GONE);
        mClusterManager.clearItems();
        mMap.clear();
        loadPlaces();
    }

    void showLimitButton(boolean show){
        if(show){
            limitOneHourButton.setVisibility(View.VISIBLE);
            limitThreeHourButton.setVisibility(View.VISIBLE);
            limitTwelveHourButton.setVisibility(View.VISIBLE);
        } else {
            limitOneHourButton.setVisibility(View.GONE);
            limitThreeHourButton.setVisibility(View.GONE);
            limitTwelveHourButton.setVisibility(View.GONE);
        }
    }

    void setSelectedPeriod(){
        switch (period){
            case 1:
                limitOneHourButton.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
                limitOneHourButton.setTextColor(Color.WHITE);
                limitThreeHourButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                limitThreeHourButton.setTextColor(Color.parseColor("#009688"));
                limitTwelveHourButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                limitTwelveHourButton.setTextColor(Color.parseColor("#009688"));
                break;
            case 3:
                limitThreeHourButton.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
                limitThreeHourButton.setTextColor(Color.WHITE);
                limitOneHourButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                limitOneHourButton.setTextColor(Color.parseColor("#009688"));
                limitTwelveHourButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                limitTwelveHourButton.setTextColor(Color.parseColor("#009688"));
                break;
            case 12:
                limitTwelveHourButton.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
                limitTwelveHourButton.setTextColor(Color.WHITE);
                limitThreeHourButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                limitThreeHourButton.setTextColor(Color.parseColor("#009688"));
                limitOneHourButton.setBackgroundResource(R.drawable.places_top_bar_bg);
                limitOneHourButton.setTextColor(Color.parseColor("#009688"));
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason presentReason) {
    }
    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason dismissReason) {
    }
    @Override
    public View onSaveTabFragmentInstance(Bundle bundle) {
        return null;
    }
    @Override
    public void onRestoreTabFragmentInstance(Bundle bundle) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mClusterManager = new ClusterManager<>(getContext(), googleMap);
        mMap.setOnCameraIdleListener(mClusterManager);

        mClusterManager.setRenderer(new OwnIconRendered(getContext(), mMap, mClusterManager));

        String lat = SharedManager.getProperty("latitude");
        String lon = SharedManager.getProperty("longitude");

        LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,0.0000001f));

        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<AbstractMarker>() {
            @Override
            public boolean onClusterItemClick(AbstractMarker abstractMarker) {
                showPlaceCard(abstractMarker.getId());
                return false;
            }
        });

        loadPlaces();
    }

    private void loadPlaces() {


        if(tabLayout.getSelectedTabPosition()==0){
            ApiFactory.getApi().getAllForMap(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), SharedManager.getIntProperty("chosen_city_id"), 0, 100).enqueue(new Callback<ResponseContainer<ResponsePlaces>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponsePlaces>> call, Response<ResponseContainer<ResponsePlaces>> response) {
                    if(response.body()!=null && response.body().getResponse().getItems()!=null){
                        placeList = response.body().getResponse().getItems();
                        for (Place p:response.body().getResponse().getItems()
                                ) {
                            placeMap.put(p.getId(), p);
                            if(p.getLatitude()!=null && p.getLongitude() != null){
                                createClusterItem(p);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponsePlaces>> call, Throwable t) {
                }
            });

        }

        if(tabLayout.getSelectedTabPosition()==1){
            ApiFactory.getApi().getPlacesForMap(App.accessToken(), SharedManager.getIntProperty("chosen_city_id"),0,  period * 60).enqueue(new Callback<ResponseContainer<ResponsePlaces>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponsePlaces>> call, Response<ResponseContainer<ResponsePlaces>> response) {
                    if(response.body()!=null && response.body().getResponse().getItems()!=null){
                        placeList = response.body().getResponse().getItems();
                        for (Place p:response.body().getResponse().getItems()
                                ) {
                            placeMap.put(p.getId(), p);
                            if(p.getLatitude()!=null && p.getLongitude() != null){
                                createClusterItem(p);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponsePlaces>> call, Throwable t) {

                }
            });
        }
    }


    List<Target> targetList = new ArrayList<>();

    void createClusterItem(final Place p){
        Target target =  new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mClusterManager.addItem(
                        new AbstractMarker(
                                p.getLatitude(),
                                p.getLongitude(),
                                p.getName(),
                                p.getId(),
                                BitmapDescriptorFactory.fromBitmap(BitmapUtils.bitmapCircularCroper(getContext(), bitmap))));

                mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom + 0.001f));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        targetList.add(target);

        if(p.getVerified()==1){
            Picasso.get()
                    .load(p.getPhoto130())
                    .into(target);
        } else {
            mClusterManager.addItem(
                    new AbstractMarker(
                            p.getLatitude(),
                            p.getLongitude(),
                            p.getName(),
                            p.getId(),
                            BitmapDescriptorFactory.fromBitmap(
                                    BitmapUtils.bitmapCircularCroper(getContext(),
                                            BitmapUtils.getBitmapFromResource(getContext(), R.drawable.ic_marker_location)))));
        }
    }



    void showPlaceCard(String id){
        final Place place;
        if(placeMap.containsKey(id)){
            place = placeMap.get(id);
        } else {
            return;
        }

        if(tabLayout.getSelectedTabPosition()==1){
            MapContentFragment bottomSheetFragment = MapContentFragment.createInstance(place.getId(), place.getCountPosts());
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        }
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenPlace(place.getId()));
            }
        };
        placeCard.setVisibility(View.VISIBLE);
        Picasso.get().load(place.getPhoto130()).into(placeImage);
        placeName.setText(place.getName());
        if(place.getVerified()==1){
            placeRatingBar.setRating(place.getRate().getAll().getValue());
            placeRatingBar.setVisibility(View.VISIBLE);
            placeImage.setOnClickListener(clickListener);
            placeName.setOnClickListener(clickListener);
        } else {
            placeRatingBar.setVisibility(View.GONE);
        }
        placeOnline.setText("Сейчас в заведении: " + place.getCountMembersInPlace());
        countCheckin.setText("Количество чекинов: " + place.getCountPosts());

    }
}
