package app.mycity.mycity.views.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import java.util.Map;

import app.mycity.mycity.App;
import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Group;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PlacesEventRecyclerAdapter extends RecyclerView.Adapter<PlacesEventRecyclerAdapter.ViewHolder> {

    List<Post> postList;
    HashMap<String, Group> groups;
    int layout;
    Context context;

    public PlacesEventRecyclerAdapter(List<Post> postList, HashMap<String, Group> groups) {
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_event_item, parent, false);
            return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Picasso.get()
                .load(postList.get(position).getAttachments()
                .get(0).getPhoto780()).resize(Util.dpToPx(context, 360), Util.dpToPx(context, 360))
                .centerCrop()
                .into(holder.photo);

        if(groups.containsKey(postList.get(position).getOwnerId())){
            Group group = (Group) groups.get(postList.get(position).getOwnerId());
            Picasso.get()
                    .load(group.getPhoto130())
                    .resize(Util.dpToPx(context, 36), Util.dpToPx(context, 36))
                    .centerCrop()
                    .into(holder.ownerImage);

            holder.name.setText(group.getName());
        }

        holder.time.setText(Util.getDatePretty(postList.get(position).getDate()));

        if(holder.likesCount!=null)
            holder.likesCount.setText(String.valueOf(postList.get(position).getLikes().getCount()));
        if(holder.likeIcon!=null){
            if(postList.get(position).getLikes().getUserLikes()==1){
                holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.ic_heart_vector_white));
            } else {
                holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.ic_heart_outline_vector_white));
            }
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.eventImage)
        ImageView photo;
        @BindView(R.id.commentButton)
        ImageView commentButton;
        @Nullable
        @BindView(R.id.likeIcon)
        ImageView likeIcon;
        @Nullable
        @BindView(R.id.placePhoto)
        ImageView ownerImage;
        @BindView(R.id.placeName)
        TextView name;
        @BindView(R.id.placeEventTime)
        TextView time;
        @Nullable
        @BindView(R.id.likesCount)
        TextView likesCount;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if(photo!=null){
                photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new EventBusMessages.OpenEventContent(
                                postList.get(getAdapterPosition()).getId(),
                                postList.get(getAdapterPosition()).getOwnerId(),
                                false));
                    }
                });
            }

            if(likeIcon != null){
                likeIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new EventBusMessages.LikePost(
                                postList.get(getAdapterPosition()).getId().toString(),
                                postList.get(getAdapterPosition()).getOwnerId().toString(),
                                getAdapterPosition()));
                    }
                });
            }

            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenComments(
                            postList.get(getAdapterPosition()).getId().toString(),
                            postList.get(getAdapterPosition()).getOwnerId().toString(), "post"));
                }
            });

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
