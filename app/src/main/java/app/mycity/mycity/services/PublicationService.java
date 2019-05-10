package app.mycity.mycity.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponsePostPhoto;
import app.mycity.mycity.api.model.ResponseSavePhoto;
import app.mycity.mycity.api.model.ResponseUploadServer;
import app.mycity.mycity.api.model.ResponseUploading;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static okhttp3.RequestBody.create;


public class PublicationService extends Service {

    String path;
    File file;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        path = intent.getStringExtra("path");
        file = new File(path);
        post();
        return START_NOT_STICKY;
    }

    public void post(){
        getUploadServer();
    }

    void getUploadServer(){
        ApiFactory.getApi().getUploadPhotoServer(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN)).enqueue(new Callback<ResponseContainer<ResponseUploadServer>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseUploadServer>> call, Response<ResponseContainer<ResponseUploadServer>> response) {
                ResponseUploadServer uploadServer = response.body().getResponse();
                uploadFile(uploadServer.getBaseUrl());
            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseUploadServer>> call, Throwable t) {
                EventBus.getDefault().postSticky(new EventBusMessages.PublicationError());
            }
        });
    }

    private void uploadFile(String server) {
        final MultipartBody.Part filePart = MultipartBody.Part.createFormData("0", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
        final RequestBody action = RequestBody.create(MediaType.parse("text/plain"), "add_photo");
        final RequestBody id = RequestBody.create(MediaType.parse("text/plain"), SharedManager.getProperty(Constants.KEY_MY_ID));
        ApiFactory.getApiUploadServer(server).uploadPhoto(action, id, filePart).enqueue(new Callback<ResponseContainer<ResponseUploading>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseUploading>> call, Response<ResponseContainer<ResponseUploading>> response) {
                ResponseUploading uploading = response.body().getResponse();
                JSONArray array = new JSONArray();
                for (int i = 0; i < uploading.getPhotoList().size(); i++) {
                    try {
                        array.put(i, uploading.getPhotoList().get(i).getJson());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                savePhoto(array.toString(), uploading.getServer());
            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseUploading>> call, Throwable t) {
                EventBus.getDefault().postSticky(new EventBusMessages.PublicationError());
            }
        });
    }

    void savePhoto(String string, String server){

        ApiFactory.getApi().savePhoto(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), string, "1", server)
                .enqueue(new Callback<ResponseContainer<ResponseSavePhoto>>() {
                    @Override
                    public void onResponse(Call<ResponseContainer<ResponseSavePhoto>> call, Response<ResponseContainer<ResponseSavePhoto>> response) {

                        ResponseSavePhoto savePhoto = response.body().getResponse();
                        String attachment = "photo" + SharedManager.getProperty(Constants.KEY_MY_ID) + "_" + savePhoto.getPhotoId();
                        postPicture(attachment);
                    }

                    @Override
                    public void onFailure(Call<ResponseContainer<ResponseSavePhoto>> call, Throwable t) {
                        EventBus.getDefault().postSticky(new EventBusMessages.PublicationError());
                    }
                });
    }

    private void postPicture(String attachment) {
        RequestBody token = create(
                MediaType.parse("text/plain"),
                SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN));

        RequestBody placeId = create(
                MediaType.parse("text/plain"), SharedManager.getProperty("currentPlace"));
        RequestBody message = create(
        MediaType.parse("text/plain"), "");
        RequestBody att = create(
                MediaType.parse("text/plain"), attachment);

        try {
            ApiFactory.getApi().postPicture(token, placeId, message, att).enqueue(new Callback<ResponseContainer<ResponsePostPhoto>>() {
                @Override
                public void onResponse(Call<ResponseContainer<ResponsePostPhoto>> call, Response<ResponseContainer<ResponsePostPhoto>> response) {
                    if(response.body().getResponse().getSuccess()==1)
                    EventBus.getDefault().postSticky(new EventBusMessages.PublicationComplete());
                }

                @Override
                public void onFailure(Call<ResponseContainer<ResponsePostPhoto>> call, Throwable t) {
                    EventBus.getDefault().postSticky(new EventBusMessages.PublicationError());
                }
            });
        } catch (Exception e){
        }
    }
}
