package app.mycity.mycity.views.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponseLike;
import app.mycity.mycity.api.model.ResponseWall;
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

public class ProfileCheckinContentOne extends android.support.v4.app.Fragment implements TabStacker.TabStackInterface {


    @BindView(R.id.feedPhoto)
    ImageView photo;

    @BindView(R.id.commentButton)
    ImageView commentButton;

    @BindView(R.id.likeIcon)
    ImageView likeIcon;

    @BindView(R.id.feedName)
    TextView name;

    @BindView(R.id.feedPostTime)
    TextView time;

    @BindView(R.id.commentsCount)
    TextView commentsCount;

    @BindView(R.id.likesCount)
    TextView likesCount;

    @BindView(R.id.placeName)
    TextView placeName;

    @BindView(R.id.constraintLayout6)
    ConstraintLayout buttonsContainer;

    @BindView(R.id.progressBarPlaceHolder)
    ConstraintLayout placeHolder;

    @BindView(R.id.eventPhoto)
    PhotoView eventPhoto;

    Post post;
    Storage storage;
    Profile profile;

    public static ProfileCheckinContentOne createInstance(String name, String postId, boolean backToProfile) {
        ProfileCheckinContentOne fragment = new ProfileCheckinContentOne();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("postId", postId);
        bundle.putBoolean("backToProfile", backToProfile);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_checkin_content_one, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        EventBus.getDefault().post(new EventBusMessages.BlackStatusBar());

        View.OnClickListener openPlaceListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenUser(post.getOwnerId()));
            }
        };
        name.setOnClickListener(openPlaceListener);
        photo.setOnClickListener(openPlaceListener);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.OpenComments(post.getId(), post.getOwnerId(), "post"));
            }
        });

        loadPost();
    }

    private void loadPost() {
        ApiFactory.getApi().getPostById(App.accessToken(), getArguments().getString("postId"), "1", "photo_130").enqueue(new Callback<ResponseContainer<ResponseWall>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseWall>> call, Response<ResponseContainer<ResponseWall>> response) {
                if(response.body().getResponse()!=null){

                    post = response.body().getResponse().getItems().get(0);

                    Picasso.get().load(response.body().getResponse().getItems().get(0).getAttachments().get(0).getPhoto780()).into(eventPhoto);
                    Picasso.get().load(response.body().getResponse().getProfiles().get(0).getPhoto130()).into(photo);

                    Profile profile = response.body().getResponse().getProfiles().get(0);
                    name.setText(profile.getFirstName() + " " + profile.getLastName());
                    time.setText(Util.getDatePretty(response.body().getResponse().getItems().get(0).getDate()));

                    likesCount.setText(post.getLikes().getCount().toString());
                    commentsCount.setText(post.getComments().getCount().toString());
                    setLiked(post.getLikes().getUserLikes()==1);
                    placeHolder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseWall>> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.backButton)
    public void back(View v){

        if(getArguments().getBoolean("backToProfile")){
            EventBusMessages.OpenUser openUser = new EventBusMessages.OpenUser(post.getOwnerId());
            openUser.setCloseCurrent(true);
            EventBus.getDefault().post(openUser);
        } else {
            getActivity().onBackPressed();
        }
    }

    void setLiked(boolean b){
        if(b){
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_vector_white));
        } else {
            likeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline_vector_white));
        }
    }

    @OnClick(R.id.likeIcon)
    public void like(View v) {
        if (post.getLikes().getUserLikes() == 1) {
            post.getLikes().setCount(post.getLikes().getCount()-1);
            post.getLikes().setUserLikes(0);
            setLiked(false);
            likesCount.setText(String.valueOf(post.getLikes().getCount()));
            ApiFactory.getApi().unlike(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "post",
                    post.getId().toString(),
                    post.getOwnerId().toString()
            ).enqueue(new Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseLike>> call, Response<ResponseContainer<ResponseLike>> response) {
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }

        if (post.getLikes().getUserLikes() == 0) {
            post.getLikes().setCount(post.getLikes().getCount()+1);
            post.getLikes().setUserLikes(1);
            setLiked(true);
            likesCount.setText(String.valueOf(post.getLikes().getCount()));
            ApiFactory.getApi().like(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "post",
                    post.getId().toString(),
                    post.getOwnerId().toString()
            ).enqueue(new Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponseLike>> call, Response<ResponseContainer<ResponseLike>> response) {
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponseLike>> call, Throwable t) {
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
                                return true;
                            case R.id.copy:
                                return true;
                            case R.id.share:
                                return true;
                            case R.id.save:
                                BitmapUtils.downloadFile(post.getAttachments().get(0).getPhotoOrig(), getActivity());
                                return true;
                        }
                        return true;
                    }
                });

        popupMenu.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
        profile = (Profile) storage.getDate(getArguments().getString("storageKey") + "profile");
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
            storage.remove("");
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
