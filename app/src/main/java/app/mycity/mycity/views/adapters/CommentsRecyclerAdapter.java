package app.mycity.mycity.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    List<Comment> commentList;
    Map profiles;

    int layout;
    Context context;

    public CommentsRecyclerAdapter(List<Comment> commentList, Map profiles) {
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

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


        if(commentList.get(position).getSubComments().getItems().size()==0){
            holder.lookMore.setVisibility(View.GONE);
            holder.firstSubCommentContainer.setVisibility(View.GONE);
            holder.secondSubCommentContainer.setVisibility(View.GONE);
        } else {

            holder.secondSubCommentContainer.setVisibility(View.GONE);

            SubComment subComment = commentList.get(position).getSubComments().getItems().get(0);
            holder.firstSubCommentContainer.setVisibility(View.VISIBLE);
            holder.firstSubCommentText.setText(subComment.getText());
            holder.firstSubCommentTime.setText(Util.getDatePretty(subComment.getDate()));

            if(profiles.containsKey(subComment.getFromId())){
                Profile profile = (Profile) profiles.get(subComment.getFromId());
                Picasso.get()
                        .load(profile.getPhoto130())
                        .into(holder.firstSubCommentOwnerImage);
                holder.firstSubCommentName.setText(profile.getFirstName()+ " " + profile.getLastName());
            }

            holder.firstSubCommentLikesCount.setText(String.valueOf(commentList.get(position).getSubComments().getItems().get(0).getLikes().getCount()));

            if(commentList.get(position).getSubComments().getItems().get(0).getLikes().getUserLikes()==1){
                holder.firstSubCommentLikeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_black_18dp));
                holder.firstSubCommentLikeIcon.setColorFilter(context.getResources().getColor(R.color.colorAccentRed));
                holder.firstSubCommentLikesCount.setTextColor(context.getResources().getColor(R.color.colorAccentRed));
            } else {
                holder.firstSubCommentLikeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_outline_grey600_18dp));
                holder.firstSubCommentLikeIcon.setColorFilter(context.getResources().getColor(R.color.black_30percent));
                holder.firstSubCommentLikesCount.setTextColor(context.getResources().getColor(R.color.black_30percent));
            }

            if(commentList.get(position).getSubComments().getItems().size()>1){
                subComment = commentList.get(position).getSubComments().getItems().get(1);
                holder.secondSubCommentContainer.setVisibility(View.VISIBLE);
                holder.secondSubCommentText.setText(subComment.getText());
                holder.secondSubCommentTime.setText(Util.getDatePretty(subComment.getDate()));

                if(profiles.containsKey(subComment.getFromId())){
                    Profile profile = (Profile) profiles.get(subComment.getFromId());
                    Picasso.get()
                            .load(profile.getPhoto130())
                            .into(holder.secondSubCommentOwnerImage);

                    holder.secondSubCommentName.setText(profile.getFirstName()+ " " + profile.getLastName());
                }

                holder.secondSubCommentLikesCount.setText(String.valueOf(commentList.get(position).getSubComments().getItems().get(1).getLikes().getCount()));

                if(commentList.get(position).getSubComments().getItems().get(1).getLikes().getUserLikes()==1){
                    holder.secondSubCommentLikeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_black_18dp));
                    holder.secondSubCommentLikeIcon.setColorFilter(context.getResources().getColor(R.color.colorAccentRed));
                    holder.secondSubCommentLikesCount.setTextColor(context.getResources().getColor(R.color.colorAccentRed));
                } else {
                    holder.secondSubCommentLikeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_outline_grey600_18dp));
                    holder.secondSubCommentLikeIcon.setColorFilter(context.getResources().getColor(R.color.black_30percent));
                    holder.secondSubCommentLikesCount.setTextColor(context.getResources().getColor(R.color.black_30percent));
                }
            }

            if(commentList.get(position).getSubComments().getItems().size()>2 || commentList.get(position).getSubComments().getCount()>2) {
                holder.lookMore.setVisibility(View.VISIBLE);
            }


        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
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
        @BindView(R.id.firstSubCommentContainer)
        ConstraintLayout firstSubCommentContainer;
        @Nullable
        @BindView(R.id.firstSubCommentLikeIcon)
        ImageView firstSubCommentLikeIcon;
        @Nullable
        @BindView(R.id.firstSubCommentPhoto)
        ImageView firstSubCommentOwnerImage;
        @BindView(R.id.firstSubCommentName)
        TextView firstSubCommentName;
        @BindView(R.id.firstSubCommentTime)
        TextView  firstSubCommentTime;
        @BindView(R.id.firstSubCommentText)
        TextView  firstSubCommentText;
        @Nullable
        @BindView(R.id.firstSubCommentLikesCount)
        TextView  firstSubCommentLikesCount;
        @BindView(R.id.firstSubCommentsSettings)
        ImageView  firstSubCommentSettings;
        @BindView(R.id.answerForFirstSubComment)
        TextView answerForFirstSubComment;
        @BindView(R.id.secondSubCommentContainer)
        ConstraintLayout secondSubCommentContainer;
        @Nullable
        @BindView(R.id.secondSubCommentLikeIcon)
        ImageView secondSubCommentLikeIcon;
        @Nullable
        @BindView(R.id.secondSubCommentPhoto)
        ImageView secondSubCommentOwnerImage;
        @BindView(R.id.secondSubCommentName)
        TextView secondSubCommentName;
        @BindView(R.id.secondSubCommentTime)
        TextView  secondSubCommentTime;
        @BindView(R.id.secondSubCommentText)
        TextView  secondSubCommentText;
        @Nullable
        @BindView(R.id.secondSubCommentLikesCount)
        TextView secondSubCommentLikesCount;
        @BindView(R.id.secondSubCommentsSettings)
        ImageView  secondSubCommentSettings;
        @BindView(R.id.answerForSecondSubComment)
        TextView answerForSecondSubComment;
        @BindView(R.id.lookMore)
        TextView lookMore;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            likeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.LikeComment(
                            commentList.get(getAdapterPosition()).getId().toString(),
                            commentList.get(getAdapterPosition()).getOwnerId().toString(),
                            getAdapterPosition()));
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
                    if(!commentList.get(getAdapterPosition()).getFromId().equals(SharedManager.getProperty(Constants.KEY_MY_ID))) {
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
            answerForFirstSubComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.ReplyToComment(
                            commentList.get(getAdapterPosition()).getSubComments().getItems().get(0).getId(),
                            commentList.get(getAdapterPosition()).getSubComments().getItems().get(0).getFromId(),
                            getAdapterPosition()));
                }
            });
            answerForSecondSubComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.ReplyToComment(
                            commentList.get(getAdapterPosition()).getSubComments().getItems().get(1).getId(),
                            commentList.get(getAdapterPosition()).getSubComments().getItems().get(1).getFromId(),
                            getAdapterPosition()));
                }
            });

            lookMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.StoreComment(getAdapterPosition()));
                    EventBus.getDefault().post(new EventBusMessages.OpenSubcomments(
                            commentList.get(getAdapterPosition()).getPostId(),
                            commentList.get(getAdapterPosition()).getOwnerId(),
                            "post",
                            commentList.get(getAdapterPosition()).getId())
                    );
                }
            });

            firstSubCommentLikeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.LikeSubcomment(
                            commentList.get(getAdapterPosition()).getId().toString(),
                            commentList.get(getAdapterPosition()).getOwnerId().toString(),
                            getAdapterPosition(), 0));
                }
            });

            secondSubCommentLikeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new EventBusMessages.LikeSubcomment(
                            commentList.get(getAdapterPosition()).getId().toString(),
                            commentList.get(getAdapterPosition()).getOwnerId().toString(),
                            getAdapterPosition(), 1));
                }
            });
        }
    }

    public void update(List<Comment> comments, Map profiles){
        commentList = comments;
        this.profiles.putAll(profiles);
        notifyDataSetChanged();
    }
}
