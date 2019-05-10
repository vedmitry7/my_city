package app.mycity.mycity.views.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Group;
import app.mycity.mycity.api.model.Message;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {

    List<Message> messages;
    private Context context;
    Map<String, Group> groups;


    public ChatRecyclerAdapter(List<Message> messages, Map groups){
        this.messages = messages;
        this.groups = groups;
    }

    String imageUrl = "";

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(context==null){
            context = parent.getContext();
        }
        View view = null;
        if(viewType == 0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.someome_message_row, parent, false);
        }
        if ((viewType == 1)){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_message_row, parent, false);
        }
        if(viewType == 3){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.someome_att_message_row, parent, false);
        }
        if ((viewType == 2)){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_att_message_row, parent, false);
        }
        if ((viewType == 4)){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sending_image_message_row, parent, false);
        }
        if ((viewType == 5)){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_post_message_row, parent, false);
        }
        if ((viewType == 6)){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.someone_post_message_row, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        DateFormat format = new SimpleDateFormat("HH:mm");

        if(holder.messageTime!=null)
            holder.messageTime.setText(Util.getTime(messages.get(position).getTime()));


        if(holder.message!=null){
            holder.message.setText(messages.get(position).getText());
        }
        if(holder.indicator!=null && messages.get(position).getOut()==1){
            if(!messages.get(position).isWasSended()){
                holder.indicator.setImageResource(R.drawable.ic_check);
            } else {
                holder.indicator.setImageResource(R.drawable.ic_check_double);
                if (messages.get(position).getRead()==1){
                    holder.indicator.setColorFilter(context.getResources().getColor(R.color.colorAccent));
                } else {
                    holder.indicator.setColorFilter(context.getResources().getColor(R.color.white_50percent));
                }
            }
        }

        if(messages.get(position).getMessageAttachments()!=null){

            if(messages.get(position).getMessageAttachments().get(0).getType().equals("photo")){
                Picasso.get().load(messages.get(position).getMessageAttachments().get(0).getPhoto360()).into(holder.attImage);
            }
            final String type = messages.get(position).getMessageAttachments().get(0).getType();
            if(type.equals("post") || type.equals("event") || type.equals("action") || type.equals("service")){
                Picasso.get().load(messages.get(position).getMessageAttachments().get(0).getAttachments().get(0).getPhoto360()).into(holder.attImage);
                if(groups.containsKey(messages.get(position).getMessageAttachments().get(0).getPlaceId())){
                    holder.placeName.setText(groups.get(messages.get(position).getMessageAttachments().get(0).getPlaceId()).getName());
                Picasso.get().load(groups.get(messages.get(position).getMessageAttachments().get(0).getPlaceId()).getPhoto130()).into(holder.placeIcon);
                }
            }

            if(holder.attImage!=null)
            holder.attImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (type){
                        case "post":
                            EventBus.getDefault().post(new EventBusMessages.ProfileCheckinContentOne(messages.get(position).getMessageAttachments().get(0).getId(), false));
                            break;
                        case "event":
                            EventBus.getDefault().post(new EventBusMessages.OpenEventContent(
                                    messages.get(position).getMessageAttachments().get(0).getId(),
                                    messages.get(position).getMessageAttachments().get(0).getOwnerId(),
                                    false));
                            break;
                            case "action":
                            EventBus.getDefault().post(new EventBusMessages.OpenEventContent(
                                    messages.get(position).getMessageAttachments().get(0).getId(),
                                    messages.get(position).getMessageAttachments().get(0).getOwnerId(),
                                    false));
                            break;
                            case "photo":

                                if(messages.get(position).getMessageAttachments().get(0).getAlbumId()>4)
                            EventBus.getDefault().post(new EventBusMessages.OpenPhotoReportContent(
                                    messages.get(position).getMessageAttachments().get(0).getId()));
                                else
                                    EventBus.getDefault().post(new EventBusMessages.ShowImageFragment(messages.get(position).getMessageAttachments().get(0).getPhotoOrig()));
                            break;
                    }
                }
            });
        }

        if(position == 0){
            holder.messageContentBottomPadding.setVisibility(View.VISIBLE);
        } else {
            holder.messageContentBottomPadding.setVisibility(View.GONE);
        }

        if(position>0){
            String tCur = Util.getDate_ddMMyyyy(messages.get(position).getTime());
            String tNext = Util.getDate_ddMMyyyy(messages.get(position-1).getTime());

            if(!tCur.equals(tNext)) {
                holder.lineDeliver.setVisibility(View.VISIBLE);
                holder.dateDeliver.setVisibility(View.VISIBLE);
                holder.dateDeliver.setText(Util.getDate_ddMMyyyy(messages.get(position - 1).getTime()));
            } else {
                holder.lineDeliver.setVisibility(View.GONE);
                holder.dateDeliver.setVisibility(View.GONE);
            }
        }

        if(holder.icon!=null){
            if(!imageUrl.equals("")){
                holder.icon.setVisibility(View.VISIBLE);
                Picasso.get().load(imageUrl).into(holder.icon);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.message_text)
        TextView message;
        @Nullable
        @BindView(R.id.messageTime)
        TextView messageTime;
        @Nullable
        @BindView(R.id.message_indicator)
        ImageView indicator;
        @Nullable
        @BindView(R.id.messageDeliverLine)
        View lineDeliver;
        @Nullable
        @BindView(R.id.icon)
        ImageView icon;
        @Nullable
        @BindView(R.id.attImage)
        ImageView attImage;
        @BindView(R.id.messageContentBottomPadding)
        View messageContentBottomPadding;
        @BindView(R.id.messageContent)
        CardView messageContent;
        @Nullable
        @BindView(R.id.dateDeliver)
        TextView dateDeliver;
        @Nullable
        @BindView(R.id.placeIcon)
        ImageView placeIcon;
        @Nullable
        @BindView(R.id.placeName)
        TextView placeName;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            messageContent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final PopupMenu popupMenu = new PopupMenu(itemView.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.popupmenu_message, popupMenu.getMenu());

                    MenuItem delete = popupMenu.getMenu().findItem(R.id.deleteMessage);
                    if(messages.get(getAdapterPosition()).getOut()==1){
                        delete.setVisible(true);
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {

                                case R.id.copyMessage:
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("", messages.get(getAdapterPosition()).getText());
                                    clipboard.setPrimaryClip(clip);
                                    break;
                                case R.id.deleteMessage:
                                    EventBus.getDefault().post(new EventBusMessages.DeleteChatMessage(messages.get(getAdapterPosition()).getId()));
                                    break;
                            }
                            return true;
                        }
                    });

                    popupMenu.show();
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).isSendingHolder()){
            return 4;
        }

        if(messages.get(position).getOut()==1){
            if(messages.get(position).getMessageAttachments()!=null){
                if(messages.get(position).getMessageAttachments().get(0).getType().equals("photo"))
                    return 2;
                if(messages.get(position).getMessageAttachments().get(0).getType().equals("post")
                        || messages.get(position).getMessageAttachments().get(0).getType().equals("event")
                        || messages.get(position).getMessageAttachments().get(0).getType().equals("action")
                        || messages.get(position).getMessageAttachments().get(0).getType().equals("service"))
                    return 5;
            }
            return 1;
        }
        if(messages.get(position).getOut()==0){
            if(messages.get(position).getMessageAttachments()!=null) {
                if(messages.get(position).getMessageAttachments().get(0).getType().equals("photo"))
                    return 3;
                if(messages.get(position).getMessageAttachments().get(0).getType().equals("post") )
                    return 6;
            }
            return 0;
        }


        return super.getItemViewType(position);
    }


    public void update(List<Message> messages, Map groups){
        this.messages = messages;
        this.groups = groups;
        notifyDataSetChanged();
    }
}
