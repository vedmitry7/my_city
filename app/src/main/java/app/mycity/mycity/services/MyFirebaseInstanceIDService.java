package app.mycity.mycity.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import app.mycity.mycity.util.SharedManager;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token  = FirebaseInstanceId.getInstance().getToken();
        SharedManager.addProperty("fcm_token", token);
        sendRegistrationTokenToServer(token);
    }

    private void sendRegistrationTokenToServer(String token) {
    }
}