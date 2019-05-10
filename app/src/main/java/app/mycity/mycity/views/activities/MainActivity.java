package app.mycity.mycity.views.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.services.SocketService;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.SuccessCreatePlace;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponsePlaces;
import app.mycity.mycity.api.model.ResponseSocketServer;
import app.mycity.mycity.api.model.Success;
import app.mycity.mycity.filter_desc_post.FilterImageActivity;
import app.mycity.mycity.filter_desc_post.VideoActivity;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.fragments.ClaimsBottomSheetFragment;
import app.mycity.mycity.views.adapters.PlacesByCoordinatesRecyclerAdapter;
import app.mycity.mycity.views.fragments.ShareFragment;
import app.mycity.mycity.views.fragments.ChatFragment;
import app.mycity.mycity.views.fragments.CommentsFragment;
import app.mycity.mycity.views.fragments.DialogsFragment;
import app.mycity.mycity.views.fragments.LoseConnectionFragment;
import app.mycity.mycity.views.fragments.MapFragment;
import app.mycity.mycity.views.fragments.MenuFragment;
import app.mycity.mycity.views.fragments.NotificationsFragment;
import app.mycity.mycity.views.fragments.ShowImageFragment;
import app.mycity.mycity.views.fragments.SubCommentsFragment;
import app.mycity.mycity.views.fragments.events.ActionContentFragment;
import app.mycity.mycity.views.fragments.events.EventContentFragment;
import app.mycity.mycity.views.fragments.events.EventsFragment;
import app.mycity.mycity.views.fragments.events.ServiceContentFragment;
import app.mycity.mycity.views.fragments.events.ServicesFragment;
import app.mycity.mycity.views.fragments.feed.ChronicsFragment;
import app.mycity.mycity.views.fragments.feed.FeedFragment;
import app.mycity.mycity.views.fragments.feed.FeedPhotoReportFragmentContent;
import app.mycity.mycity.views.fragments.feed.FeedPlacesCheckinFragment;
import app.mycity.mycity.views.fragments.feed.PhotoReportFragment;
import app.mycity.mycity.views.fragments.places.PlaceFragment;
import app.mycity.mycity.views.fragments.places.PlaceSubscribersFragment;
import app.mycity.mycity.views.fragments.places.PlacesFragment;
import app.mycity.mycity.views.fragments.places.UsersInPlaceFragment;
import app.mycity.mycity.views.fragments.profile.ProfileCheckinContent;
import app.mycity.mycity.views.fragments.profile.ProfileCheckinContentOne;
import app.mycity.mycity.views.fragments.profile.ProfileEvents;
import app.mycity.mycity.views.fragments.profile.ProfileFragment;
import app.mycity.mycity.views.fragments.profile.SomeoneProfileFragment;
import app.mycity.mycity.views.fragments.profile.UserPlacesFragment;
import app.mycity.mycity.views.fragments.settings.BlackListFragment;
import app.mycity.mycity.views.fragments.settings.MainSettingsFragment;
import app.mycity.mycity.views.fragments.settings.NotificationSettingsFragment;
import app.mycity.mycity.views.fragments.settings.ProfileSettingsFragment;
import app.mycity.mycity.views.fragments.subscribers.SubscribersFragment;
import app.mycity.mycity.views.fragments.subscribers.SubscriptionFragment;
import app.mycity.mycity.views.fragments.people.PeoplesFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Storage {

    @BindView(R.id.choseMediaTypeContainer)
    CardView choseMediaTypeContainer;

    private TabStacker mTabStacker;
    HashMap<String, Object> date = new HashMap<>();
    android.support.v4.app.FragmentManager fragmentManager;
    private boolean video;
    Timer hideButtonsTimer = new Timer();
    private boolean startProfile;
    private int REQUEST_MAKE_CHECKIN = 24;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        ButterKnife.bind(this);
        choseMediaTypeContainer.setBackgroundResource(R.drawable.chose_media_type);
        choseMediaTypeContainer.setVisibility(View.GONE);

        setStatusBarColor();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult().getToken();
                        registrationDevice(token);
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("profile" + SharedManager.getProperty(Constants.KEY_MY_ID));
        FirebaseMessaging.getInstance().subscribeToTopic("news");

        updateTimestemp();


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent serviceIntent = new Intent(MainActivity.this, SocketService.class);
                serviceIntent.putExtra("data", "activity");
                startService(serviceIntent);
            }
        }, 500);


        ButterKnife.bind(this);

        fragmentManager = getSupportFragmentManager();
        mTabStacker = new TabStacker(getSupportFragmentManager(), R.id.main_act_fragment_container);

        openMenuFragment(new EventBusMessages.OpenMenu());

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            if(extras.getString("type")!=null){
                switch (extras.getString("type")){
                    case Constants.KEY_FOLLOW:
                        openNotifications(new EventBusMessages.OpenNotifications());
                        break;
                    case Constants.KEY_LIKE_POST:
                        openNotifications(new EventBusMessages.OpenNotifications());
                        break;
                    case Constants.KEY_LIKE_COMMENT:
                        openNotifications(new EventBusMessages.OpenNotifications());
                        break;
                    case Constants.KEY_COMMENT_POST:
                        openNotifications(new EventBusMessages.OpenNotifications());
                        break;
                    case Constants.KEY_PLACE_EVENT:
                        String placeId = extras.getString("group_id");
                        String eventId = extras.getString("event_id");
                        openEventContent(new EventBusMessages.OpenEventContent(
                                eventId,
                                placeId,
                                true));
                        break;
                    case Constants.KEY_PLACE_ACTION:
                        String groupId = extras.getString("group_id");
                        String actionId = extras.getString("action_id");
                        openActionContent(new EventBusMessages.OpenActionContent(
                                actionId,
                                groupId,
                                true));
                        break;
                }
            }

            Uri data = this.getIntent().getData();
            if (data != null && data.isHierarchical()) {
                String uri = this.getIntent().getDataString();
                String type = "";
                String objectId;
                String ownerId;

                int pos = uri.lastIndexOf('/')+1;
                String res = uri.substring(pos, uri.length());

                if(res.startsWith(Constants.KEY_POST)){
                    type = Constants.KEY_POST;
                }
                if(res.startsWith(Constants.KEY_ALBUM)){
                    type = Constants.KEY_ALBUM;
                }
                if(res.startsWith(Constants.KEY_EVENT)){
                    type = Constants.KEY_EVENT;
                }
                if(res.startsWith(Constants.KEY_ACTION)){
                    type = Constants.KEY_ACTION;
                }
                if(res.startsWith(Constants.KEY_SERVICE)){
                    type = Constants.KEY_SERVICE;
                }
                ownerId = res.substring(type.length(), res.lastIndexOf("_"));
                objectId = res.substring(res.lastIndexOf("_")+1, res.length());

                switch (type){
                    case Constants.KEY_EVENT:
                        openEventContent(new EventBusMessages.OpenEventContent(
                                objectId,
                                ownerId,
                                true));
                        break;
                    case Constants.KEY_ACTION:
                        openActionContent(new EventBusMessages.OpenActionContent(
                                objectId,
                                ownerId,
                                true));
                        break;
                    case Constants.KEY_SERVICE:
                        openServiceContent(new EventBusMessages.OpenServiceContent(
                                objectId,
                                ownerId,
                                true));
                        break;
                    case Constants.KEY_POST:
                        checkinContentOne(new EventBusMessages.ProfileCheckinContentOne(objectId, true));
                        break;

                    case Constants.KEY_ALBUM:
                        openPhotoReportContent(new EventBusMessages.OpenPhotoReportContent(
                                objectId));
                        break;
                }
            }
        }
        if(getIntent().getStringExtra("peer_id")!=null){
            openChat(new EventBusMessages.OpenChat(getIntent().getStringExtra("peer_id")));
        }
    }

    @Override
    protected void onResume() {
        if(startProfile){
            if(mTabStacker.getCurrentTopFragment() instanceof ProfileFragment){
                mTabStacker.onBackPressed();
                openUser(new EventBusMessages.OpenUser(SharedManager.getProperty(Constants.KEY_MY_ID)));
            } else {
                openUser(new EventBusMessages.OpenUser(SharedManager.getProperty(Constants.KEY_MY_ID)));
                startProfile = false;
            }
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 74){
            if(resultCode == RESULT_OK){
                mTabStacker.getCurrentTopFragment().onActivityResult(requestCode, resultCode, data);
            }
        }
        if (requestCode == REQUEST_MAKE_CHECKIN) {
            if(resultCode == RESULT_OK){
                startProfile = true;
            }
        }

        if(requestCode == 122 || requestCode == 123){
            mTabStacker.getCurrentTopFragment().onActivityResult(requestCode, resultCode, data);
        }
    }


    private void registrationDevice(String token) {

        if(!SharedManager.getBooleanProperty("deviceRegister")){
            String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            ApiFactory.getApi().registerDevice(App.accessToken(), androidID, token).enqueue(new Callback<ResponseContainer<Success>>() {
                @Override
                public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                    if(response.body().getResponse().getSuccess()==1){
                        SharedManager.addBooleanProperty("deviceRegister", true);
                    }
                }
                @Override
                public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {
                }
            });
        }
    }

    public void setStatusBarColor() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            int statusBarColor = ContextCompat.getColor(this,R.color.colorAccent);
            if (statusBarColor == Color.BLACK && window.getNavigationBarColor() == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setStatusBarColor(statusBarColor);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void blackStatusBar(EventBusMessages.BlackStatusBar event){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void colorStatusBar(EventBusMessages.DefaultStatusBar event){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private void updateTimestemp() {
        ApiFactory.getApi().getSocketServer(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN)).enqueue(new Callback<ResponseContainer<ResponseSocketServer>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseSocketServer>> call, Response<ResponseContainer<ResponseSocketServer>> response) {
                if(response.body()!=null){
                    SharedManager.addProperty("ts", response.body().getResponse().getTs());
                    SharedManager.addProperty("socketServer", response.body().getResponse().getServer());
                }
            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseSocketServer>> call, Throwable t) {

            }
        });
    }

    private void makePlaceRequestAndMakeCheckin(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Определение местоположения");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.asking_place_dialog, null);
        final RecyclerView recyclerView = dialogView.findViewById(R.id.placesRecyclerView);
        final List placeList = new ArrayList<>();

        final PlacesByCoordinatesRecyclerAdapter adapter = new PlacesByCoordinatesRecyclerAdapter(placeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        final ProgressBar progress = dialogView.findViewById(R.id.placesDialogProgress);
        final TextView message = dialogView.findViewById(R.id.placeDialogMessage);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                String lat = SharedManager.getProperty("latitude");
                String lon = SharedManager.getProperty("longitude");

                if(SharedManager.getProperty("latitude")==null){
                    lat = "48.5685831";
                    lon = "39.2961263";
                }

                ApiFactory.getApi().getPlaceByCoordinates(SharedManager.getProperty(Constants.KEY_ACCESS_TOKEN), lat, lon, 300, 0).enqueue(new Callback<ResponseContainer<ResponsePlaces>>() {
                    @Override
                    public void onResponse(Call<ResponseContainer<ResponsePlaces>> call, Response<ResponseContainer<ResponsePlaces>> response) {
                        if(response.body()!=null && response.body().getResponse().getItems()!=null){

                            progress.setVisibility(View.GONE);
                            if(response.body().getResponse().getCount()==0){
                                message.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                            placeList.addAll(response.body().getResponse().getItems());
                            adapter.update(placeList);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseContainer<ResponsePlaces>> call, Throwable t) {
                    }
                });
            }
        }, 1000);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SharedManager.addProperty("currentPlace", adapter.getSelectedPlaceId());

                if(placeList.size()>0){

                    if(video){
                        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                        startActivityForResult(intent, 24);
                    } else {
                        Intent intent = new Intent(MainActivity.this, FilterImageActivity.class);
                        startActivityForResult(intent, 24);
                    }
                    Dexter.withActivity(MainActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.LOCATION_HARDWARE)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    if (report.areAllPermissionsGranted()) {

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();

                }
                dialog.dismiss();
            }
        });

        final AlertDialog b = dialogBuilder.create();
        b.setButton(AlertDialog.BUTTON_NEUTRAL, "Создать место", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {


                b.dismiss();

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this)
                        .setPositiveButton("Ok", null);

                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View dialogView = inflater.inflate(R.layout.create_location_dialog, null);

                final TextInputLayout textInputLayout = dialogView.findViewById(R.id.textInputLayout);
                final EditText createPlaceEt = dialogView.findViewById(R.id.createPlaceEt);


                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("Создание места");

                final AlertDialog c = dialogBuilder.create();
                c.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        c.dismiss();
                        App.closeKeyboard(getApplicationContext());
                    }
                });

                c.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(createPlaceEt.getText().toString().length()<3){
                                    textInputLayout.setError("Не менее трех символов");
                                    return;
                                }

                                String lat = SharedManager.getProperty("latitude");
                                String lon = SharedManager.getProperty("longitude");
                                ApiFactory.getApi().createPlace(App.accessToken(), createPlaceEt.getText().toString().trim(), SharedManager.getIntProperty("chosen_city_id"), lat, lon).enqueue(new Callback<ResponseContainer<SuccessCreatePlace>>() {
                                    @Override
                                    public void onResponse(Call<ResponseContainer<SuccessCreatePlace>> call, Response<ResponseContainer<SuccessCreatePlace>> response) {
                                        if(response.body()!=null){

                                            if(response.body().getError()!=null){
                                                textInputLayout.setError("Такая локация уже существует");

                                                return;
                                            }
                                            if(response.body().getResponse().getSuccess()==1){
                                                SharedManager.addProperty("currentPlace", response.body().getResponse().getId());
                                                if(video){
                                                    Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                                                    startActivityForResult(intent, REQUEST_MAKE_CHECKIN);
                                                } else {
                                                    Intent intent = new Intent(MainActivity.this, FilterImageActivity.class);
                                                    startActivityForResult(intent, REQUEST_MAKE_CHECKIN);
                                                }
                                                c.dismiss();
                                            }

                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<ResponseContainer<SuccessCreatePlace>> call, Throwable t) {
                                    }
                                });
                            }
                        });
                    }
                });
                c.show();
                App.showKeyboard(MainActivity.this);
            }});
        b.show();
    }


    @OnClick(R.id.photoButton)
    public void makePhoto(View v){
        choseMediaTypeContainer.setVisibility(View.GONE);
        video = false;
        checkLocationAndStoragePermission();
    }


    @OnClick(R.id.videoButton)
    public void makeVideo(View v){
        choseMediaTypeContainer.setVisibility(View.GONE);
        video = true;
        checkLocationAndStoragePermission();
    }

    void getLastKnownLocation(){
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            SharedManager.addProperty("latitude", String.valueOf(location.getLatitude()));
                            SharedManager.addProperty("longitude", String.valueOf(location.getLongitude()));

                            makePlaceRequestAndMakeCheckin();
                        } else {
                            Toast.makeText(MainActivity.this, "Find location problem", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkLocationAndStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            getLastKnownLocation();
                            return;
                        }

                        if(report.getDeniedPermissionResponses().size()>1){
                            Toast.makeText(MainActivity.this, "Not granted permission", Toast.LENGTH_LONG).show();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Для того чтобы сделать чекин вам нужно дать разрешение на определение местоположения устройства и предоставить доступ к карте памяти");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton("Настройки", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    goToSettings();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            List<PermissionDeniedResponse> permissions = report.getDeniedPermissionResponses();

                            if(permissions.get(0).getPermissionName().equals(Manifest.permission.ACCESS_FINE_LOCATION)){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Для того чтобы сделать чекин вам нужно дать разрешение на определение местоположения устройства");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                            if(permissions.get(0).getPermissionName().equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Для того чтобы сделать чекин вам нужно предоставить доступ к карте памяти");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.setNegativeButton("Настройки", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        goToSettings();
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.MakeCheckin event){
        if(hideButtonsTimer !=null)
            hideButtonsTimer.cancel();

        hideButtonsTimer = new Timer();
        hideButtonsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        choseMediaTypeContainer.setVisibility(View.GONE);
                    }
                });
            }
        }, 3000);
        if(choseMediaTypeContainer.getVisibility()==View.VISIBLE)
            choseMediaTypeContainer.setVisibility(View.GONE);
        else
            choseMediaTypeContainer.setVisibility(View.VISIBLE);
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, 19);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startSubscribers(EventBusMessages.OpenSubscribers event) {
        SubscribersFragment myFriendsFragment = SubscribersFragment.createInstance(
                getFragmentName(),
                event.getUserId());
        mTabStacker.replaceFragment(myFriendsFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startSubscription(EventBusMessages.OpenSubscriptions event) {
        SubscriptionFragment subscriptionFragment = SubscriptionFragment.createInstance(
                getFragmentName(),
                event.getUserId());
        mTabStacker.replaceFragment(subscriptionFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startPlaceSubscribers(EventBusMessages.OpenPlaceSubscribers event) {
        PlaceSubscribersFragment myFriendsFragment = PlaceSubscribersFragment.createInstance(
                getFragmentName(),
                event.getGroupId());
        mTabStacker.replaceFragment(myFriendsFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startUsersInPlace(EventBusMessages.OpenUsersInPlace event) {
        UsersInPlaceFragment myFriendsFragment = UsersInPlaceFragment.createInstance(
                getFragmentName(),
                event.getGroupId());
        mTabStacker.replaceFragment(myFriendsFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startUserPlaces(EventBusMessages.OpenUserPlace event) {
        UserPlacesFragment subscriptionFragment = UserPlacesFragment.createInstance(
                getFragmentName(),
                event.getUserId());
        mTabStacker.replaceFragment(subscriptionFragment, null);
    }

    String getFragmentName(){
        String s = "TAB_" + mTabStacker.getCurrentTabSize();
        return s;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openUser(EventBusMessages.OpenUser event){

        if(event.isCloseCurrent()){
            mTabStacker.onBackPressed();
        }

        android.support.v4.app.Fragment profileFragment;
        if(event.getMessage().equals(SharedManager.getProperty(Constants.KEY_MY_ID))){
            profileFragment = ProfileFragment.createInstance(
                    getFragmentName());
        } else {
            profileFragment = SomeoneProfileFragment.createInstance(
                    getFragmentName(),
                    event.getMessage());
        }
        mTabStacker.replaceFragment(profileFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.OpenComments event){
        CommentsFragment commentsFragment = CommentsFragment.createInstance(
                getFragmentName(),
                event.getPostId(),
                event.getOwnerId(),
                event.getType());
        mTabStacker.replaceFragment(commentsFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.OpenSubcomments event){
        SubCommentsFragment commentsFragment = SubCommentsFragment.createInstance(
                getFragmentName(),
                event.getPostId(),
                event.getOwnerId(),
                event.getType(),
                event.getCommentId());
        mTabStacker.replaceFragment(commentsFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openPlace(EventBusMessages.OpenPlace event){
        if(event.isCloseCurrent()){
            mTabStacker.onBackPressed();
        }

        PlaceFragment placeFragment = PlaceFragment.createInstance(
                getFragmentName(),
                event.getId(),
                event.getTabPos());
        mTabStacker.replaceFragment(placeFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openPlacePhoto(EventBusMessages.OpenPlacePhoto event){
        FeedPlacesCheckinFragment placeFragment = FeedPlacesCheckinFragment.createInstance(
                getFragmentName(),
                event.getPlaceId(),
                event.getPostId());
        mTabStacker.replaceFragment(placeFragment, null);
    }

    /**
     *      Open Album Content
     */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openPhotoReportContent(EventBusMessages.OpenPhotoReportContent event){
        FeedPhotoReportFragmentContent placeFragment = FeedPhotoReportFragmentContent.createInstance(
                getFragmentName(),
                event.getPhotoId());
        mTabStacker.replaceFragment(placeFragment, null);
    }

    /**
     *      Open Album Content
     */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void asdfasdfdas(EventBusMessages.OpenPhotoReport event){
        PhotoReportFragment photoReportFragment = PhotoReportFragment.createInstance(
                getFragmentName(),
                event.getAlbum().getId(),
                event.getAlbum().getTitle(),
                event.getAlbum().getDateCreated());
        mTabStacker.replaceFragment(photoReportFragment, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openFeed(EventBusMessages.OpenFeed event){
        mTabStacker.replaceFragment(
                FeedFragment.createInstance(getFragmentName()), null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OpenChronics(EventBusMessages.OpenChronics event){
        mTabStacker.replaceFragment(
                ChronicsFragment.createInstance(getFragmentName()), null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openPlaces(EventBusMessages.OpenPlaces event){
        mTabStacker.replaceFragment(
                PlacesFragment.createInstance(getFragmentName()), null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openPeople(EventBusMessages.OpenPeople event){
        mTabStacker.replaceFragment(
                PeoplesFragment.createInstance(getFragmentName(), mTabStacker.getCurrentTabSize(), 2), null);
    }

    /**
     *      Open Profile Content
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openProfileContent(EventBusMessages.OpenCheckinProfileContent event){
        mTabStacker.replaceFragment(ProfileCheckinContent.createInstance(getFragmentName(), event.getPostId(), event.getStorageKey()), null);
    }

    /**
     *      Open Event Content
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openEventContent(EventBusMessages.OpenEventContent event){
        mTabStacker.replaceFragment(
                EventContentFragment.createInstance(getFragmentName(), event.getEventId(), event.getOwnerId(), event.isBackToPlace()), null);
    }

    /**
     *      Open Action Content
     */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openActionContent(EventBusMessages.OpenActionContent event){
        mTabStacker.replaceFragment(
                ActionContentFragment.createInstance(getFragmentName(), event.getEventId(), event.getOwnerId(), event.isBackToPlace()), null);
    }

    /**
     *      Open Service Content
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openServiceContent(EventBusMessages.OpenServiceContent event){
        mTabStacker.replaceFragment(ServiceContentFragment.createInstance(getFragmentName(), event.getEventId(), event.getOwnerId(), event.isBackToPlace()), null);
    }

    /**
     *      Open Dialogs
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openDialogs(EventBusMessages.OpenDialogs event){
        if(!(mTabStacker.getCurrentTopFragment() instanceof DialogsFragment)){
            mTabStacker.replaceFragment(
                    DialogsFragment.createInstance(getFragmentName()), null);
        }
    }

    /**
     *      Open Chat
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openChat(EventBusMessages.OpenChat event){
        mTabStacker.replaceFragment(
                ChatFragment.createInstance(getFragmentName(), event.getPeerId()), null);
    }

    /**
     *      Open Notification
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openNotifications(EventBusMessages.OpenNotifications event) {
        if(!(mTabStacker.getCurrentTopFragment() instanceof NotificationsFragment)){
            mTabStacker.replaceFragment(NotificationsFragment.createInstance(
                    getFragmentName()), null);
        }
    }
    /**
     *      Open Menu
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openMenuFragment(EventBusMessages.OpenMenu event){
        if(!(mTabStacker.getCurrentTopFragment() instanceof MenuFragment)){
            mTabStacker.clearTabStack();
            mTabStacker.replaceFragment(
                    new MenuFragment(), null);
        }
        mTabStacker.popToTop(true);
    }

    /**
     *      Open Settings
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startSettings(EventBusMessages.MainSettings event){
        mTabStacker.replaceFragment(
                new MainSettingsFragment(), null);
    }

    /**
     *      Open Notifications Settings
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startNotificationSettings(EventBusMessages.OpenNotificationSettings event){
        mTabStacker.replaceFragment(
                new NotificationSettingsFragment(), null);
    }
    /**
     *      Open Profile Settings
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startProfileSettings(EventBusMessages.OpenProfileSettings event){
        mTabStacker.replaceFragment(
                new ProfileSettingsFragment(), null);
        //currentFragment = CurrentFragment.MENU_FRAGMENT;
    }
    /**
     *      Open Black List
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startBlackList(EventBusMessages.OpenBlackList event){
        mTabStacker.replaceFragment(
                new BlackListFragment(), null);
        //currentFragment = CurrentFragment.MENU_FRAGMENT;
    }

    /**
     *      Open Events
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startEvents(EventBusMessages.OpenEvents event){
        mTabStacker.replaceFragment(
                EventsFragment.createInstance(getFragmentName()), null);
    }

    /**
     *      Open Profile Events
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startProfileEvents(EventBusMessages.OpenProfileEvents event){
        mTabStacker.replaceFragment(
                ProfileEvents.createInstance(getFragmentName(), event.getUserId()), null);
    }

    /**
     *      Open Servers
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startServers(EventBusMessages.OpenServices event){
        mTabStacker.replaceFragment(
                ServicesFragment.createInstance(getFragmentName()), null);
    }

    /**
     *      Open checkinContentOne
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void checkinContentOne(EventBusMessages.ProfileCheckinContentOne event){
        mTabStacker.replaceFragment(
                ProfileCheckinContentOne.createInstance(getFragmentName(), event.getPostId(), event.isBackToProfile()), null);
    }

    /**
     *      lose connection
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loseConnection(EventBusMessages.LoseConnection event){
        mTabStacker.replaceFragment(
                LoseConnectionFragment.createInstance(getFragmentName()), null);
    }

    /**
     *      Map
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void map(EventBusMessages.OpenMap event){
        mTabStacker.replaceFragment(
                new MapFragment(), null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void share(EventBusMessages.Share event){
        ShareFragment bottomSheetFragment = ShareFragment.createInstance(event.getAttachments());
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showImage(EventBusMessages.ShowImageFragment event){
        mTabStacker.replaceFragment(
                ShowImageFragment.createInstance(getFragmentName(), event.getImageUrl()), null);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void claim(EventBusMessages.Claim event){
        ClaimsBottomSheetFragment bottomSheetFragment = ClaimsBottomSheetFragment.createInstance(event.getAttachments());
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void tryConnection(EventBusMessages.TryConnection event){
        mTabStacker.onBackPressed();
        recreateFragment();
    }

    public boolean checkPeerId(String peerId){
        if(mTabStacker.getCurrentTopFragment() instanceof ChatFragment){
            return peerId.equals(((ChatFragment)mTabStacker.getCurrentTopFragment()).getPeerId());
        }
        return false;
    }

    void recreateFragment(){
        getSupportFragmentManager()
                .beginTransaction()
                .detach(mTabStacker.getCurrentTopFragment())
                .attach(mTabStacker.getCurrentTopFragment())
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Object getDate(String key) {
        if(date.containsKey(key)){
            return date.get(key);
        }
        return null;
    }

    @Override
    public void setDate(String key, Object date) {
        remove(key);
        this.date.put(key, date);
    }

    @Override
    public void remove(String key) {
        date.remove(key);
    }


    @Override
    public void onBackPressed() {
        android.support.v4.app.Fragment fragment = mTabStacker.getCurrentTopFragment();
        if (fragment != null && fragment instanceof EventContentFragment) {
            if (fragment.getArguments().getBoolean("backToPlace")) {
                ((EventContentFragment) fragment).back(null);
            } else {
                if (!mTabStacker.onBackPressed()) {
                    super.onBackPressed();
                }
            }
            return;
        }

        if (fragment != null && fragment instanceof ActionContentFragment) {
            if (fragment.getArguments().getBoolean("backToPlace")) {
                ((ActionContentFragment) fragment).back(null);
            } else {
                if (!mTabStacker.onBackPressed()) {
                    super.onBackPressed();
                }
            }
            return;
        }

        if (fragment != null && fragment instanceof ProfileCheckinContentOne) {
            if (fragment.getArguments().getBoolean("backToProfile")) {
                ((ProfileCheckinContentOne) fragment).back(null);
            } else {
                Log.i("TAG26", "хз 2");
                if (!mTabStacker.onBackPressed()) {
                    super.onBackPressed();
                }
            }
            return;
        }

        if (!mTabStacker.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
