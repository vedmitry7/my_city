package app.mycity.mycity.api.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Message {


    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("from_id")
    @Expose
    private Integer fromId;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("out")
    @Expose
    private Integer out;
    @SerializedName("date")
    @Expose
    private Integer date;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("read")
    @Expose
    private Integer read;
    @SerializedName("attachments")
    @Expose
    private List<MessageAttachment> messageAttachments = null;

    private boolean sendingHolder;

    long time;
    long user;
    boolean wasSended;
    boolean qw;

    public boolean isSendingHolder() {
        return sendingHolder;
    }

    public void setSendingHolder(boolean sendingHolder) {
        this.sendingHolder = sendingHolder;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setOut(Integer out) {
        this.out = out;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getRead() {
        return read;
    }

    public void setRead(Integer read) {
        this.read = read;
    }

    public List<MessageAttachment> getMessageAttachments() {
        return messageAttachments;
    }

    public void setMessageAttachments(List<MessageAttachment> messageAttachments) {
        this.messageAttachments = messageAttachments;
    }

    public boolean isWasSended() {
        return wasSended;
    }

    public void setWasSended(boolean wasSended) {
        this.wasSended = wasSended;
    }

    public boolean isQw() {
        return qw;
    }

    public void setQw(boolean qw) {
        this.qw = qw;
    }

    public int getOut() {
        return out;
    }

    public void setOut(int out) {
        this.out = out;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
