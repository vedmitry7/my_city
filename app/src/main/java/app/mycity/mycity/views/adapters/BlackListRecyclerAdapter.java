package app.mycity.mycity.views.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BlackListRecyclerAdapter extends RecyclerView.Adapter<BlackListRecyclerAdapter.ViewHolder> {

    List<Profile> userList;

    public BlackListRecyclerAdapter(List<Profile> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.black_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = userList.get(position).getFirstName() + " " + userList.get(position).getLastName();
        holder.name.setText(name);
        Picasso.get().load(userList.get(position).getPhoto130()).into(holder.image);
        if(userList.get(position).getOnline()==1)
            holder.onlineIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.friendsRowName)
        TextView name;

        @BindView(R.id.friendsRowImage)
        CircleImageView image;

        @BindView(R.id.onlineIndicator)
        ImageView onlineIndicator;

        @BindView(R.id.buttonDelete)
        Button buttonDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnTouchListener(Util.getTouchTextListener(name));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenUser(userList.get(getAdapterPosition()).getId()));
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.ClickItem(getAdapterPosition()));
                }
            });
        }
    }

    public void update(List<Profile> users){
        userList = users;
        notifyDataSetChanged();
    }
}
