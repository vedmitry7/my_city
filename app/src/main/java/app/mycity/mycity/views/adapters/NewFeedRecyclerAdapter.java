package app.mycity.mycity.views.adapters;

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

public class NewFeedRecyclerAdapter extends RecyclerView.Adapter<NewFeedRecyclerAdapter.ViewHolder> {

    List<Post> postList;
    Map<String, Profile> profiles;
    Map<String, Group> groups;
    int layout;
    Context context;

    public NewFeedRecyclerAdapter(List<Post> postList, Map profiles, Map groups) {
        this.postList = postList;
        this.profiles = profiles;
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_feed_checkin_item, parent, false);
            return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        if(postList.get(position).getAttachments().get(0).getType().equals("video")){
            holder.indicatorVideo.setVisibility(View.VISIBLE);
        } else {
            holder.indicatorVideo.setVisibility(View.GONE);
        }
        Picasso.get()
                .load(postList.get(position).getAttachments()
                .get(0).getPhoto550())
                .placeholder(R.drawable.logo)
                .into(holder.photo);

        if(groups.containsKey(postList.get(position).getPlaceId())){
            String name = (groups.get(postList.get(position).getPlaceId())).getName();
            holder.place.setText((groups.get(postList.get(position).getPlaceId())).getName());
        } else {
            holder.place.setText("albumName absent");
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.feedImage)
        ImageView photo;

        @BindView(R.id.indicatorVideo)
        ImageView indicatorVideo;

        @BindView(R.id.placeLabel)
        TextView place;

        @Nullable
        @BindView(R.id.likesCount)
        TextView likesCount;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

           itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(getAdapterPosition()!=-1){
                       if(groups.containsKey(postList.get(getAdapterPosition()).getPlaceId())){
                           EventBus.getDefault().post(new EventBusMessages.OpenPlacePhoto(
                                   groups.get(postList.get(getAdapterPosition()).getPlaceId()).getId(),
                                   postList.get(getAdapterPosition()).getId())
                           );
                           EventBus.getDefault().postSticky(new EventBusMessages.OpenPlacePhoto2(
                                   groups.get(postList.get(getAdapterPosition()).getPlaceId()).getId(),
                                   postList.get(getAdapterPosition()),
                                   groups.get(postList.get(getAdapterPosition()).getPlaceId()),
                                   profiles.get(postList.get(getAdapterPosition()).getOwnerId())
                           ));
                       }
                   }
               }
           });
        }
    }

    public void update(List<Post> posts, Map profiles, Map groups){
        postList = posts;
        this.profiles = profiles;
        this.groups = groups;

        notifyDataSetChanged();
    }
}
