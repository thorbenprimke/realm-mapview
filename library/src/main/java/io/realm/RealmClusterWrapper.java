package io.realm;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import io.realm.internal.Row;

/**
 * A wrapper around a {@link RealmObject} and cache for the latitude and longitude values so that
 * the wrapper class can be used on the background thread for marker calculation.
 */
public class RealmClusterWrapper<T extends RealmObject> implements ClusterItem {

    private T realmObject;

    private LatLng latLng;

    public RealmClusterWrapper(T realmObject, double latitude, double longitude) {
        this.realmObject = realmObject;
        latLng = new LatLng(latitude, longitude);
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    public Row getRealmRow() {
        return realmObject.row;
    }
}
