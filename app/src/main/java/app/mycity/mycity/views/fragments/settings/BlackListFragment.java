package app.mycity.mycity.views.fragments.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.Success;
import app.mycity.mycity.api.model.UsersContainer;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.adapters.BlackListRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlackListFragment extends Fragment implements TabStacker.TabStackInterface {


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.progressPlaceHolder)
    ConstraintLayout placeHolder;

    BlackListRecyclerAdapter adapter;
    private List<Profile> userList;
    private boolean mayRestore;
    private Integer totalCount;
    private boolean isLoading;

    LinearLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_black_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BlackListRecyclerAdapter(userList);
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

        getUsers(0);
    }

    private void getUsers(int offset){
        if(mayRestore){
            placeHolder.setVisibility(View.GONE);
            mayRestore = false;
            adapter.update(userList);
        } else {
            ApiFactory.getApi().getBanned(App.accessToken(), SharedManager.getProperty(Constants.KEY_MY_ID), "photo_130").enqueue(new retrofit2.Callback<ResponseContainer<UsersContainer>>() {
                @Override
                public void onResponse(Call<ResponseContainer<UsersContainer>> call, Response<ResponseContainer<UsersContainer>> response) {
                    UsersContainer users = response.body().getResponse();
                    if(users != null){

                        userList = users.getProfiles();
                        totalCount = response.body().getResponse().getCount();
                        isLoading = false;
                        placeHolder.setVisibility(View.GONE);
                        adapter.update(userList);
                    } else {
                    }
                }
                @Override
                public void onFailure(Call<ResponseContainer<UsersContainer>> call, Throwable t) {
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void delete(final EventBusMessages.ClickItem event){

        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setMessage("Удалить " + userList.get(event.getPosition()).getFirstName() + " " + userList.get(event.getPosition()).getLastName() + " из черного списка?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ок",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ApiFactory.getApi().unban(App.accessToken(), userList.get(event.getPosition()).getId()).enqueue(new Callback<ResponseContainer<Success>>() {
                            @Override
                            public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                                if(response.body().getResponse()!=null && response.body().getResponse().getSuccess()==1){
                                    userList.remove(event.getPosition());
                                    adapter.notifyItemRemoved(event.getPosition());
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


    @OnClick(R.id.backButton)
    public void sadsa(View v){
        getActivity().onBackPressed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
