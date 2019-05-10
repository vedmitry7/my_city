package app.mycity.mycity.views.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.services.SendImageService;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Group;
import app.mycity.mycity.api.model.Message;
import app.mycity.mycity.api.model.MessageResponse;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.SendMessageResponse;
import app.mycity.mycity.api.model.SuccessResponceNumber;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.adapters.ChatRecyclerAdapter;
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

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends android.support.v4.app.Fragment implements TabStacker.TabStackInterface {

    @BindView(R.id.chatRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.chatEditText)
    EmojiconEditText editText;

    @BindView(R.id.chatProfileImage)
    CircleImageView imageView;

    @BindView(R.id.chatName)
    TextView nameText;

    @BindView(R.id.newMessageIndicator)
    CardView newMessageIndicator;

    @BindView(R.id.dateIndicator)
    CardView dateIndicator;

    @BindView(R.id.dateIndicatorLabel)
    TextView dateIndicatorLabel;

    @BindView(R.id.placesProgressBar)
    ConstraintLayout progressBar;

    @BindView(R.id.blockedHolder)
    ConstraintLayout blockedHolder;

    @BindView(R.id.chatRootView)
    ConstraintLayout chatRootView;

    @BindView(R.id.change)
    ImageView changeButton;

    LinearLayoutManager layoutManager;

    ChatRecyclerAdapter adapter;

    int totalCount;


    long lastMyMessageId;

    EmojIconActions emojIcon;
    String peerId;
    private boolean isLoading;

    private boolean emojyShoven;

    File file;

    List<Message> results;
    Map groups;

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


    @OnClick(R.id.sendImage)
    public void sendImage(View v){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getActivity().startActivityForResult(pickPhoto , 74);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 74:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    file = new File(Util.getPath(getActivity(), selectedImage));
                    sendMessagePhoto();
                }
                break;
        }
    }

    @OnClick(R.id.backButton)
    public void back(View v){
        getActivity().onBackPressed();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public static ChatFragment createInstance(String name, String peerId) {
        ChatFragment fragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("peerId", peerId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        results = new ArrayList<>();
        groups = new HashMap();
        emojIcon=new EmojIconActions(getContext(), chatRootView, editText, new ImageView(getContext()));
        emojIcon.ShowEmojIcon();
        peerId = getArguments().getString("peerId");
        SharedManager.addProperty("unread_" + peerId, "0");
        NotificationManager nMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(Integer.parseInt(peerId));
        layoutManager = new LinearLayoutManager(getContext());

        adapter = new ChatRecyclerAdapter(results, groups);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        isLoading = true;
                        if(totalCount > results.size()){
                            loadMessages(results.size());
                        }
                    }
                }
            }
        };

        RecyclerView.OnScrollListener scrollListener2 = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if(firstVisibleItem==-1){
                    return;
                }
                if(results.size()!=0)  {
                    dateIndicatorLabel.setText(Util.getDate_ddMMyyyy(results.get(lastVisibleItems).getTime()));
                }

                if(results.get(firstVisibleItem).getOut()==1){
                    lastMyMessageId = results.get(firstVisibleItem).getId();
                }
                if(results.get(firstVisibleItem).getOut()==0 && !(results.get(firstVisibleItem).getRead()==1)){
                    if(newMessageIndicator.getVisibility() == View.VISIBLE){
                        newMessageIndicator.setVisibility(View.GONE);
                    }
                    markAsReadMessage(results.get(firstVisibleItem).getId());
                    results.get(firstVisibleItem).setRead(1);
                }
            }
        };


        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.addOnScrollListener(scrollListener2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        newMessageIndicator.setVisibility(View.GONE);
        newMessageIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(0);
                newMessageIndicator.setVisibility(View.GONE);
            }
        });
        recyclerView.scrollToPosition(0);
        loadMessages(0);
    }

    @OnClick(R.id.chatSettings)
    public void chatSettings(View v){
        final PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_chat, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.clearChat:
                        ApiFactory.getApi().deleteDialogs(App.accessToken(), peerId).enqueue(new Callback<ResponseContainer<SuccessResponceNumber>>() {
                            @Override
                            public void onResponse(Call<ResponseContainer<SuccessResponceNumber>> call, Response<ResponseContainer<SuccessResponceNumber>> response) {
                                if(response.body()!=null && response.body().getResponse()!=null){
                                    results.clear();
                                    groups.clear();
                                    adapter.update(results, groups);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseContainer<SuccessResponceNumber>> call, Throwable t) {
                            }
                        });
                        break;
                    case R.id.blockUser:
                        break;
                }
                return true;
            }
        });

        popupMenu.show();
    }

    private void loadMessages(int offset) {

        ApiFactory.getApi().getMessages(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), peerId, offset).enqueue(new retrofit2.Callback<ResponseContainer<MessageResponse>>() {
            @Override
            public void onResponse(Call<ResponseContainer<MessageResponse>> call, final Response<ResponseContainer<MessageResponse>> response) {
                if(response.body()!=null && response.body().getResponse()!=null){
                    Picasso.get().load(response.body().getResponse().getProfiles().get(1).getPhoto130()).into(imageView);
                    if(response.body().getResponse().getProfiles().get(1).getCanAccessClosed() == 1){
                        blockedHolder.setVisibility(View.VISIBLE);
                    }

                    nameText.setText(response.body().getResponse().getProfiles().get(1).getFirstName() + " " +
                            response.body().getResponse().getProfiles().get(1).getLastName());

                    if(response.body().getResponse().getCount()==0){
                        dateIndicator.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventBus.getDefault().post(new EventBusMessages.OpenUser(response.body().getResponse().getProfiles().get(1).getId()));
                        }
                    };
                    imageView.setOnClickListener(onClickListener);
                    nameText.setOnClickListener(onClickListener);

                    adapter.setImageUrl(response.body().getResponse().getProfiles().get(1).getPhoto130());

                    totalCount = response.body().getResponse().getCount();
                    dateIndicator.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                    if(response.body().getResponse().getGroups()!=null){
                        for (Group g: response.body().getResponse().getGroups()){
                            groups.put(g.getId(), g);
                        }
                    }
                    results.addAll(response.body().getResponse().getItems());
                    isLoading = false;
                    adapter.update(results, groups);
                    if(results.size() == response.body().getResponse().getItems().size()){
                        recyclerView.scrollToPosition(0);
                    }
                    markAsRead();
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<MessageResponse>> call, Throwable t) {
            }
        });
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.UpdateChat event){
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final EventBusMessages.NewChatMessage event){
        if(!String.valueOf(event.getMessage().getUser()).equals(peerId))
            return;

        if(dateIndicator.getVisibility()==View.GONE){
            dateIndicator.setVisibility(View.VISIBLE);
        }

        if(event.getOut()==0){
            results.add(0, event.getMessage());
            adapter.notifyItemInserted(0);
            if(layoutManager.findFirstVisibleItemPosition()==0){
                recyclerView.scrollToPosition(0);
                markAsReadMessage(event.getMessage().getId());
            }
            else {
                newMessageIndicator.setVisibility(View.VISIBLE);
            }
        }

        if(event.getOut()==1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean exist = false;
                    for (int i = 0; i < results.size(); i++) {
                        if (results.get(i).getId() == event.getMessage().getId()) {
                            exist = true;
                            results.get(i).setTime(event.getMessage().getTime());
                            results.get(i).setMessageAttachments(event.getMessage().getMessageAttachments());
                            adapter.notifyItemChanged(i);
                            recyclerView.scrollToPosition(0);
                            break;
                        }
                    }

                    if(!exist){
                        results.add(0, event.getMessage());
                        adapter.notifyItemInserted(0);
                        recyclerView.scrollToPosition(0);
                    }
                }
            }, 300);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.MessageWasRead event){

        for (int i = 0; i < results.size(); i++) {
            if(results.get(i).getId()==event.getMessageId()){
                results.get(i).setRead(1);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void sendMessagePhoto() {

        long curId = getVirtualMessageId();

        final Message message = new Message();
        message.setId(curId);
        message.setTime(System.currentTimeMillis()/1000);
        message.setOut(1);
        message.setRead(0);
        message.setSendingHolder(true);

        results.add(0, message);
        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);

        Intent serviceIntent = new Intent(getContext(), SendImageService.class);
        serviceIntent.putExtra("path", file.getAbsolutePath());
        serviceIntent.putExtra("peerId", peerId);
        serviceIntent.putExtra("virtualMessageId", curId);
        getContext().startService(serviceIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messagePhotoDelivered(EventBusMessages.MessagePhotoDelivered event){
        for (Message m: results
                ) {
            if(m.getId()==event.getMessageVirtualId()){
                m.setId(event.getMessageId());
                m.setWasSended(true);
                m.setSendingHolder(false);
            }
        }
        adapter.notifyItemChanged(0);
    }


    long getVirtualMessageId(){
        String id = SharedManager.getProperty("virtualId");
        if(id==null||id.equals("")){
            id = "1000000";
            SharedManager.addProperty("virtualId", id);
        }
        long longId = Long.parseLong(id);

        final long curId = longId;
        longId++;
        SharedManager.addProperty("virtualId", String.valueOf(longId));
        return curId;
    }

    @OnClick(R.id.sendMessageButton)
    public void sendMessage(View v){

        final String messageText = editText.getText().toString();
        editText.setText("");
        final long curId = getVirtualMessageId();
        final Message message = new Message();
        message.setId(curId);
        message.setTime(System.currentTimeMillis()/1000);
        message.setText(messageText);
        message.setOut(1);
        message.setRead(0);

        results.add(0, message);
        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);

        if(!messageText.equals(""))
            ApiFactory.getApi().sendMessage(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), peerId, 0, messageText).enqueue(new retrofit2.Callback<ResponseContainer<SendMessageResponse>>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseContainer<SendMessageResponse>> call, retrofit2.Response<ResponseContainer<SendMessageResponse>> response) {
                    final SendMessageResponse sendMessageResponse = response.body().getResponse();

                    if(response.errorBody()!=null && response.body().getError().getErrorCode()==26){
                        blockedHolder.setVisibility(View.VISIBLE);
                        results.remove(0);
                        adapter.notifyItemRemoved(0);
                        return;
                    }
                    for (Message m: results
                            ) {
                        if(m.getId()==curId){
                            m.setId(response.body().getResponse().getMessageId());
                            m.setWasSended(true);
                            m.setText(messageText);
                        }
                    }
                    adapter.notifyItemChanged(0);
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseContainer<SendMessageResponse>> call, Throwable t) {

                }
            });
    }

    private void markAsRead() {
        ApiFactory.getApi().markAsRead(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), peerId).enqueue(new retrofit2.Callback<ResponseContainer<SuccessResponceNumber>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<SuccessResponceNumber>> call, retrofit2.Response<ResponseContainer<SuccessResponceNumber>> response) {
                if(response.body().getResponse().getSuccess()==1){
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<SuccessResponceNumber>> call, Throwable t) {
             }
        });
    }

    private void markAsReadMessage(final long messageId) {
        ApiFactory.getApi().markAsReadMessages(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), messageId).enqueue(new Callback<ResponseContainer<SuccessResponceNumber>>() {
            @Override
            public void onResponse(Call<ResponseContainer<SuccessResponceNumber>> call, Response<ResponseContainer<SuccessResponceNumber>> response) {
                if(response.body().getResponse()!=null && response.body().getResponse().getSuccess()==1){
                    for (Message m:results
                            ) {
                        if(m.getId()==messageId){
                            m.setRead(1);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<SuccessResponceNumber>> call, Throwable t) {
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final EventBusMessages.DeleteChatMessage event){
        ApiFactory.getApi().deleteMessages(App.accessToken(), event.getId()).enqueue(new Callback<ResponseContainer<SuccessResponceNumber>>() {
            @Override
            public void onResponse(Call<ResponseContainer<SuccessResponceNumber>> call, Response<ResponseContainer<SuccessResponceNumber>> response) {
                if(response.body()!=null && response.body().getResponse()!=null){
                    if(response.body().getResponse().getSuccess()==1){
                        for (int i = 0; i < results.size(); i++) {
                            if(results.get(i).getId() == event.getId()){
                                results.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<SuccessResponceNumber>> call, Throwable t) {

            }
        });
    }

    public String getPeerId(){
        return peerId;
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
    public void onDestroy() {
        super.onDestroy();
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
