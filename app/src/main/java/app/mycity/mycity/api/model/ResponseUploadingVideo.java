package app.mycity.mycity.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseUploadingVideo {


    @SerializedName("server")
    @Expose
    private String server;
    @SerializedName("video_list")
    @Expose
    private List<VideoList> videoList = null;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("hash")
    @Expose
    private String hash;
    @SerializedName("date")
    @Expose
    private Integer date;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<VideoList> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<VideoList> videoList) {
        this.videoList = videoList;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

}
