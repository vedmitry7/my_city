package app.mycity.mycity.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SubComment {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("owner_id")
    @Expose
    private String ownerId;
    @SerializedName("post_id")
    @Expose
    private String postId;
    @SerializedName("from_id")
    @Expose
    private String fromId;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("date")
    @Expose
    private Integer date;
    @SerializedName("can_edit")
    @Expose
    private Integer canEdit;
    @SerializedName("likes")
    @Expose
    private Likes likes;
    @SerializedName("reply_to_user")
    @Expose
    private Integer replyToUser;
    @SerializedName("reply_to_comment")
    @Expose
    private Integer replyToComment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Integer canEdit) {
        this.canEdit = canEdit;
    }

    public Likes getLikes() {
        return likes;
    }

    public void setLikes(Likes likes) {
        this.likes = likes;
    }

    public Integer getReplyToUser() {
        return replyToUser;
    }

    public void setReplyToUser(Integer replyToUser) {
        this.replyToUser = replyToUser;
    }

    public Integer getReplyToComment() {
        return replyToComment;
    }

    public void setReplyToComment(Integer replyToComment) {
        this.replyToComment = replyToComment;
    }

}
