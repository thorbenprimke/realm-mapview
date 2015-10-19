package io.realm;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

public class RealmClusterManager<M extends RealmObject, T extends ClusterItem> extends ClusterManager<T> {

    public RealmClusterManager(Context context, GoogleMap map) {
        super(context, map);
    }

    public RealmClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        super(context, map, markerManager);
    }

    public void addRealmResults(RealmResults<M> realmResults) {
        // Needs implementation
    }
}
