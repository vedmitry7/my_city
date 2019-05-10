package app.mycity.mycity.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.views.activities.MainActivity;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map data = remoteMessage.getData();
        if (data != null) {
            String type = (String) data.get("type");
            String peerId = (String) data.get("peer_id");
            switch (type) {
                case "new_message":
                    if (!App.checkPeerId(peerId)) {
                        sendMessageNotification(remoteMessage, type);
                    }
                    break;
                case Constants.KEY_LIKE_POST:
                    sendMessageNotification(remoteMessage, type);
                    break;
                case Constants.KEY_LIKE_COMMENT:
                    sendMessageNotification(remoteMessage, type);
                    break;
                case Constants.KEY_COMMENT_POST:
                    sendMessageNotification(remoteMessage, type);
                    break;
                case Constants.KEY_FOLLOW:
                    sendMessageNotification(remoteMessage, type);
                    break;
                case Constants.KEY_PLACE_EVENT:
                    sendMessageNotification(remoteMessage, type);
                    break;
                case Constants.KEY_PLACE_ACTION:
                    sendMessageNotification(remoteMessage, type);
                    break;
            }

        }
    }

    private void sendMessageNotification(RemoteMessage remoteMessage, String type) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_main)
                        .setContentTitle("F " + remoteMessage.getNotification().getTitle())
                        .setContentText("F " + remoteMessage.getNotification().getBody());
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        long[] vibrate = { 0, 200, 100, 100, 100, 50};
        mBuilder.setVibrate(vibrate);

        Map data = remoteMessage.getData();

        Intent resultIntent = null;

        switch (type){
            case Constants.KEY_NEW_MESSAGE:
                resultIntent = new Intent(this, MainActivity.class);
                String peerId = (String) data.get("peer_id");
                resultIntent.putExtra("peer_id", peerId);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                break;
            case Constants.KEY_LIKE_POST:
                resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("type", type);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                break;
            case Constants.KEY_PLACE_EVENT:
                resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("type", type);
                resultIntent.putExtra("group_id", (String) data.get("group_id"));
                resultIntent.putExtra("event_id", (String) data.get("event_id"));
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                break;
            case Constants.KEY_PLACE_ACTION:
                resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("type", type);
                resultIntent.putExtra("group_id", (String) data.get("group_id"));
                resultIntent.putExtra("event_id", (String) data.get("action_id"));
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                break;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_ONE_SHOT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String nId = (String) data.get("peer_id");
        if(nId==null){
            nId = "255";
        }
        mNotificationManager.notify(Integer.parseInt(nId), mBuilder.build());

    }
}
