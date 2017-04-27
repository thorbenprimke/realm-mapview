package io.realm;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of the {@link ClusterManager} that handles processing a {@link RealmResults}
 * list and lookup of the respective columnName/Index to query the lat/long in order to store them
 * in the {@link RealmClusterItem}.
 */
public class RealmClusterManager<M extends RealmObject & ClusterItem>
        extends ClusterManager<RealmClusterItem<M>> {

    public RealmClusterManager(Context context, GoogleMap map) {
        super(context, map);
    }

    public RealmClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        super(context, map, markerManager);
    }

    @Override
    public void addItems(Collection<RealmClusterItem<M>> items) {
        throw new IllegalStateException("Use updateRealmResults instead");
    }

    @Override
    public void addItem(RealmClusterItem<M> myItem) {
        throw new IllegalStateException("Use addRealmResultItems instead");
    }

    public void updateRealmResults(RealmResults<M> realmResults) {
        super.clearItems();

        List<RealmClusterItem<M>> items = new ArrayList<>(realmResults.size());
        for (M item : realmResults) {
            if (item.getPosition() != null) {
                items.add(new RealmClusterItem<M>(item));
            }
        }
        super.addItems(items);
    }
}
