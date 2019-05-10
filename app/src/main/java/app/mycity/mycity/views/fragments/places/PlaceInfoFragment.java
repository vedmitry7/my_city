package app.mycity.mycity.views.fragments.places;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Place;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaceInfoFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    Place place;

    @BindView(R.id.placeDescription)
    TextView description;
    @BindView(R.id.placeAddress)
    TextView address;
    @BindView(R.id.placeSchedule)
    TextView schedule;
    @BindView(R.id.placeNumber)
    TextView number;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.place_info_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public static PlaceInfoFragment createInstance() {
        PlaceInfoFragment fragment = new PlaceInfoFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        place =  EventBus.getDefault().getStickyEvent(Place.class);

        address.setText(place.getAddress());
        schedule.setText(place.getSchedule());
        if(place.getPhone()!=null){
            number.setText(place.getPhone());
        }

        if(place.getDescription()!= null && !place.getDescription().equals("")){
            description.setText(place.getDescription());
        } else {
            description.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location = new LatLng(place.getLatitude(), place.getLongitude());
        mMap.addMarker(new MarkerOptions().position(location).title(place.getName()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,16));
    }
}
