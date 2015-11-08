package io.realm;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import io.realm.annotations.RealmClass;

public class RealmClusterWrapper<T extends RealmObject> implements ClusterItem {

    private T realmObj;

    private LatLng latLng;

    public RealmClusterWrapper(T realmObj, double latitude, double longitude) {
        this.realmObj = realmObj;
        latLng = new LatLng(latitude, longitude);
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }
}
