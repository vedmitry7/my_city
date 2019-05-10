package app.mycity.mycity.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import app.mycity.mycity.App;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.Success;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClaimsBottomSheetFragment extends BottomSheetDialogFragment {

    String attachment;

    @BindView(R.id.claimsRadioGroup)
    RadioGroup claimsRadioGroup;
    @BindView(R.id.claimEditText)
    EditText editText;
    @BindView(R.id.progressLayout)
    ConstraintLayout progressLayout;
    @BindView(R.id.doneLayout)
    ConstraintLayout doneLayout;
    String reason = "Оскорбление";

    public static ClaimsBottomSheetFragment createInstance(String attachments) {
        ClaimsBottomSheetFragment fragment = new ClaimsBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString("attachments", attachments);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_bottom_sheet_claims, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        attachment = getArguments().getString("attachments");

        claimsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.insult:
                        reason = "Оскорбление";
                        break;
                    case R.id.spam:
                        reason = "Спам";
                        break;
                    case R.id.inappropriateMaterial:
                        reason = "Неуместный материал";
                        break;
                    case R.id.fraud:
                        reason = "Мошенничество";
                        break;
                    case R.id.prohibitedContent:
                        reason = "Запрещенный контент";
                        break;
                }
            }
        });
    }

    @OnClick(R.id.sendClaim)
    public void sendClaim(){
        progressLayout.setVisibility(View.VISIBLE);
        sendClaim(reason + " - " + editText.getText().toString());
    }

    @OnClick(R.id.done)
    public void done(){
        this.dismiss();
    }

    void sendClaim(String description){

        ApiFactory.getApi().claim(App.accessToken(), attachment, description).enqueue(new Callback<ResponseContainer<Success>>() {
            @Override
            public void onResponse(Call<ResponseContainer<Success>> call, Response<ResponseContainer<Success>> response) {
                doneLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseContainer<Success>> call, Throwable t) {
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}