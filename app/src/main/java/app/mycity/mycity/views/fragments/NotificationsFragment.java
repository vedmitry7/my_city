package app.mycity.mycity.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import app.mycity.mycity.App;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Notification;
import app.mycity.mycity.api.model.NotificationResponce;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.NotificationRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotificationsFragment extends Fragment implements TabStacker.TabStackInterface {

    @BindView(R.id.dialogsFragRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.placesProgressBar)
    ConstraintLayout placesProgressBar;

    NotificationRecyclerAdapter adapter;

    List<Notification> notificationList;

    private boolean isLoading;
    private int totalCount;
    private View fragmentView;
    private Storage storage;
    private boolean mayRestore;
    private int scrollPosition;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public static NotificationsFragment createInstance(String name) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentView = view;

        Util.setNawBarClickListener(view);
        Util.setNawBarIconColor(getContext(), view, 3);
        Util.setUnreadCount(view);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        isLoading = true;
                        if(totalCount > notificationList.size()){
                            loadNotifications(notificationList.size());
                        }
                    }
                }
            }
        };
        adapter = new NotificationRecyclerAdapter(notificationList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setAdapter(adapter);

        loadNotifications(notificationList.size());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.UnreadCountUpdate event){
        Util.setUnreadCount(fragmentView);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void loadNotifications(int offset) {
        if(mayRestore){
            adapter.update(notificationList);
            recyclerView.scrollToPosition(scrollPosition);
            mayRestore = false;
            placesProgressBar.setVisibility(View.GONE);
        } else {
            ApiFactory.getApi().getNotifications(App.accessToken(), offset).enqueue(new Callback<ResponseContainer<NotificationResponce>>() {
                @Override
                public void onResponse(Call<ResponseContainer<NotificationResponce>> call, Response<ResponseContainer<NotificationResponce>> response) {
                    if(response != null && response.body().getResponse() != null){

                        placesProgressBar.setVisibility(View.GONE);
                        totalCount = response.body().getResponse().getCount();
                        notificationList.addAll(response.body().getResponse().getItems());
                        adapter.update(notificationList);
                        isLoading = false;
                    }
                }

                @Override
                public void onFailure(Call<ResponseContainer<NotificationResponce>> call, Throwable t) {
                }
            });

        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final EventBusMessages.DeleteDialog event) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        storage = (Storage) context;

        notificationList = (List<Notification>) storage.getDate(getArguments().get("name") + "_notifications");

        if(notificationList==null){
            notificationList = new ArrayList<>();
        } else {
            scrollPosition = (int) storage.getDate(getArguments().get("name") + "_scrollPosition");
            mayRestore = true;
        }
    }


    @OnClick(R.id.profileFragBackButtonContainer)
    public void back(View v){
        getActivity().onBackPressed();
    }


    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason presentReason) {

    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason dismissReason) {
        if(dismissReason == TabStacker.DismissReason.REPLACED){
            storage.setDate(getArguments().get("name") + "_notifications", notificationList);
            storage.setDate(getArguments().get("name") + "_scrollPosition", ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition());

        }
        if(dismissReason == TabStacker.DismissReason.BACK){
            storage.remove(getArguments().get("name") + "_notifications");
            storage.remove(getArguments().get("name") + "_scrollPosition");
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
