package app.mycity.mycity.views.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.fragments.ShareFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BottomSheetRecyclerAdapter extends RecyclerView.Adapter<BottomSheetRecyclerAdapter.ViewHolder> {

    List<Profile> userList;
    Map<String, ShareFragment.ButtonState> stateMap;

    public BottomSheetRecyclerAdapter(List<Profile> userList, Map<String, ShareFragment.ButtonState> stateMap) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = userList.get(position).getFirstName() + " " + userList.get(position).getLastName();
        holder.name.setText(name);

        ShareFragment.ButtonState state = stateMap.get(userList.get(position).getId());

        switch (state){
            case cancel:
                holder.buttonSend.setVisibility(View.VISIBLE);
                holder.buttonSend.setBackgroundResource(R.drawable.places_top_bar_bg);
                holder.buttonSend.setTextColor(Color.parseColor("#009688"));
                holder.buttonSend.setText("Отмена");
                break;
            case send:
                holder.buttonSend.setVisibility(View.VISIBLE);
                holder.buttonSend.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
                holder.buttonSend.setTextColor(Color.WHITE);
                holder.buttonSend.setText("Отправить");
                break;
            case progress:
                holder.buttonSend.setVisibility(View.INVISIBLE);
                break;
        }

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
        @BindView(R.id.buttonSend)
        Button buttonSend;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;


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

            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.ClickItem(getAdapterPosition()));
                }
            });
        }
    }

    public void update(List<Profile> users, Map<String, ShareFragment.ButtonState> stateMap){
        userList.addAll(users);
        this.stateMap = stateMap;
        notifyDataSetChanged();
    }
}
