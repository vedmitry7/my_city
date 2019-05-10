package app.mycity.mycity.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.List;
import java.util.Map;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.model.Comment;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.SubComment;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SubCommentsRecyclerAdapter extends RecyclerView.Adapter<SubCommentsRecyclerAdapter.ViewHolder> {

    List<SubComment> commentList;
    Map profiles;
    int layout;
    Context context;
    Comment comment;

    public SubCommentsRecyclerAdapter(Comment comment, List<SubComment> commentList, Map profiles) {
        this.comment = comment;
        this.commentList = commentList;
        this.profiles = profiles;
    }

    public void setLayout(int layout){
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(context==null)
            context = parent.getContext();
        View view = null;
        if(viewType == 1){
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subcomment_item, parent, false);
        }
        if(viewType == 0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == 0){
            if(profiles.containsKey(comment.getFromId())){
                Profile profile = (Profile) profiles.get(comment.getFromId());
                Picasso.get()
                        .load(profile.getPhoto130())
                        .into(holder.ownerImage);
                holder.name.setText(profile.getFirstName()+ " " + profile.getLastName());
            } else {
                if(comment.getFromId()== SharedManager.getProperty(Constants.KEY_MY_ID)){
                    Picasso.get()
                            .load(SharedManager.getProperty(Constants.KEY_PHOTO_130))
                            .into(holder.ownerImage);
                    holder.name.setText(SharedManager.getProperty(Constants.KEY_MY_FULL_NAME));
                }
            }

            holder.time.setText(Util.getDatePretty(comment.getDate()));
            holder.text.setText(comment.getText());
            if(holder.likesCount!=null)
                holder.likesCount.setText(String.valueOf(comment.getLikes().getCount()));
            if(holder.likeIcon!=null){
                if(comment.getLikes().getUserLikes()==1){
                    holder.likeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_black_18dp));
                    holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.colorAccentRed));
                    holder.likesCount.setTextColor(context.getResources().getColor(R.color.colorAccentRed));
                } else {
                    holder.likeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_outline_grey600_18dp));
                    holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.black_30percent));
                    holder.likesCount.setTextColor(context.getResources().getColor(R.color.black_30percent));
                }
            }
            return;
        } else {
            position--;
        }

        if(profiles.containsKey(commentList.get(position).getFromId())){
            Profile profile = (Profile) profiles.get(commentList.get(position).getFromId());
            Picasso.get()
                    .load(profile.getPhoto130())
                    .into(holder.ownerImage);
            holder.name.setText(profile.getFirstName()+ " " + profile.getLastName());
        } else {
            if(commentList.get(position).getFromId()== SharedManager.getProperty(Constants.KEY_MY_ID)){
                Picasso.get()
                        .load(SharedManager.getProperty(Constants.KEY_PHOTO_130))
                        .into(holder.ownerImage);
                holder.name.setText(SharedManager.getProperty(Constants.KEY_MY_FULL_NAME));
            }
        }

        holder.time.setText(Util.getDatePretty(commentList.get(position).getDate()));
        holder.text.setText(commentList.get(position).getText());
        if(holder.likesCount!=null)
            holder.likesCount.setText(String.valueOf(commentList.get(position).getLikes().getCount()));
        if(holder.likeIcon!=null){
            if(commentList.get(position).getLikes().getUserLikes()==1){
                holder.likeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_black_18dp));
                holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.colorAccentRed));
                holder.likesCount.setTextColor(context.getResources().getColor(R.color.colorAccentRed));
            } else {
                holder.likeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_outline_grey600_18dp));
                holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.black_30percent));
                holder.likesCount.setTextColor(context.getResources().getColor(R.color.black_30percent));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0)
            return 0;
        return 1;
    }

    @Override
    public int getItemCount() {
        return commentList.size()+1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.likeIcon)
        ImageView likeIcon;
        @Nullable
        @BindView(R.id.commentPhoto)
        ImageView ownerImage;
        @BindView(R.id.commentName)
        TextView name;
        @BindView(R.id.commentPostTime)
        TextView time;
        @BindView(R.id.commentText)
        TextView text;
        @Nullable
        @BindView(R.id.likesCount)
        TextView likesCount;
        @BindView(R.id.commentsSettings)
        ImageView settings;
        @BindView(R.id.answerForComment)
        TextView answerForComment;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            likeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(getAdapterPosition()-1 == -1){
                        EventBus.getDefault().post(new EventBusMessages.LikeComment(
                                "",
                                "",
                                0));
                    } else {
                        EventBus.getDefault().post(new EventBusMessages.LikeSubcomment(
                                commentList.get(getAdapterPosition()-1).getId().toString(),
                                commentList.get(getAdapterPosition()-1).getOwnerId().toString(),
                                getAdapterPosition()-1,
                                0));
                    }
                }
            });
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(profiles.containsKey(commentList.get(getAdapterPosition()).getFromId())){
                        EventBus.getDefault().post(new EventBusMessages.OpenUser(((Profile) profiles.get(commentList.get(getAdapterPosition()).getFromId())).getId()));
                    }
                }
            };

            name.setOnClickListener(clickListener);
            ownerImage.setOnClickListener(clickListener);

            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.inflate(R.menu.popupmenu);
                    MenuItem delete = popupMenu.getMenu().findItem(R.id.deleteComment);
                    MenuItem complain = popupMenu.getMenu().findItem(R.id.complainComment);
                    if(commentList.get(getAdapterPosition()).getFromId().equals(SharedManager.getProperty(Constants.KEY_MY_ID))){
                    } else {
                        complain.setVisible(true);
                    }
                    if(commentList.get(getAdapterPosition()).getCanEdit()==1){
                        delete.setVisible(true);
                        complain.setVisible(true);
                    }
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.deleteComment:
                                    EventBus.getDefault().post(new EventBusMessages.DeleteComment(getAdapterPosition()));
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });

            answerForComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.ReplyToComment(
                            commentList.get(getAdapterPosition()).getId(),
                            commentList.get(getAdapterPosition()).getFromId(),
                            getAdapterPosition()));
                }
            });
        }
    }

    public void update(List<SubComment> comments, Map profiles){
        commentList = comments;
        this.profiles.putAll(profiles);
        notifyDataSetChanged();
    }
}
