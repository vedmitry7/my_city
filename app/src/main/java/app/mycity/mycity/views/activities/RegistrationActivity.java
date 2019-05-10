package app.mycity.mycity.views.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.OkHttpClientFactory;
import app.mycity.mycity.views.fragments.registrationFragments.ConfirmEmailFragment;
import app.mycity.mycity.views.fragments.registrationFragments.DataFragment;
import app.mycity.mycity.views.fragments.registrationFragments.EmailFragment;
import app.mycity.mycity.views.fragments.registrationFragments.PasswordFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RegistrationActivity extends AppCompatActivity implements RegisterActivityImpl {

    @BindView(R.id.registrationLabel)
    TextView label;

    private String firstName;
    private String secondName;
    private String birthday;
    private String email;
    private String sex, code;
    private String password, confirm;

    private String cityId;

    private android.support.v4.app.FragmentManager fragmentManager;
    private EmailFragment emailFragment;
    private ConfirmEmailFragment confirmEmailFragment;
    private PasswordFragment passwordFragment;
    private DataFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        fragmentManager = getSupportFragmentManager();

        Typeface type = Typeface.createFromAsset(getAssets(),"abril_fatface_regular.otf");
        label.setTypeface(type);

        showDataFragment();
    }

    public void showDataFragment() {
        fragment = new DataFragment();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    @Override
    public void setPassword(String password, String confirm) {
        this.password = password;
        this.confirm = confirm;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void setInfo(String firstName, String secondName, String birthday, String sex, String cityId) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.birthday = birthday;
        this.sex = sex;
        this.cityId = cityId;
        nextEmailStep();
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public void nextEmailStep() {
        emailFragment = new EmailFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
        transaction.replace(R.id.fragmentContainer, emailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void nextConfirmEmailCodeStep() {
        confirmEmailFragment = new ConfirmEmailFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
        transaction.replace(R.id.fragmentContainer, confirmEmailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void checkEmail() {
        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .build();

        Request request = new Request.Builder().url(Constants.URL_BASE + "auth.emailExists")
                .post(body)
                .build();

        OkHttpClientFactory.getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String responseString = response.body().string();

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject innerObject = null;
                try {
                    innerObject = jsonObject.getJSONObject("response");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                boolean code;

                try {
                    code = innerObject.getBoolean("exists");
                    if (code){
                        emailFragment.emailExist("Данный email уже занят, выберите другой или восстановите доступ");
                    } else {
                        sendEmail();
                        nextConfirmEmailCodeStep();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void sendEmail() {
        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .build();
        Request request = new Request.Builder().url(Constants.URL_BASE + "auth.reconfirm")
                .post(body)
                .build();
        OkHttpClientFactory.getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
            }
        });
    }

    @Override
    public void checkEmailCodeAndRegistration() {

        RequestBody body = new FormBody.Builder()
                .add("first_name", firstName)
                .add("last_name", secondName)
                .add("code", code)
                .add("bdate", birthday)
                .add("email", email)
                .add("sex", sex)
                .add("city_id", cityId)
                .build();

        Request request = new Request.Builder().url(Constants.URL_BASE + "auth.signUp")
                .post(body)
                .build();

        OkHttpClientFactory.getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String responseString = response.body().string();
                JSONObject jsonObject = null;
                JSONObject innerResponseObject = null;
                try {
                    jsonObject = new JSONObject(responseString);
                    innerResponseObject = jsonObject.getJSONObject("response");
                    if(innerResponseObject!=null){
                        String success = innerResponseObject.getString("success");
                        if(success.equals("1")){
                            passwordFragment = new PasswordFragment();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
                            transaction.replace(R.id.fragmentContainer, passwordFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();

                        } else {
                            confirmEmailFragment.codeIsWrong();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject innerErrorObject;
                try {
                    innerErrorObject = jsonObject.getJSONObject("error");
                    if(innerErrorObject!=null){
                        String errorMassage = innerErrorObject.getString("error_msg");
                        if(errorMassage.equals("account_error")){
                            confirmEmailFragment.codeIsWrong();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void commitPassword() {

        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("code", code)
                .add("password", password)
                .add("confirm_password", confirm)
                .add("intro", "0")
                .build();

        Request request = new Request.Builder().url(Constants.URL_BASE + "auth.confirm")
                .post(body)
                .build();

        OkHttpClientFactory.getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String responseString = response.body().string();
                JSONObject jsonObject = null;
                JSONObject innerResponseObject = null;
                try {
                    jsonObject = new JSONObject(responseString);
                    innerResponseObject = jsonObject.getJSONObject("response");
                    String success;
                    if(innerResponseObject != null){
                        try {
                            success = innerResponseObject.getString("success");
                            if(success.equals("1")){
                                RegistrationActivity.this.finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject innerErrorObject;
                try {
                    innerErrorObject = jsonObject.getJSONObject("error");
                    if(innerErrorObject!=null){
                        String errorMassage = innerErrorObject.getString("error_msg");
                        if(errorMassage.equals("account_password_length")){
                            passwordFragment.wrongCodeLength();
                        }
                        if(errorMassage.equals("account_password_not_match")){
                            passwordFragment.codeNotMatch();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}