package app.mycity.mycity.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.CheckTokenResponse;
import app.mycity.mycity.api.model.Message;
import app.mycity.mycity.api.model.MessageAttachment;
import app.mycity.mycity.api.model.RefreshTokenResponse;
import app.mycity.mycity.api.model.ResponseAuth;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.activities.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocketService extends Service {

    private Socket mSocket;
    TimerTask timerTask;
    Timer timer;

    private void updateToken(){
        ApiFactory.getApi().updateToken(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), SharedManager.getProperty(Constants.KEY_REFRESH_TOKEN)).enqueue(new Callback<ResponseContainer<RefreshTokenResponse>>() {
            @Override
            public void onResponse(Call<ResponseContainer<RefreshTokenResponse>> call, Response<ResponseContainer<RefreshTokenResponse>> response) {
                RefreshTokenResponse tokenResponse = response.body().getResponse();
                if(tokenResponse!=null){
                    SharedManager.addProperty(Constants.KEY_ACCESS_TOKEN, tokenResponse.getAccessToken());
                    SharedManager.addProperty(Constants.KEY_REFRESH_TOKEN, tokenResponse.getRefreshToken());
                    SharedManager.addProperty(Constants.KEY_EXPIRED_AT, String.valueOf(tokenResponse.getExpiredAt()));
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<RefreshTokenResponse>> call, Throwable t) {

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if(!SharedManager.getBooleanProperty("login")){
            stopSelf();
            return;
        } else {
        }

        EventBus.getDefault().register(this);
        Intent ishintent = new Intent(this, SocketService.class);
        ishintent.putExtra("data", "alarm");
        PendingIntent pintent = PendingIntent.getService(this, 6, ishintent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),560000, pintent);
        checkToken();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startSubscribers(EventBusMessages.UpdateSocketConnection event) {
        timerTask.cancel();
        mSocket.off("history");
        mSocket.disconnect();
        mSocket.close();
        initSocket();
    }

    private void initSocket() {
        {
            try {
                if(SharedManager.getProperty("socketServer")!=null)
                    mSocket = IO.socket("http://" + SharedManager.getProperty("socketServer"));
            } catch (URISyntaxException e) {}
        }

        mSocket.connect();
        mSocket.off("history");

        JSONObject obj = new JSONObject();
        long l = 15350138831666L;
        try {
            obj.put("hash", SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN));
            if(SharedManager.getProperty("ts")==null){
                SharedManager.addProperty("ts", String.valueOf(l));
            }
            if(SharedManager.getProperty("ts")!=null){
                obj.put("ts", Long.parseLong(SharedManager.getProperty("ts")));
            }
            else{
                obj.put("ts", l);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("auth", obj);

        Emitter.Listener listener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String history = "" + args[0];
                chatResponse2(history);
            }
        };

        mSocket.on("history", listener);

        final boolean[] wasLost = new boolean[1];

        timerTask = new TimerTask() {
            @Override
            public void run() {

                if(!mSocket.connected()){
                    wasLost[0] = true;
                } else {
                    if(wasLost[0]){
                       wasLost[0] = false;
                        mSocket.off("history");
                        mSocket.disconnect();
                        mSocket.close();
                        this.cancel();
                        checkToken();
                    }
                }
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 10000, 5000);
    }

    void chatResponse2(String responseString){

        JSONObject jsonObject = null;
        JSONArray jsonArray = null;

        try {
            jsonObject = new JSONObject(responseString);

            jsonArray = jsonObject.getJSONArray("history");
            String ts = jsonObject.getString("ts");
            SharedManager.addProperty("ts", ts);
            int dialogId = 0;
            String messageText = "";
            int unreadCount;
            int date;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);

                String info = null;
                switch (array.getInt(0)){
                    case 1: info = "new message   "; break;
                    case 2: info = "was read      "; break;
                    case 5: info = "dialog update "; break;
                    case 6: info = "read dialog   "; break;
                    case 7: info = "общее кол-во непрочит. "; break;
                    case 21: info = "notification";
                }

                int type = array.getInt(0);
                Message message;
                switch (type){
                    case 1:
                        JSONObject object = new JSONObject(array.getString(5));
                        JSONArray attArray;
                        attArray = object.has("attachments") ? object.getJSONArray("attachments") : null;
                        message = new Message();
                        message.setId(object.getInt("id"));
                        message.setUser(object.getInt("user_id"));
                        message.setTime(array.getLong(1));
                        message.setText(object.getString("text"));
                        message.setOut(object.getInt("out"));
                        message.setWasSended(true);
                        message.setRead(0);

                        if(attArray!=null){
                            JSONObject photo = attArray.getJSONObject(0);
                            MessageAttachment messageAttachment = new MessageAttachment();
                            messageAttachment.setPhoto360(photo.getString("photo_360"));
                            List<MessageAttachment> list = new ArrayList<>();
                            list.add(messageAttachment);
                            message.setMessageAttachments(list);
                        }
                        EventBus.getDefault().post(new EventBusMessages.NewChatMessage(message, object.getInt("out")));
                        break;
                    case 2:
                        int messageId = array.getInt(4);
                        EventBus.getDefault().post(new EventBusMessages.MessageWasRead(messageId));
                        break;
                    case 5:
                        dialogId = array.getInt(2);
                        messageText = array.getString(5);
                        break;
                    case 6:
                        unreadCount = array.getInt(4);
                        date = array.getInt(1);
                            EventBus.getDefault().post(new EventBusMessages.DialogUpdate(String.valueOf(dialogId), messageText, unreadCount, date));
                        break;
                    case 7:
                        SharedManager.addBooleanProperty("unreadMessages", true);
                        EventBus.getDefault().postSticky(new EventBusMessages.UnreadCountUpdate());
                        break;
                    case 21:
                        String event = array.getString(2);
                        JSONObject obj = array.getJSONObject(3);
                        String name = obj.getString("first_name") + " " + obj.getString("last_name");
                        switch (event){
                            case "follow":{
                                generateNotification("На вас подписались", name);
                                break;
                            }
                            case "like_post":{
                                generateNotification("Ваш чекин оценили", name);
                                break;
                            }
                            case "like_comment":{
                                generateNotification("Ваш комментарий оценили", name);
                                break;
                            }
                            case "comment_post":{
                                generateNotification("Комментарий к чекину", name);
                                break;
                            }
                        }
                        break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void generateNotification(String text, String userName) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_main)
                        .setContentTitle("S " + text)
                        .setContentText("S " + userName);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        long[] vibrate = { 0, 200, 100, 100, 100, 50};
        mBuilder.setVibrate(vibrate);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("data", "follow");
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        mBuilder.setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_ONE_SHOT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(25, mBuilder.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!SharedManager.getBooleanProperty("login")){
            stopSelf();
        } else {
            if(mSocket==null){
                checkToken();
            }
        }
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if(timerTask!=null){
            timerTask.cancel();
            timer.cancel();
        }
        if(mSocket!=null){
            mSocket.off("history");
            mSocket.disconnect();
            mSocket.close();
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
    }

    private void checkToken() {
        ApiFactory.getApi().checkToken(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN)).enqueue(new Callback<ResponseContainer<CheckTokenResponse>>() {
            @Override
            public void onResponse(Call<ResponseContainer<CheckTokenResponse>> call, Response<ResponseContainer<CheckTokenResponse>> response) {
                if(response.body().getResponse()!=null){
                    if(response.body().getResponse().getSuccess()==1){
                        initSocket();
                    }
                } else {
                    authorize();
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<CheckTokenResponse>> call, Throwable t) {
            }
        });
    }

    private void authorize() {
        ApiFactory.getApi().authorize(SharedManager.getProperty(Constants.KEY_LOGIN), SharedManager.getProperty(Constants.KEY_PASSWORD)).enqueue(new retrofit2.Callback<ResponseContainer<ResponseAuth>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<ResponseAuth>> call, retrofit2.Response<ResponseContainer<ResponseAuth>> response) {
                ResponseAuth responseAuth = response.body().getResponse();
                if(responseAuth != null){
                    SharedManager.addProperty(Constants.KEY_MY_ID, responseAuth.getUserId());
                    SharedManager.addProperty(Constants.KEY_ACCESS_TOKEN, responseAuth.getAccessToken());
                    SharedManager.addProperty(Constants.KEY_REFRESH_TOKEN, responseAuth.getRefreshToken());
                    SharedManager.addProperty(Constants.KEY_EXPIRED_AT, responseAuth.getExpiredAt());

                    initSocket();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<ResponseAuth>> call, Throwable t) {
            }
        });
    }
}
