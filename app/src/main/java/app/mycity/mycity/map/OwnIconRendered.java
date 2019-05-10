package app.mycity.mycity.map;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class OwnIconRendered extends DefaultClusterRenderer<AbstractMarker> {

    public OwnIconRendered(Context context, GoogleMap map, ClusterManager<AbstractMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(AbstractMarker item, MarkerOptions markerOptions) {
        markerOptions.icon(item.getIcon());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }


    @Override
    protected boolean shouldRenderAsCluster(Cluster<AbstractMarker> cluster) {
        return cluster.getSize()>=2;
    }
}
