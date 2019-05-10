package app.mycity.mycity.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Dialog;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DialogsRecyclerAdapter extends RecyclerView.Adapter<DialogsRecyclerAdapter.ViewHolder> {

    List<Dialog> dialogList;
    private Context context;

    public DialogsRecyclerAdapter(List<Dialog> dialogList) {
        this.dialogList = dialogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(context == null){
            context =parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = dialogList.get(position).getTitle();
        holder.name.setText(name);
        holder.lastMessage.setText(dialogList.get(position).getText());

        if(dialogList.get(position).getCountUnread()!=0){
            holder.unreadCount.setVisibility(View.VISIBLE);
            holder.unreadCount.setText("" + dialogList.get(position).getCountUnread());
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        if(dialogList.get(position).getDate()!=null){
            holder.time.setText(Util.getDatePretty(dialogList.get(position).getDate()));
        } else {
            holder.time.setText("null");
        }
        Picasso.get().load(dialogList.get(position).getPhoto130()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return dialogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.dialog_row_name)
        TextView name;

        @BindView(R.id.unreadCount)
        TextView unreadCount;

        @BindView(R.id.dialogsRowImage)
        CircleImageView image;

        @BindView(R.id.dialog_row_time)
        TextView time;

        @BindView(R.id.dialog_row_message)
        TextView lastMessage;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedManager.addProperty("unread_" + dialogList.get(getAdapterPosition()).getId(), "0");
                    notifyItemChanged(getAdapterPosition());
                    dialogList.get(getAdapterPosition()).setCountUnread(0);
                    EventBus.getDefault().post(new EventBusMessages.OpenChat(dialogList.get(getAdapterPosition()).getId()));
                }
            });

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenUser(dialogList.get(getAdapterPosition()).getId()));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final PopupMenu popupMenu = new PopupMenu(itemView.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.popupmenu_dialog, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.deleteDialog:
                                    EventBus.getDefault().post(new EventBusMessages.DeleteDialog(dialogList.get(getAdapterPosition()).getId()));
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

    public void update(List<Dialog> dialogs){
        dialogList = dialogs;
        notifyDataSetChanged();
    }
}
