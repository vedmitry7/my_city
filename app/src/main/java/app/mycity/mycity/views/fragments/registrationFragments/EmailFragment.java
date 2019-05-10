package app.mycity.mycity.views.fragments.registrationFragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import app.mycity.mycity.views.activities.RegisterActivityImpl;
import app.mycity.mycity.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmailFragment extends Fragment {

    @BindView(R.id.emailFragEmailEt)
    EditText email;
    @BindView(R.id.emailFragProgressBarContainer)
    ConstraintLayout progressBarContainer;
    @BindView(R.id.emailFragmentTextViewInfo)
    TextView info;

    RegisterActivityImpl dataStore;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBarContainer.setVisibility(View.INVISIBLE);

        email.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    getActivity().onBackPressed();
                }
                return true;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataStore = (RegisterActivityImpl) context;
    }

    @OnClick(R.id.passwordFragNext)
    public void confirmEmail(View view){

        if(isValidEmailAddress(email.getText().toString())){
            dataStore.setEmail(email.getText().toString());
            progressBarContainer.setVisibility(View.VISIBLE);
            dataStore.setEmail(email.getText().toString());
            dataStore.checkEmail();
        } else {
            info.setTextColor(Color.parseColor("#ff0000"));
            info.setText("Email введен не верно");
        }
    }

    public void emailExist(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarContainer.setVisibility(View.GONE);
                info.setTextColor(Color.parseColor("#ff0000"));
                info.setText(msg);
            }
        });

    }

    public boolean isWaitAnswer(){
        return progressBarContainer.getVisibility() == View.VISIBLE;
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
