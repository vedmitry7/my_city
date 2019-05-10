package app.mycity.mycity.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.UsersContainer;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.UserRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class UniversalUserListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.myAllFriendsRecyclerAdapter)
    RecyclerView recyclerView;

    @BindView(R.id.listEmptyContainer)
    ConstraintLayout listEmptyContainer;

    @BindView(R.id.progressBarPlaceHolder)
    ConstraintLayout placeHolder;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    UserRecyclerAdapter adapter;
    List<Profile> userList;

    String id;

    Storage storage;

    boolean mayRestore;

    String type;

    LinearLayoutManager layoutManager;
    private boolean isLoading;
    private int totalCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_friends, container, false);

        if(getArguments() != null){
            id = getArguments().getString("ID");
        }
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        type = getArguments().getString("type");
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new UserRecyclerAdapter(userList);
        recyclerView.setAdapter(adapter);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        isLoading = true;
                        if(totalCount > userList.size()){
                            getUsers(userList.size());
                        }
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        getUsers(userList.size());
    }


    public static UniversalUserListFragment createInstance(String name, String id, String type) {
        UniversalUserListFragment fragment = new UniversalUserListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("id", id);
        bundle.putString("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;

        userList = (List<Profile>) storage.getDate(getArguments().get("name")+ "_" + getArguments().getString("type") + "_userlist");

        if(userList==null){
            userList = new ArrayList<>();
        } else {
           mayRestore = true;
        }
    }

    private void getUsers(int offset){
        if(mayRestore){
            placeHolder.setVisibility(View.GONE);
            mayRestore = false;
            if(userList.size()==0){
                listEmptyContainer.setVisibility(View.VISIBLE);
            }
            adapter.update(userList);
        } else {
            retrofit2.Callback<ResponseContainer<UsersContainer>> callback = new retrofit2.Callback<ResponseContainer<UsersContainer>>() {
                @Override
                public void onResponse(Call<ResponseContainer<UsersContainer>> call, Response<ResponseContainer<UsersContainer>> response) {
                    UsersContainer users = response.body().getResponse();
                    if(users != null){

                        swipeRefreshLayout.setRefreshing(false);
                        userList = users.getProfiles();
                        totalCount = response.body().getResponse().getCount();
                        isLoading = false;
                        placeHolder.setVisibility(View.GONE);
                        if(userList.size()==0){
                            listEmptyContainer.setVisibility(View.VISIBLE);
                        }
                        adapter.update(userList);
                    } else {
                    }
                }
                @Override
                public void onFailure(Call<ResponseContainer<UsersContainer>> call, Throwable t) {
                }
            };

            switch (type){
                case Constants.KEY_PLACE_SUBSCRIBERS:
                    ApiFactory.getApi().getPlaceSubscribers(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"),"photo_130", "0").enqueue(callback);
                return;
                case Constants.KEY_PLACE_ONLINE_SUBSCRIBERS:
                    ApiFactory.getApi().getPlaceSubscribers(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"),"photo_130", "1").enqueue(callback);
                    return;
                case Constants.KEY_USER_IN_PLACE:
                    ApiFactory.getApi().getUsersInPlace(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"),"photo_130", "0").enqueue(callback);
                    return;
                case Constants.KEY_USER_IN_PLACE_SUBSCRIPTION:
                    ApiFactory.getApi().getUsersInPlace(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"),"photo_130", "1").enqueue(callback);
                    return;

                case Constants.KEY_SUBSCRIBERS:
                    ApiFactory.getApi().getSubscribers(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"), 0, "photo_130").enqueue(callback);
                    return;
                case Constants.KEY_SUBSCRIBERS_ONLINE:
                    ApiFactory.getApi().getSubscribers(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"), 1, "photo_130").enqueue(callback);
                    return;

                case Constants.KEY_SUBSCRIPTIONS:
                    ApiFactory.getApi().getSubscriptions(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"),0, "photo_130").enqueue(callback);
                    return;
                case Constants.KEY_SUBSCRIPTIONS_ONLINE:
                    ApiFactory.getApi().getSubscriptions(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), offset, getArguments().getString("id"), 1 , "photo_130").enqueue(callback);
                    return;

            }
        }
    }


    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
        storage.setDate(getArguments().get("name") + "_" + getArguments().getString("type") + "_userlist", userList);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onRefresh() {
        userList = new ArrayList<>();
        getUsers(0);
        placeHolder.setVisibility(View.VISIBLE);
    }
}
