package app.mycity.mycity.views.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Place;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponsePlaces;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.PlacesRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;

public class UserPlacesFragment extends Fragment implements TabStacker.TabStackInterface {


    @BindView(R.id.placesFragmentRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.placesProgressBar)
    ConstraintLayout placesProgressBar;

    @BindView(R.id.placeInfoPlaceHolder)
    ConstraintLayout placeInfoPlaceHolder;

    PlacesRecyclerAdapter adapter;

    List<Place> placeList;

    boolean isLoading;

    int totalCount;

    Storage storage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_places_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.backButton)
    public void back(View v){
        getActivity().onBackPressed();
    }

    public static UserPlacesFragment createInstance(String name, String userId) {
        UserPlacesFragment fragment = new UserPlacesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("id", userId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Util.setNawBarClickListener(view);
        Util.setNawBarIconColor(getContext(), view, -1);
        placeList = new ArrayList<>();
        adapter = new PlacesRecyclerAdapter(placeList);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        isLoading = true;
                        if(totalCount >= placeList.size()){
                            loadPlaces(placeList.size());
                        }
                    }
                }
            }
        };

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(scrollListener);
        loadPlaces(placeList.size());
    }

    private void loadPlaces(int offset) {
        ApiFactory.getApi().getPlacesByUserId(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, 0, getArguments().getString("id")).enqueue(new retrofit2.Callback<ResponseContainer<ResponsePlaces>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<ResponsePlaces>> call, retrofit2.Response<ResponseContainer<ResponsePlaces>> response) {
                if(response.body().getResponse()!=null){
                    placesProgressBar.setVisibility(View.GONE);
                    if(response.body().getResponse().getCount()==0){
                        placeInfoPlaceHolder.setVisibility(View.VISIBLE);
                    }
                    placeList.addAll(response.body().getResponse().getItems());
                    adapter.update(placeList);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<ResponsePlaces>> call, Throwable t) {
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
    }


    @Override
    public void onResume() {
        super.onResume();
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
}
