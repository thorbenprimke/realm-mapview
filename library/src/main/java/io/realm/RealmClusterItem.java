package io.realm;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * A wrapper around a {@link RealmObject} and cache for title, latitude, and longitude values so
 * that the wrapper class can be used on the background thread for marker calculation.
 */
public class RealmClusterItem<T extends RealmObject & ClusterItem> implements ClusterItem {

    private T realmObject;
    private LatLng latLng;
    private String title;
    private String snippet;

    public RealmClusterItem(T realmObject) {
        this.realmObject = realmObject;
        this.title = realmObject.getTitle();
        this.latLng = realmObject.getPosition();
        this.snippet = realmObject.getSnippet();
    }

    /**
     * Returns the original RealmObject used to create the cluster item. Make sure the returned
     * object is only used on the UI thread or else an exception will be thrown.
     */
    public T getRealmObject() {
        return realmObject;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}