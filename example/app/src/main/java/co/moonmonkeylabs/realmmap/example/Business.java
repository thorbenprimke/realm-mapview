package co.moonmonkeylabs.realmmap.example;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import io.realm.RealmObject;

public class Business extends RealmObject implements ClusterItem {

    public int id;
    public String name;
    public String description;
    public Float latitude;
    public Float longitude;

    @Override
    public LatLng getPosition() {
        if (latitude != null && longitude != null) {
            return new LatLng(latitude, longitude);
        } else {
            return null;
        }
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return description;
    }
}
