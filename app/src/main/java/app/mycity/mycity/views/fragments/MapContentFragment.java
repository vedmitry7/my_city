package app.mycity.mycity.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponseWall;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.adapters.MapCheckinsRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapContentFragment extends BottomSheetDialogFragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    MapCheckinsRecyclerAdapter adapter;
    List<Post> postList;
    HashMap<String, Profile> profiles = new HashMap<>();

    private Integer totalCount;
    private boolean isLoading;
    private String placeId;
    private int count;

    public static MapContentFragment createInstance(String placeId, int count) {
        MapContentFragment fragment = new MapContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("placeId", placeId);
        bundle.putInt("count", count);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_bottom_sheet_checkins, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        placeId = getArguments().getString("placeId");
        count = getArguments().getInt("count");

        postList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MapCheckinsRecyclerAdapter(postList, profiles);

        recyclerView.setAdapter(adapter);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        if(totalCount > postList.size()){
                            isLoading = true;
                            loadCheckins(totalItemCount);
                        }
                    }
                }
            }
        };

        recyclerView.addOnScrollListener(scrollListener);
        loadCheckins(0);
    }

    private void loadCheckins(final int offset) {

            ApiFactory.getApi().getGroupWallById(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), placeId, "checkin", "1","photo_130", offset, count).enqueue(new Callback<ResponseContainer<ResponseWall>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseWall>> call, Response<ResponseContainer<ResponseWall>> response) {


                    if(response.body().getResponse()!=null){
                        totalCount = response.body().getResponse().getCount();
                        isLoading = false;
                        postList.addAll(response.body().getResponse().getItems());
                        if(response.body().getResponse().getProfiles()!=null
                                )
                            for (Profile p: response.body().getResponse().getProfiles()
                                    ) {
                                profiles.put(p.getId(), p);
                            }
                    }
                    adapter.update(postList, profiles);
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseWall>> call, Throwable t) {

                }
            });
        }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}