package app.mycity.mycity.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.greenrobot.eventbus.EventBus;
import java.util.HashMap;
import java.util.List;
import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Post;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MapCheckinsRecyclerAdapter extends RecyclerView.Adapter<MapCheckinsRecyclerAdapter.ViewHolder> {

    List<Post> postList;
    HashMap<String, Profile> profiles;
    int layout;
    Context context;

    public MapCheckinsRecyclerAdapter(List<Post> postList, HashMap<String, Profile> profiles) {
        this.postList = postList;
        this.profiles = profiles;
    }

    public void setLayout(int layout){
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkin_row_map, parent, false);
            return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get()
                .load(postList.get(position).getAttachments().get(0).getPhoto130())
                .into(holder.photo);

        holder.time.setText(Util.getDatePretty(postList.get(position).getDate()));

        if(profiles.containsKey(postList.get(position).getOwnerId())){
            final Profile profile = (Profile) profiles.get(postList.get(position).getOwnerId());
            Picasso.get()
                    .load(profile.getPhoto360())
                    .resize(Util.dpToPx(context, 36), Util.dpToPx(context, 36))
                    .centerCrop()
                    .into(holder.profilePhoto);

            holder.name.setText(profile.getFirstName() + " " + profile.getLastName());

            View.OnClickListener onGroupClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenPlace(profile.getId()));
                }
            };

            holder.profilePhoto.setOnClickListener(onGroupClick);
            holder.name.setOnClickListener(onGroupClick);

            holder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.OpenUser(
                            profile.getId()));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo)
        ImageView photo;

        @Nullable
        @BindView(R.id.time)
        TextView time;

        @Nullable
        @BindView(R.id.userPhoto)
        ImageView profilePhoto;

        @Nullable
        @BindView(R.id.name)
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public void update(List<Post> posts, HashMap<String, Profile> profiles){
        postList = posts;
        this.profiles = profiles;
        notifyDataSetChanged();
    }
}
