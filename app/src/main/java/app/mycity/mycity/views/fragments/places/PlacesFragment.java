package app.mycity.mycity.views.fragments.places;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.github.chuross.library.ExpandableLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


import app.mycity.mycity.App;
import app.mycity.mycity.util.Constants;
import app.mycity.mycity.R;
import app.mycity.mycity.api.ApiFactory;
import app.mycity.mycity.api.model.Place;
import app.mycity.mycity.api.model.PlaceCategoryResponce;
import app.mycity.mycity.api.model.PlaceCategory;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponsePlaces;
import app.mycity.mycity.util.EventBusMessages;
import app.mycity.mycity.util.SharedManager;
import app.mycity.mycity.util.Util;
import app.mycity.mycity.views.activities.Storage;
import app.mycity.mycity.views.adapters.PlacesRecyclerAdapter;
import app.mycity.mycity.views.adapters.PlacesTopBarAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.arnaudguyon.tabstacker.TabStacker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlacesFragment extends Fragment implements TabStacker.TabStackInterface {


    @BindView(R.id.placesFragmentRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.horizontalRecyclerView)
    RecyclerView categoriesRecyclerView;

    @BindView(R.id.placesProgressBar)
    ConstraintLayout placesProgressBar;

    @BindView(R.id.toolBarTitle)
    TextView title;

    @BindView(R.id.placesFragmentMessage)
    TextView placesFragmentMessage;

    EditText search;

    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.searchButton)
    ImageView searchButton;

    PlacesRecyclerAdapter adapter;

    PlacesTopBarAdapter placesCategoriesAdapter;

    List<Place> placeList;
    List<PlaceCategory> placeCategories;

    boolean isLoading;

    int totalCount;

    Storage storage;

    String order = Constants.FILTER_RATE;
    private View fragmentView;

    public static PlacesFragment createInstance(String name) {
        PlacesFragment fragment = new PlacesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @OnClick(R.id.searchButton)
    public void search(View v){
        title.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconified(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.places_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.backButton)
    public void sadsa(View v){
        getActivity().onBackPressed();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentView = view;
        if(SharedManager.getProperty("placesOrder")!=null && !SharedManager.getProperty("placesOrder").equals("")){
        }

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor_drawable_white); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }

        search = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        search.setTextColor(getResources().getColor(R.color.white));
        search.setHintTextColor(getResources().getColor(R.color.white));

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    placeList = new ArrayList<>();
                    adapter.notifyDataSetChanged();
                    placesProgressBar.setVisibility(View.VISIBLE);
                    App.closeKeyboard(getContext());
                    loadPlaces(0, 0, search.getText().toString(), order);
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    search.clearFocus();
                    return true;
                }
                return false;
            }
        });

        Util.setNawBarClickListener(view);
        Util.setNawBarIconColor(getContext(), view, -1);
        Util.setUnreadCount(view);

        title.setText("Места");

        placeList = new ArrayList<>();
        adapter = new PlacesRecyclerAdapter(placeList);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (!isLoading) {
                    if ( lastVisibleItems >= totalItemCount -10 ) {
                        isLoading = true;
                        if(totalCount >= placeList.size()){
                            loadPlaces(placeList.size(), placesCategoriesAdapter.getCategoryId(), "", order);
                        }
                    }
                }
            }
        };

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        loadPlaces(placeList.size(), 0, "", order);

        placeCategories = new ArrayList<>();
        PlaceCategory placeCategory = new PlaceCategory();
        placeCategory.setId(0);
        placeCategory.setTitle("Все");
        placeCategories.add(placeCategory);

        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(horizontalLayoutManager);
        categoriesRecyclerView.setHasFixedSize(true);
        placesCategoriesAdapter = new PlacesTopBarAdapter(placeCategories);
        categoriesRecyclerView.setAdapter(placesCategoriesAdapter);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                loadPlaces(0, placesCategoriesAdapter.getCategoryId(), "", order);
                searchView.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
                return true;
            }
        });

        loadCategories();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessages.UnreadCountUpdate event){
        Util.setUnreadCount(fragmentView);
    }


    @OnClick(R.id.sortButton)
    public void sort(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Сортировка");

        View view = getActivity().getLayoutInflater().inflate(R.layout.places_sort_dialog, null);
        builder.setView(view);


        final ExpandableLayout expandableLayout
                = view.findViewById(R.id.expandable_layout);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        final RadioGroup radioGroupRate = view.findViewById(R.id.radioGroupRate);

        if(order.equals("rate")) {
            radioGroup.check(R.id.ratingRadioButton);
            radioGroupRate.check(R.id.ratingRadioButtonAll);
        }
        if(order.equals("new")) {
            radioGroup.check(R.id.onlineCountRadioButton);
            expandableLayout.collapse(false);
        }
        if(order.equals("pop")) {
            radioGroup.check(R.id.checkinCountRadioButton);
            expandableLayout.collapse(false);
        }


        if(order.equals(Constants.FILTER_RATE_INTERIOR)) {
            radioGroupRate.check(R.id.ratingRadioButtonInterior);
            radioGroup.check(R.id.ratingRadioButton);
        }
        if(order.equals(Constants.FILTER_RATE_SERVICE)) {
            radioGroupRate.check(R.id.ratingRadioButtonService);
            radioGroup.check(R.id.ratingRadioButton);
        }
        if(order.equals(Constants.FILTER_RATE_QUALITY)) {
            radioGroupRate.check(R.id.ratingRadioButtonServiceQuality);
            radioGroup.check(R.id.ratingRadioButton);
        }
        if(order.equals(Constants.FILTER_RATE_PRICE)) {
            radioGroupRate.check(R.id.ratingRadioButtonPrice);
            radioGroup.check(R.id.ratingRadioButton);
        }


              radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.ratingRadioButton:
                        order = Constants.FILTER_RATE;
                        radioGroupRate.check(R.id.ratingRadioButtonAll);
                        expandableLayout.expand(false);
                        break;
                    case R.id.onlineCountRadioButton:
                        expandableLayout.collapse(false);
                        order = Constants.FILTER_NEW;
                        break;
                    case R.id.checkinCountRadioButton:
                        order = Constants.FILTER_POP;
                        expandableLayout.collapse(false);
                        break;
                }
            }
        });
        radioGroupRate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.ratingRadioButtonAll:
                        order = Constants.FILTER_RATE;
                        break;
                    case R.id.ratingRadioButtonService:
                        order = Constants.FILTER_RATE_SERVICE;
                        break;
                    case R.id.ratingRadioButtonPrice:
                        order = Constants.FILTER_RATE_PRICE;
                        break;
                    case R.id.ratingRadioButtonInterior:
                        order = Constants.FILTER_RATE_INTERIOR;
                        break;
                    case R.id.ratingRadioButtonServiceQuality:
                        order = Constants.FILTER_RATE_QUALITY;
                        break;
                }
            }
        });


        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                loadPlaces(0, placesCategoriesAdapter.getCategoryId(), "", order);
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void gfdsgsd(EventBusMessages.SortPlaces event){
        placeList = new ArrayList<>();
        placesProgressBar.setVisibility(View.VISIBLE);
        loadPlaces(0, placesCategoriesAdapter.getCategoryId(), search.getText().toString(), order);
        search.clearFocus();
    }

    private void loadPlaces(final int offset, int category, String search, String order) {
        ApiFactory.getApi().getPlaces(App.accessToken(), offset, App.chosenCity(), category, order, search, 1).enqueue(new retrofit2.Callback<ResponseContainer<ResponsePlaces>>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseContainer<ResponsePlaces>> call, retrofit2.Response<ResponseContainer<ResponsePlaces>> response) {
                if(response.body()!=null){
                    totalCount = response.body().getResponse().getCount();
                    placesProgressBar.setVisibility(View.GONE);
                    if(offset==0){
                        placeList = response.body().getResponse().getItems();
                    } else {
                        placeList.addAll(response.body().getResponse().getItems());
                    }
                    adapter.update(placeList);

                    if(response.body().getResponse().getItems().size()==0){
                        placesFragmentMessage.setVisibility(View.VISIBLE);
                    } else {
                        placesFragmentMessage.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseContainer<ResponsePlaces>> call, Throwable t) {
            }
        });
    }

    private void loadCategories() {
        ApiFactory.getApi().getPlaceCategories(App.accessToken()).enqueue(new Callback<PlaceCategoryResponce>() {
            @Override
            public void onResponse(Call<PlaceCategoryResponce> call, Response<PlaceCategoryResponce> response) {
                if(response.body()!=null&&response.body().getResponse()!=null){
                    placeCategories.addAll(response.body().getResponse());
                    placesCategoriesAdapter.update(placeCategories);
                }
            }

            @Override
            public void onFailure(Call<PlaceCategoryResponce> call, Throwable t) {
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
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
