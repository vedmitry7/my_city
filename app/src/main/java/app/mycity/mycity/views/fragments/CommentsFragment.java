package app.mycity.mycity.views.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Comment;
import app.mycity.mycity.api.model.Likes;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseAddComment;
import app.mycity.mycity.api.model.ResponseComments;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponseLike;
import app.mycity.mycity.api.model.SubComment;
import app.mycity.mycity.api.model.SubCommentContainer;
import app.mycity.mycity.api.model.Success;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.CommentsRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import fr.arnaudguyon.tabstacker.TabStacker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsFragment extends android.support.v4.app.Fragment implements TabStacker.TabStackInterface {


    @BindView(R.id.commentsFragmentRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.commentPhoto)
    CircleImageView photo;

    @BindView(R.id.addCommentEditText)
    EmojiconEditText editText;

    CommentsRecyclerAdapter adapter;

    List<Comment> commentList;
    Map profiles = new HashMap<Long, Profile>();

    @BindView(R.id.rootView)
    ConstraintLayout rootView;

    @BindView(R.id.commentsPlaceHolder)
    ConstraintLayout placeHolder;

    @BindView(R.id.addCommentProgress)
    ProgressBar progressBar;

    @BindView(R.id.change)
    ImageView changeButton;

    @BindView(R.id.replyText)
    TextView replyText;

    EmojIconActions emojIcon;

    boolean isLoading;

    int totalCount;

    String postId = "46";
    String ownerId = "45";
    private String commentText;

    boolean emojyShoven;

    Type type = Type.COMMENT;
    private String replyCommentId;
    private int replyCommentPosition;
    private Storage storage;

    enum Type {
        COMMENT, REPLY
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comment_fragment, container, false);
        postId = getArguments().getString("postId");
        ownerId = getArguments().getString("ownerId");
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.change)
    public void clickChange(View v){
        if(!emojyShoven){
            emojIcon.setUseSystemEmoji(true);
            changeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_outline));
        } else {
            changeButton.setImageDrawable(getResources().getDrawable(R.drawable.smiley));
            emojIcon.closeEmojIcon();
        }
        emojyShoven = !emojyShoven;

    }


    public static CommentsFragment createInstance(String fragmentId, String postId, String ownerId, String type) {
        CommentsFragment fragment = new  CommentsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", fragmentId);
        bundle.putString("postId", postId);
        bundle.putString("ownerId", ownerId);
        bundle.putString("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @OnClick(R.id.profileFragBackButtonContainer)
    public void backButton(View v){
        getActivity().onBackPressed();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        emojIcon=new EmojIconActions(getContext(), rootView, editText, new ImageView(getContext()));
        emojIcon.ShowEmojIcon();

        commentList = new ArrayList<>();

        adapter = new CommentsRecyclerAdapter(commentList, profiles);

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
                        if(totalCount >= commentList.size()){
                            loadComments(commentList.size());
                        }
                    }
                }
            }
        };

        if(SharedManager.getProperty(Constants.KEY_PHOTO_130)!=null){
            Picasso.get().load(SharedManager.getProperty(Constants.KEY_PHOTO_130)).into(photo);
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(scrollListener);
        loadComments(commentList.size());

        replyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyText.setVisibility(View.GONE);
                type = Type.COMMENT;
            }
        });
    }



    private void loadComments(final int offset) {

        Callback callback = new Callback<ResponseContainer<ResponseComments>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseComments>> call, Response<ResponseContainer<ResponseComments>> response) {

                if(response!=null&&response.body().getResponse()!=null){
                    isLoading = false;
                    totalCount = response.body().getResponse().getCount();
                    commentList.addAll(response.body().getResponse().getItems());
                    if(response.body().getResponse().getProfiles()!=null){
                        for (Profile p: response.body().getResponse().getProfiles()
                                ) {
                            profiles.put(p.getId(), p);
                        }
                    }
                    adapter.update(commentList, profiles);
                    if(commentList.size()==0){
                        placeHolder.setVisibility(View.VISIBLE);
                    } else {
                        placeHolder.setVisibility(View.GONE);
                    }
                    if(offset==0){
                        recyclerView.scrollToPosition(0);
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseContainer<ResponseComments>> call, Throwable t) {
            }
        };
        switch (getArguments().getString("type")){
            case "post":
                ApiFactory.getApi().getCommentsPost(
                        SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                        "1",
                        postId,
                        ownerId,
                        offset,
                        "1",
                        20,
                        2,
                        "photo_130").enqueue(callback);
                break;

            case "photo":
                ApiFactory.getApi().getCommentsPhoto(
                        SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                        "1",
                        postId,
                        ownerId,
                        offset,
                        "1",
                        2,
                        20,
                        "photo_130").enqueue(callback);
                break;

                case "event":
                ApiFactory.getApi().getCommentsEvent(
                        SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                        "1",
                        postId,
                        ownerId,
                        offset,
                        "1",
                        2,
                        20,
                        "photo_130").enqueue(callback);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reply(EventBusMessages.ReplyToComment event){
        if(profiles.containsKey(event.getUserId())){
            Profile profile = (Profile) profiles.get(event.getUserId());
            replyText.setText("Ответ для " + profile.getFirstName());
        }
        type = Type.REPLY;
        replyText.setVisibility(View.VISIBLE);
        App.showKeyboard(getContext());
        editText.requestFocus();
        replyCommentId = event.getCommentId();
        replyCommentPosition = event.getPosition();
    }




    @OnClick(R.id.addComment)
    public void addComment(View v){
        progressBar.setVisibility(View.VISIBLE);
        commentText = editText.getText().toString();
        editText.setText("");

        Callback commentCallback = new Callback<ResponseContainer<ResponseAddComment>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseAddComment>> call, Response<ResponseContainer<ResponseAddComment>> response) {

                if(response!=null&&response.body().getResponse()!=null){
                    Comment comment = new Comment();
                    comment.setText(commentText);
                    comment.setOwnerId(SharedManager.getProperty(Constants.KEY_MY_ID));
                    comment.setDate((int) (Calendar.getInstance().getTimeInMillis()/1000));
                    comment.setId(response.body().getResponse().getCommentId());
                    comment.setFromId(SharedManager.getProperty(Constants.KEY_MY_ID));
                    comment.setPostId(postId);
                    Likes likes = new Likes();
                    likes.setUserLikes(0);
                    likes.setCount(0);
                    comment.setLikes(likes);
                    SubCommentContainer subCommentContainer = new SubCommentContainer();
                    List<SubComment> subCommentList = new ArrayList<>();
                    subCommentContainer.setItems(subCommentList);
                    comment.setSubComments(subCommentContainer);
                    progressBar.setVisibility(View.GONE);
                    commentList.add(0, comment);
                    adapter.update(commentList, profiles);
                    adapter.notifyItemChanged(replyCommentPosition);
                    placeHolder.setVisibility(View.GONE);
                    replyText.setVisibility(View.GONE);
                    type = Type.COMMENT;
                }

            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseAddComment>> call, Throwable t) {
            }
        };

        Callback replyCallback = new Callback<ResponseContainer<ResponseAddComment>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseAddComment>> call, Response<ResponseContainer<ResponseAddComment>> response) {

                if(response!=null&&response.body().getResponse()!=null){
                    SubComment comment = new SubComment();
                    comment.setText(commentText);
                    comment.setOwnerId(SharedManager.getProperty(Constants.KEY_MY_ID));
                    comment.setDate((int) (Calendar.getInstance().getTimeInMillis()/1000));
                    comment.setId(response.body().getResponse().getCommentId());
                    comment.setFromId(SharedManager.getProperty(Constants.KEY_MY_ID));
                    comment.setPostId(postId);
                    Likes likes = new Likes();
                    likes.setUserLikes(0);
                    likes.setCount(0);
                    comment.setLikes(likes);
                    progressBar.setVisibility(View.GONE);
                    commentList.get(replyCommentPosition).getSubComments().getItems().add(0, comment);
                    commentList.get(replyCommentPosition).getSubComments().setCount(commentList.get(replyCommentPosition).getSubComments().getItems().size());
                    adapter.notifyDataSetChanged();
                    placeHolder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseAddComment>> call, Throwable t) {

            }
        };

        if(type == Type.REPLY){
            switch (getArguments().getString("type")){

                case "post":
                    ApiFactory.getApi().addReplyToPostComment(App.accessToken(), postId, ownerId, commentText, replyCommentId).enqueue(replyCallback);
                    break;
                case "photo":
                    ApiFactory.getApi().addReplyToPhotoComment(App.accessToken(), postId, ownerId, commentText, replyCommentId).enqueue(replyCallback);
                    break;
                case "event":
                    ApiFactory.getApi().addReplyToEventComment(App.accessToken(), postId, ownerId, commentText, replyCommentId).enqueue(replyCallback);
                    break;
            }
        }

        if(type == Type.COMMENT){
            switch (getArguments().getString("type")){
                case "post":
                    ApiFactory.getApi().addPostComment(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), postId, ownerId, commentText).enqueue(commentCallback);
                    break;
                case "photo":
                    ApiFactory.getApi().addPhotoComment(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), postId, ownerId, commentText).enqueue(commentCallback);
                    break;
                case "event":
                    ApiFactory.getApi().addEventComment(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), postId, ownerId, commentText).enqueue(commentCallback);
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void like(final EventBusMessages.LikeComment event) {

        Comment comment = commentList.get(event.getAdapterPosition());

        if (comment.getLikes().getUserLikes() == 1) {
            comment.getLikes().setCount(comment.getLikes().getCount()-1);
            comment.getLikes().setUserLikes(0);
            adapter.notifyItemChanged(event.getAdapterPosition());
            ApiFactory.getApi().unlike(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "comment",
                    comment.getId(),
                    comment.getOwnerId()
            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseLike>> call, retrofit2.Response<ResponseContainer<ResponseLike>> response) {
                    if (response != null && response.body() != null) {
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        } else
        if (comment.getLikes().getUserLikes() == 0) {

            comment.getLikes().setCount(comment.getLikes().getCount()+1);
            comment.getLikes().setUserLikes(1);
            adapter.notifyItemChanged(event.getAdapterPosition());
            ApiFactory.getApi().like(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "comment",
                    comment.getId(),
                    comment.getOwnerId()

            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseLike>> call, retrofit2.Response<ResponseContainer<ResponseLike>> response) {
                    if (response != null && response.body() != null) {
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void like(final EventBusMessages.LikeSubcomment event) {

        Comment comment = commentList.get(event.getAdapterPosition());

        SubComment  subComment = comment.getSubComments().getItems().get(event.getCommentPosition());

        if (comment.getLikes().getUserLikes() == 1) {
            subComment.getLikes().setCount(subComment.getLikes().getCount()-1);
            subComment.getLikes().setUserLikes(0);
            adapter.notifyItemChanged(event.getAdapterPosition());
            ApiFactory.getApi().unlike(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "comment",
                    subComment.getId(),
                    subComment.getOwnerId()
            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseLike>> call, retrofit2.Response<ResponseContainer<ResponseLike>> response) {
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        } else
        if (comment.getLikes().getUserLikes() == 0) {
            subComment.getLikes().setCount(subComment.getLikes().getCount()+1);
            subComment.getLikes().setUserLikes(1);
            adapter.notifyItemChanged(event.getAdapterPosition());
            ApiFactory.getApi().like(
                    SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                    "comment",
                    subComment.getId(),
                    subComment.getOwnerId()

            ).enqueue(new retrofit2.Callback<ResponseContainer<ResponseLike>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<ResponseLike>> call, retrofit2.Response<ResponseContainer<ResponseLike>> response) {
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<ResponseLike>> call, Throwable t) {
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final EventBusMessages.DeleteComment event) {

        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Удалить комментарий?");
        alertDialog.setMessage(commentList.get(event.getPosition()).getText());
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        final int position = event.getPosition();
                        Callback callback = new Callback<ResponseContainer<Success>>() {
                            @Override
                            public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                                if(response!=null&&response.body().getResponse() != null && response.body().getResponse().getSuccess()==1)
                                    commentList.remove(position);
                                adapter.notifyItemRemoved(position);
                            }
                            @Override
                            public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {
                            }
                        };

                        switch (getArguments().getString("type")){
                            case "post":
                                ApiFactory.getApi().deleteComment(
                                        SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                                        commentList.get(position).getId(),
                                        ownerId).enqueue(callback);
                                break;
                            case "photo":
                                ApiFactory.getApi().deleteCommentPhoto(
                                        SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN),
                                        commentList.get(position).getId(),
                                        ownerId).enqueue(callback);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void storeComment(EventBusMessages.StoreComment event){
        storage.setDate("tempComment" + commentList.get(event.getPosition()).getId(), commentList.get(event.getPosition()));
        if(profiles.containsKey(commentList.get(event.getPosition()).getOwnerId())){
            storage.setDate("tempCommentProfile" + commentList.get(event.getPosition()).getId(), profiles.get(commentList.get(event.getPosition()).getOwnerId()));
        }

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;

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
