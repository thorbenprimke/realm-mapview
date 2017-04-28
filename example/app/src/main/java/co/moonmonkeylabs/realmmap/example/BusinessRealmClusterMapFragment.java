package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Locale;

import co.moonmonkeylabs.realmmapview.RealmClusterMapFragment;
import io.realm.Realm;
import io.realm.RealmClusterItem;
import io.realm.RealmResults;

/**
 * Implementation of {@link RealmClusterMapFragment} for the {@link Business} class.
 */
public class BusinessRealmClusterMapFragment extends RealmClusterMapFragment<Business> {

    Realm realm;
    RealmResults<Business> realmResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realm = Realm.getDefaultInstance();
        realmResults =  realm.where(Business.class)
                .isNotNull("latitude")
                .isNotNull("longitude").findAll();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    // Set data
    @Override
    protected RealmResults<Business> getRealmResults() {
        return realmResults;
    }

    // Overriding default camera location and zoom
    @Override
    public double getDefaultLatitude() {
        return MainActivity.DEFAULT_LATITUDE;
    }

    @Override
    public double getDefaultLongitude() {
        return MainActivity.DEFAULT_LONGITUDE;
    }

    @Override
    public float getDefaultZoom() {
        return 9.75f;
    }

    // Configure map controls and gestures
    @Override
    public void configureMapOptions(GoogleMapOptions options) {
        super.configureMapOptions(options);
        options.zoomControlsEnabled(true);
        options.tiltGesturesEnabled(false);
//        options.mapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    // Configure map features and appearance
    @Override
    protected void configureMap(GoogleMap googleMap) {
        super.configureMap(googleMap);
        googleMap.setTrafficEnabled(true);
        googleMap.setBuildingsEnabled(false);
//        googleMap.setMyLocationEnabled(); Requires location permissions

        // Perform action when marker info popup is clicked
        getClusterManager().setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<RealmClusterItem<Business>>() {
            @Override
            public void onClusterItemInfoWindowClick(RealmClusterItem<Business> item) {
                // Get access to the original RealmObject through the ClusterItem
                Business realmObject = item.getRealmObject();
                String text = String.format(Locale.US, "Tapped %s (id = %d)", realmObject.name,
                        realmObject.id);
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
