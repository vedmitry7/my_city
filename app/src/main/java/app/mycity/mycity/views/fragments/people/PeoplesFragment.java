package app.mycity.mycity.views.fragments.people;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import app.mycity.mycity.App;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.PlaceCategory;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.UsersContainer;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.adapters.PeoplesRecyclerAdapter;
import app.mycity.mycity.views.adapters.PeoplesTopBarAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import fr.arnaudguyon.tabstacker.TabStacker.TabStackInterface;

public class PeoplesFragment extends Fragment implements TabStackInterface {


    @BindView(R.id.myAllFriendsRecyclerAdapter)
    RecyclerView recyclerView;

    @BindView(R.id.progressBarPlaceHolder)
    ConstraintLayout progressBarPlaceHolder;

    @BindView(R.id.listEmptyContainer)
    ConstraintLayout listEmptyContainer;

    @BindView(R.id.horizontalRecyclerView)
    RecyclerView categoriesRecyclerView;

    TextView search;
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.searchButton)
    ImageView searchButton;


    @BindView(R.id.toolBarTitle)
    TextView toolBarTitle;

    PeoplesRecyclerAdapter adapter;
    List<Profile> userList;

    PeoplesTopBarAdapter placesCategoriesAdapter;

    List<PlaceCategory> placeCategories;

    String filter = "";
    private boolean isLoading;
    private int totalCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.people_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.searchButton)
    public void search(View v){
        toolBarTitle.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconified(false);
    }


    public static PeoplesFragment createInstance(String name, int stackPos, int tabPos) {
        PeoplesFragment fragment = new PeoplesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putInt("tabPos", tabPos);
        bundle.putInt("stackPos", stackPos);
        fragment.setArguments(bundle);
        return fragment;
    }

    @OnClick(R.id.backButton)
    public void sadsa(View v){
        getActivity().onBackPressed();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolBarTitle.setText("Люди");

        Util.setNawBarClickListener(view);
        Util.setNawBarIconColor(getContext(), view, -1);

        search = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        search.setTextColor(getResources().getColor(R.color.white));
        search.setHintTextColor(getResources().getColor(R.color.white));

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor_drawable_white); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }

        final LinearLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);

        userList = new ArrayList<>();
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PeoplesRecyclerAdapter(userList);
        recyclerView.setAdapter(adapter);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        isLoading = true;
                        if(totalCount > userList.size()){
                            getUsersList(filter, search.getText().toString(), userList.size());
                        }
                    }
                }
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        progressBarPlaceHolder.setVisibility(View.GONE);

        getUsersList("", "", userList.size());
        placeCategories = new ArrayList<>();
        PlaceCategory placeCategory = new PlaceCategory();
        placeCategory.setId(0);
        placeCategory.setTitle("Все");
        placeCategories.add(placeCategory);
        PlaceCategory inPalce = new PlaceCategory();
        inPalce.setId(1);
        inPalce.setTitle("В заведениях");
        placeCategories.add(inPalce);
        PlaceCategory inPlaceReady = new PlaceCategory();
        inPlaceReady.setId(2);
        inPlaceReady.setTitle("Готовы к знакомству");
        placeCategories.add(inPlaceReady);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(horizontalLayoutManager);
        categoriesRecyclerView.setHasFixedSize(true);
        placesCategoriesAdapter = new PeoplesTopBarAdapter(placeCategories);
        categoriesRecyclerView.setAdapter(placesCategoriesAdapter);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    userList = new ArrayList<>();
                    adapter.notifyDataSetChanged();
                    App.closeKeyboard(getContext());
                    getUsersList(filter, search.getText().toString(), userList.size());
                    search.clearFocus();
                    return true;
                }
                return false;
            }
        });




        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                userList = new ArrayList<>();
                getUsersList(filter, "", userList.size());
                searchView.setVisibility(View.GONE);
                toolBarTitle.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void gfdsgsd(EventBusMessages.SortPeople event){
        userList = new ArrayList<>();
        switch (event.getPosition()){
            case 0:
                filter = "";
                break;
            case 1:
                filter = "in_place";
                break;
            case 2:
                filter = "ready_meet";
                break;
        }

        getUsersList(filter, search.getText().toString(), userList.size());
        search.clearFocus();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    private void getUsersList(String filter, String search, int offset){
        progressBarPlaceHolder.setVisibility(View.VISIBLE);
        int sex = 0;
        if(SharedManager.getBooleanProperty("sortBySex")){
            sex = SharedManager.getIntProperty("sex");
        }
        int ageFrom = 0;
        int ageTo = 100;
        if(SharedManager.getBooleanProperty("sortByAge")){
            ageFrom = SharedManager.getIntProperty("ageFrom");
            ageTo = SharedManager.getIntProperty("ageTo");
        }

        ApiFactory.getApi().getTopUsersInPlacesWithSorting(App.accessToken(), App.chosenCity(), offset, "photo_360,place,count_likes", "top", filter, search, sex, ageFrom, ageTo, "1").enqueue(new retrofit2.Callback<ResponseContainer<UsersContainer>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<UsersContainer>> call, retrofit2.Response<ResponseContainer<UsersContainer>> response) {
                UsersContainer users = response.body().getResponse();

                if(users != null){
                    userList.addAll(users.getProfiles());
                    isLoading = false;
                    progressBarPlaceHolder.setVisibility(View.GONE);
                    totalCount = response.body().getResponse().getCount();
                    adapter.update(userList);
                    if(response.body().getResponse().getCount()==0){
                        listEmptyContainer.setVisibility(View.VISIBLE);
                    } else {
                        listEmptyContainer.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<UsersContainer>> call, Throwable t) {
                EventBus.getDefault().post(new EventBusMessages.LoseConnection());
            }
        });
    }

    @OnClick(R.id.sortButton)
    public void sort2(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Фильтры");

        View view = getActivity().getLayoutInflater().inflate(R.layout.people_sort_dialog2, null);
        builder.setView(view);

        final RadioGroup radioGroup = view.findViewById(R.id.radioGroupSex);
        radioGroup.check(R.id.ratingRadioButton);
        RadioButton male = (RadioButton) view.findViewById(R.id.maleRadioButton);
        RadioButton female = (RadioButton) view.findViewById(R.id.femaleRadioButton);

        int sex = SharedManager.getIntProperty("sex");
        if(sex==0){
            SharedManager.addIntProperty("sex", 2);
        }
        if(sex==1){
            female.setChecked(true);
        } else {
            male.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.maleRadioButton:
                        SharedManager.addIntProperty("sex", 2);
                        Log.d("TAG23", "m ");
                        break;
                    case R.id.femaleRadioButton:
                        Log.d("TAG23", "f ");
                        SharedManager.addIntProperty("sex", 1);
                        break;
                }
            }
        });

        boolean sortBySex = SharedManager.getBooleanProperty("sortBySex");

        final CheckBox checkboxSex = view.findViewById(R.id.checkBoxSexActive);
        checkboxSex.setChecked(sortBySex);

        for(int i = 0; i < radioGroup.getChildCount(); i++){
            ((RadioButton)radioGroup.getChildAt(i)).setEnabled(sortBySex);
        }

        checkboxSex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedManager.addBooleanProperty("sortBySex", isChecked);
                for(int i = 0; i < radioGroup.getChildCount(); i++){
                    ((RadioButton)radioGroup.getChildAt(i)).setEnabled(isChecked);
                }
            }
        });

        boolean sortByAge = SharedManager.getBooleanProperty("sortByAge");

        final CheckBox checkboxAge = view.findViewById(R.id.checkBoxAgeActive);
        checkboxAge.setChecked(sortByAge);

        final EditText editTextFrom = view.findViewById(R.id.editTextFrom);
        final EditText editTextTo = view.findViewById(R.id.editTextTo);

        editTextFrom.setText(String.valueOf(SharedManager.getIntProperty("ageFrom")));
        editTextTo.setText(String.valueOf(SharedManager.getIntProperty("ageTo")));

        editTextFrom.setEnabled(sortByAge);
        editTextTo.setEnabled(sortByAge);

        checkboxAge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedManager.addBooleanProperty("sortByAge", isChecked);

                editTextFrom.setEnabled(isChecked);
                editTextTo.setEnabled(isChecked);
            }
        });

        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {


                int ageTo = Integer.valueOf(editTextTo.getText().toString());
                int ageFrom = Integer.valueOf(editTextFrom.getText().toString());

                if(SharedManager.getBooleanProperty("sortByAge")){
                    SharedManager.addIntProperty("ageTo", ageTo);
                    SharedManager.addIntProperty("ageFrom", ageFrom);
                }
                getUsersList(filter,"", userList.size());
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });


        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onStart() {
        super.onStart();
    }


    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason presentReason) {

    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason dismissReason) {

    }

    @Override
    public View onSaveTabFragmentInstance(Bundle bundle) {
        return null;
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle bundle) {

    }
}
