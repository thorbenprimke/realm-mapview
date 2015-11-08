package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmClusterManager;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RealmClusterMapFragment<M extends RealmObject> extends Fragment {

    private static final double DEFAULT_LATITUDE = 37.791116;
    private static final double DEFAULT_LONGITUDE = -122.403816;

    private GoogleMap map;
    private RealmClusterManager<M> realmClusterManager;

    private RealmResults<M> realmResults;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.realm_cluster_map_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpMapIfNeeded();
        if (savedInstanceState != null) {
            double latitude = savedInstanceState.getDouble("latitude");
            double longitude = savedInstanceState.getDouble("longitude");
            float zoom = savedInstanceState.getFloat("zoom");

            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("latitude", getMap().getCameraPosition().target.latitude);
        outState.putDouble("longitude", getMap().getCameraPosition().target.longitude);
        outState.putFloat("zoom", getMap().getCameraPosition().zoom);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (map != null) {
            return;
        }
        map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.support_map_fragment)).getMap();
        if (map != null) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), 10));

            realmClusterManager = new RealmClusterManager<>(getActivity(), getMap());
            if (realmResults != null) {
                realmClusterManager.addRealmResultItems(realmResults);
            }

            getMap().setOnCameraChangeListener(realmClusterManager);
        }
    }

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return map;
    }

    public void setRealmResults(RealmResults<M> realmResults) {
        this.realmResults = realmResults;
        if (realmClusterManager != null) {
            realmClusterManager.addRealmResultItems(realmResults);
        }
    }


}
