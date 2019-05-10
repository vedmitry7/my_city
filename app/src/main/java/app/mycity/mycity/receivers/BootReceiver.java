package app.mycity.mycity.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import app.mycity.mycity.App;
import app.mycity.mycity.services.SocketService;
import app.mycity.mycity.util.EventBusMessages;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(App.isOnline(context)) {
            EventBus.getDefault().post(new EventBusMessages.LocationResume());
            Intent serviceIntent = new Intent(context, SocketService.class);
            serviceIntent.putExtra("data", "receiver");
            context.startService(serviceIntent);
        } else {
            EventBus.getDefault().post(new EventBusMessages.LocationStop());
        }
    }
}