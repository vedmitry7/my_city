package app.mycity.mycity.views.fragments.events;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponseEvents;
import app.mycity.mycity.util.BitmapUtils;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActionContentFragment extends android.support.v4.app.Fragment implements TabStacker.TabStackInterface {

    LinearLayoutManager mLayoutManager;


    @BindView(R.id.placeName)
    TextView placeName;

    @BindView(R.id.progressBarPlaceHolder)
    ConstraintLayout placeHolder;

    @BindView(R.id.numeration)
    TextView numeration;

    @BindView(R.id.eventPhoto)
    PhotoView eventPhoto;
    Post event;
    String currentPlaceId;

    boolean isLoading;
    int totalCount;

    Storage storage;
    private boolean mayRestore;

    boolean clearScreen;

    Profile profile;

    public static ActionContentFragment createInstance(String name, String eventId, String ownerId, boolean backToPlace) {
        ActionContentFragment fragment = new ActionContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("eventId", eventId);
        bundle.putBoolean("backToPlace", backToPlace);
        bundle.putString("ownerId", ownerId);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.action_content, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        Log.d("TAG21", "ACTION CONTENT");

        EventBus.getDefault().post(new EventBusMessages.BlackStatusBar());

        View.OnClickListener openUserListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenPlace(currentPlaceId));
            }
        };

        loadContent(0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
    }

    private void loadContent(int offset) {

        if(mayRestore){

        } else {
            ApiFactory.getApi().getActionById(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), getArguments().getString("eventId"), "1").enqueue(new Callback<ResponseContainer<ResponseEvents>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseEvents>> call, Response<ResponseContainer<ResponseEvents>> response) {
                    if(response!=null && response.body().getResponse()!=null){
                        placeHolder.setVisibility(View.GONE);
                        event = response.body().getResponse().getItems().get(0);
                        if(response.body().getResponse().getGroups()!=null)
                            placeName.setText(response.body().getResponse().getGroups().get(0).getName());
                        Picasso.get().load(response.body().getResponse().getItems().get(0).getAttachments().get(0).getPhoto780()).into(eventPhoto);
                    }
                }
                @Override
                public void onFailure(Call<ResponseContainer<ResponseEvents>> call, Throwable t) {
                }
            });
        }
    }


    @OnClick(R.id.menuButton)
    public void menu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);

        popupMenu.inflate(R.menu.content_menu);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.complain:
                                EventBus.getDefault().post(new EventBusMessages.Claim(Util.createAtt("action", event.getOwnerId(),event.getId())));

                                return true;
                            case R.id.copy:
                                Toast.makeText(getContext(),
                                        Util.createLink("action", event.getOwnerId(), event.getId()),
                                        Toast.LENGTH_SHORT).show();

                                Util.copyTextToClipboard(
                                        getContext(),
                                        Util.createLink("", event.getOwnerId(), event.getId()));
                                return true;
                            case R.id.share:
                                EventBus.getDefault().post(new EventBusMessages.Share(Util.createAtt("action", event.getOwnerId(),event.getId())));
                                return true;
                            case R.id.save:
                                BitmapUtils.downloadFile(event.getAttachments().get(0).getPhotoOrig(), getActivity());
                                return true;
                        }
                        return true;
                    }
                });

        popupMenu.show();
    }

    @OnClick(R.id.backButton)
    public void back(View v){
        if(getArguments().getBoolean("backToPlace")) {
            EventBusMessages.OpenPlace openPlace = new EventBusMessages.OpenPlace(getArguments().getString("ownerId"));
            openPlace.setCloseCurrent(true);
            openPlace.setTabPos(4);
            EventBus.getDefault().post(openPlace);
        } else {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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
        if(dismissReason == TabStacker.DismissReason.REPLACED){
        }
        if(dismissReason == TabStacker.DismissReason.BACK){
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
