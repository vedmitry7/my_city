package app.mycity.mycity.views.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class ForgotPasswordActivity extends AppCompatActivity implements RegisterActivityImpl {

    @BindView(R.id.registrationLabel)
    TextView label;

    private String email, code, password, confirm;
    private android.support.v4.app.FragmentManager fragmentManager;
    private EmailFragment emailFragment;
    private ConfirmEmailFragment confirmEmailFragment;
    private PasswordFragment passwordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        Typeface type = Typeface.createFromAsset(getAssets(),"abril_fatface_regular.otf");
        label.setTypeface(type);

        fragmentManager = getSupportFragmentManager();
        showEmailFragment();

    }

    private void showEmailFragment() {
        emailFragment = new EmailFragment();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragmentContainer, emailFragment);
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
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void nextConfirmEmailCodeStep() {
        confirmEmailFragment = new ConfirmEmailFragment();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
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
                        sendEmail();
                        nextConfirmEmailCodeStep();
                    } else {
                        emailFragment.emailExist("Пользователь с таким email не найден, проверьте правильность ввода email");

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
        Request request = new Request.Builder().url(Constants.URL_BASE + "auth.restore")
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
                .add("code", code)
                .add("email", email)
                .build();
        Request request = new Request.Builder().url(Constants.URL_BASE + "auth.checkCode")
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
                        String success = innerResponseObject.getString("exists");
                        if(success.equals("true")){
                            passwordFragment = new PasswordFragment();
                            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
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
                .build();

        Request request = new Request.Builder().url(Constants.URL_BASE + "auth.reset")
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
                            if(success!=null && !success.equals("")){
                                Log.i("TAG", "All right");
                                ForgotPasswordActivity.this.setResult(RESULT_OK);
                                ForgotPasswordActivity.this.finish();
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
        Fragment currentFrag = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if(currentFrag instanceof DataFragment){
            this.finish();
        }
        if(currentFrag instanceof EmailFragment){
            if(emailFragment.isWaitAnswer()){
                showDialog("Отменить подтверждение email?");
            } else {
                super.onBackPressed();
            }
        }
        if(currentFrag instanceof ConfirmEmailFragment){
            showDialog("Отменить подтверждение email?");
        }
    }

    private void showDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ForgotPasswordActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}