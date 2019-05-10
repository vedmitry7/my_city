package app.mycity.mycity.views.fragments.registrationFragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.mycity.mycity.App;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.City;
import app.mycity.mycity.api.model.ResponseCities;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.views.activities.RegisterActivityImpl;
import app.mycity.mycity.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataFragment extends Fragment {

    RegisterActivityImpl dataStore;

    @BindView(R.id.dataFragBirthdayTv)
    TextView birthday;

    @BindView(R.id.dataFragFirstNameEt)
    EditText firstName;

    @BindView(R.id.dataFragSecondNameEt)
    EditText secondName;

    @BindView(R.id.dataFragSexRadioGroup)
    RadioGroup radioGroup;

    @BindView(R.id.cityEditText)
    AutoCompleteTextView cityEditText;

    private String sex = "2";
    Context context;
    private String cityId = "";
    private String birthdayData;

    List<City> cities;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.male:
                        sex = "2";
                        break;
                    case R.id.female:
                        sex = "1";
                        break;
                }
            }
        });

        context = getContext();

        firstName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    firstName.setText(firstName.getText().toString().trim());
                    secondName.requestFocus();
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    getActivity().onBackPressed();
                }
                return true;
            }
        });

        firstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    firstName.setText(firstName.getText().toString().trim());
                }
            }
        });

        secondName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    secondName.setText(secondName.getText().toString().trim());
                    setDate();
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    getActivity().onBackPressed();
                }
                return true;
            }
        });

        secondName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    secondName.setText(secondName.getText().toString().trim());
                }
            }
        });

        cityEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    for (City c:cities
                         ) {
                        if(c.getTitle().startsWith(cityEditText.getText().toString().trim())){
                            cityId = c.getId();
                            cityEditText.setText(c.getTitle());
                            saveCityToSharedPrefs(c.getTitle(), c.getId());
                            break;
                        }
                    }

                    App.closeKeyboard(context);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    getActivity().onBackPressed();
                }
                return true;
            }
        });

        cityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cityId = "";
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                loadCities(s.toString());
            }
        });
        firstName.requestFocus();
        App.showKeyboard(getContext());
    }

    private void loadCities(String search) {
        ApiFactory.getApi().getCitiesWithSearch(search).enqueue(new Callback<ResponseContainer<ResponseCities>>() {
            @Override
            public void onResponse(Call<ResponseContainer<ResponseCities>> call, final Response<ResponseContainer<ResponseCities>> response) {
                final ArrayList<String> data = new ArrayList<>();

                cities = response.body().getResponse().getItems();

                for (int i = 0; i < response.body().getResponse().getItems().size(); i++) {
                    data.add(cities.get(i).getTitle());
                }
                cityEditText.setAdapter(new ArrayAdapter<String>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, data));
                cityEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        cityId = response.body().getResponse().getItems().get(position).getId();
                        saveCityToSharedPrefs(response.body().getResponse().getItems().get(position).getTitle(), cityId);
                        App.closeKeyboard(getContext());
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseContainer<ResponseCities>> call, Throwable t) {
            }
        });
    }

    void saveCityToSharedPrefs(String name, String id){

        String chosenCity = name;
        if(name.contains(",")){
            chosenCity = name.substring(0, name.indexOf(","));
        }
        SharedManager.addProperty("chosen_city", chosenCity);
        SharedManager.addIntProperty("chosen_city_id", Integer.parseInt(id));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataStore = (RegisterActivityImpl) context;
    }

    @OnClick(R.id.dataFragmentNext)
    public void next(View view){

        if(isValidLogin(firstName.getText().toString()) && isValidLogin(secondName.getText().toString())){
        } else {
            showAlert("Имя и фамилия не должны быть менее двух символов");
            return;
        }

        if(birthday.getText().toString().equals("Дата рождения")){
            showAlert("Заполните поле даты рождения");
            return;
        }

        if (cityId.length()==0) {

            boolean finded = false;
            for (City c:cities
                    ) {
                if(c.getTitle().startsWith(cityEditText.getText().toString().trim())){
                    cityId = c.getId();
                    cityEditText.setText(c.getTitle());
                    saveCityToSharedPrefs(c.getTitle(), c.getId());
                    finded = true;
                    break;
                }
            }
            if(!finded){
                showAlert("Заполните поле город");
                cityEditText.requestFocus();
                App.showKeyboard(context);
                return;
            }

        }
        dataStore.setInfo(firstName.getText().toString(), secondName.getText().toString(), birthdayData, sex, cityId);
    }

    private void showAlert(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(s);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.dataFragBirthdayIb)
    public void setDate(View view){
        setDate();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.dataFragBirthdayTv)
    public void setDate2(View view){
        setDate();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setDate() {

        App.closeKeyboard(getActivity().getApplicationContext());
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this.getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                birthday.setTextColor(Color.parseColor("#000000"));
                Calendar calendar = Calendar.getInstance();
                calendar.set(i,  i1, i2);
                DateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
                DateFormat dateFormatForServer = new SimpleDateFormat("dd.MM.yyyy");
                Date date = calendar.getTime();
                String dateText = dateFormat.format(date);
                birthdayData = dateFormatForServer.format(calendar.getTime());
                birthday.setText(dateText);
                App.showKeyboard(getContext());

            }
        },
                mYear,
                mMonth,
                mDay)
                .show();
        cityEditText.requestFocus();
    }

    public boolean isValidLogin(String email) {
        String ePattern = "^[а-яА-Я]{2,}|[a-zA-Z]{2,}$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
