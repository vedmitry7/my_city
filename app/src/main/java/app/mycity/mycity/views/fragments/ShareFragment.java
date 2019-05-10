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
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.SendMessageResponse;
import app.mycity.mycity.api.model.SuccessResponceNumber;
import app.mycity.mycity.api.model.UsersContainer;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.adapters.BottomSheetRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareFragment extends BottomSheetDialogFragment {

    String attachment;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    BottomSheetRecyclerAdapter adapter;
    public enum ButtonState {
        send, progress, cancel
    }

    Map<String, ButtonState> stateMap;
    Map<String, Long> messageIdMap;

    List<Profile> userList;
    private Integer totalCount;
    private boolean isLoading;

    public static ShareFragment createInstance(String attachments) {
        ShareFragment fragment = new ShareFragment();
        Bundle bundle = new Bundle();
        bundle.putString("attachments", attachments);
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
        View v =  inflater.inflate(R.layout.fragment_bottom_sheet_share, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        attachment = getArguments().getString("attachments");

        userList = new ArrayList<>();
        stateMap = new HashMap<>();
        messageIdMap = new HashMap<>();

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BottomSheetRecyclerAdapter(userList, stateMap);

        recyclerView.setAdapter(adapter);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int first = layoutManager.findFirstVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        if(totalCount > userList.size()){
                            isLoading = true;
                            getUsers(userList.size());
                        }
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        getUsers(userList.size());
    }

    private void getUsers(int size) {

        ApiFactory.getApi().getSubscriptions(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), size, getArguments().getString("id"), 0, "photo_130").enqueue(
                new Callback<ResponseContainer<UsersContainer>>() {
            @Override
            public void onResponse(Call<ResponseContainer<UsersContainer>> call, Response<ResponseContainer<UsersContainer>> response) {
                UsersContainer users = response.body().getResponse();
                if(users != null){
                    userList = users.getProfiles();
                    totalCount = response.body().getResponse().getCount();
                    isLoading = false;
                 for(Profile u:users.getProfiles()){
                     stateMap.put(u.getId(), ButtonState.send);
                 }
                    adapter.update(userList, stateMap);
                }
            }
            @Override
            public void onFailure(Call<ResponseContainer<UsersContainer>> call, Throwable t) {
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void clickItem(final EventBusMessages.ClickItem event){

        final String id = userList.get(event.getPosition()).getId();

        switch (stateMap.get(id)){
            case send:
                stateMap.put(id, ButtonState.progress);
                adapter.notifyItemChanged(event.getPosition());

                ApiFactory.getApi().sendImageMessage(App.accessToken(), id, 0, attachment).enqueue(new Callback<ResponseContainer<SendMessageResponse>>() {
                    @Override
                    public void onResponse(Call<ResponseContainer<SendMessageResponse>> call, Response<ResponseContainer<SendMessageResponse>> response) {
                        if(response.body().getResponse().getSuccess()==1){
                            messageIdMap.put(id, response.body().getResponse().getMessageId());
                            stateMap.put(id, ButtonState.cancel);
                            adapter.notifyItemChanged(event.getPosition());
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseContainer<SendMessageResponse>> call, Throwable t) {

                    }
                });
                break;
            case cancel:
                stateMap.put(id, ButtonState.progress);
                adapter.notifyItemChanged(event.getPosition());

                ApiFactory.getApi().deleteMessages(App.accessToken(), messageIdMap.get(id)).enqueue(new Callback<ResponseContainer<SuccessResponceNumber>>() {
                    @Override
                    public void onResponse(Call<ResponseContainer<SuccessResponceNumber>> call, Response<ResponseContainer<SuccessResponceNumber>> response) {
                        if(response.body()!=null && response.body().getResponse()!=null){
                            stateMap.put(id, ButtonState.send);
                            adapter.notifyItemChanged(event.getPosition());
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseContainer<SuccessResponceNumber>> call, Throwable t) {

                    }
                });
                break;
        }
    }

    @OnClick(R.id.done)
    public void done(){
        this.dismiss();
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
}