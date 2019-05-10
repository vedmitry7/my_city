package app.mycity.mycity.views.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import app.mycity.mycity.App;
import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Group;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ServicesRecyclerAdapter extends RecyclerView.Adapter<ServicesRecyclerAdapter.ViewHolder> {

    List<Post> postList;
    HashMap<String, Group> groups;
    int layout;
    Context context;

    public ServicesRecyclerAdapter(List<Post> postList, HashMap<String, Group> groups) {
        this.postList = postList;
        this.groups = groups;
    }

    public void setLayout(int layout){
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(context==null)
            context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Picasso.get()
                .load(postList.get(position).getAttachments()
                .get(0).getPhoto780()).resize(Util.dpToPx(context, 360), Util.dpToPx(context, 360))
                .centerCrop()
                .into(holder.photo);

        holder.eventName.setText(postList.get(position).getText());

        if(groups.containsKey(postList.get(position).getOwnerId())){
            final Group group = (Group) groups.get(postList.get(position).getOwnerId());
            Picasso.get()
                    .load(group.getPhoto130())
                    .resize(Util.dpToPx(context, 36), Util.dpToPx(context, 36))
                    .centerCrop()
                    .into(holder.ownerImage);

            holder.name.setText(group.getName());

            View.OnClickListener onGroupClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenPlace(group.getId()));
                }
            };

            holder.ownerImage.setOnClickListener(onGroupClick);
            holder.name.setOnClickListener(onGroupClick);

            holder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenServiceContent(
                            postList.get(position).getId(),
                            postList.get(position).getOwnerId(),
                            false));
                }
            });

            holder.name.setVisibility(View.VISIBLE);
            holder.ownerImage.setVisibility(View.VISIBLE);
        } else {
            holder.name.setVisibility(View.GONE);
            holder.ownerImage.setVisibility(View.GONE);
        }
        holder.price.setText(postList.get(position).getPrice());
        holder.time.setText(Util.getDatePretty(postList.get(position).getDate()));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.eventImage)
        ImageView photo;
        @Nullable
        @BindView(R.id.placePhoto)
        ImageView ownerImage;
        @BindView(R.id.placeName)
        TextView name;
        @BindView(R.id.placeEventTime)
        TextView time;
        @BindView(R.id.eventName)
        TextView eventName;
        @BindView(R.id.price)
        TextView price;
        @Nullable
        @BindView(R.id.likesCount)
        TextView likesCount;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenUser(postList.get(getAdapterPosition()).getOwnerId()));
                }
            });
        }
    }

    public void update(List<Post> posts, HashMap<String, Group> groups){
        postList = posts;
        this.groups = groups;
        notifyDataSetChanged();
    }
}
