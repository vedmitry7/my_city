package app.mycity.mycity.filter_desc_post;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.ArrayList;
import java.util.List;

import app.mycity.mycity.R;
import app.mycity.mycity.util.BitmapUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FiltersListFragment extends Fragment implements FilterThumbnailsAdapter.ThumbnailsAdapterListener {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    FilterThumbnailsAdapter mAdapter;

    List<ThumbnailItem> thumbnailItemList;

    FiltersListFragmentListener listener;

    Bitmap image;

    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    public FiltersListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filters_list, container, false);

        ButterKnife.bind(this, view);

        thumbnailItemList = new ArrayList<>();
        mAdapter = new FilterThumbnailsAdapter(getActivity(), thumbnailItemList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new FiltersThumbSpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);

        if(image!=null)
        prepareThumbnail(image);

        return view;
    }

    public void prepareThumbnail(final Bitmap bitmap) {

        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage;

                if (bitmap == null) {
                    thumbImage = BitmapUtils.getBitmapFromAssets(getActivity(), "logo_white_no_shadow.png", 100, 100);
                } else {
                    thumbImage = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/4, bitmap.getHeight()/4, false);
                }

                if (thumbImage == null)
                    return;

                FilterThumbnailsManager.clearThumbs();
                thumbnailItemList.clear();

                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImage;
                thumbnailItem.filterName = getString(R.string.filter_normal);
                FilterThumbnailsManager.addThumb(thumbnailItem);

                List<Filter> filters = FilterPack.getFilterPack(getActivity());

                for (Filter filter : filters) {
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImage;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    FilterThumbnailsManager.addThumb(tI);
                }

                thumbnailItemList.addAll(FilterThumbnailsManager.processThumbs(getActivity()));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        new Thread(r).start();
    }

    public void reloadIndicator(){
        mAdapter.setNormalFilter();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (listener != null)
            listener.onFilterSelected(filter);
    }

    public void addImage(Bitmap originalImage) {
        image = originalImage;
    }

    public interface FiltersListFragmentListener {
        void onFilterSelected(Filter filter);
    }
}
