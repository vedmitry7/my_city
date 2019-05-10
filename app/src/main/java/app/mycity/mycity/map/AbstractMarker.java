package app.mycity.mycity.map;


import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class AbstractMarker implements ClusterItem {

    private String mId;
    private LatLng mPosition;
    private String mTitle;
    BitmapDescriptor mIcon;

    public AbstractMarker(double lat, double lng, String title, String id, BitmapDescriptor icon) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mId = id;
        mIcon = icon;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public String getId() {
        return mId;
    }

    public BitmapDescriptor getIcon() {
        return mIcon;
    }
}
