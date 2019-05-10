package app.mycity.mycity;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDex;
import android.view.inputmethod.InputMethodManager;
import com.google.firebase.FirebaseApp;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.activities.MainActivity;

public class App extends Application {

    static Activity activity;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        SharedManager.init(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    public static void closeKeyboard(Context context){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void showKeyboard(Context context){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static boolean checkPeerId(String peerId){
        if(activity instanceof MainActivity){
            return ((MainActivity)activity).checkPeerId(peerId);
        }
        return false;
    }

    public static String accessToken() {
        return SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN);
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public static int chosenCity() {
        return SharedManager.getIntProperty("chosen_city_id");
    }
}
