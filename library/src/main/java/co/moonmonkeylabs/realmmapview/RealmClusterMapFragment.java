package co.moonmonkeylabs.realmmapview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import co.moonmonkeylabs.realmmap.R;
import io.realm.Realm;
import io.realm.RealmClusterItem;
import io.realm.RealmClusterManager;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * A fragment that wraps a {@link SupportMapFragment} with added {@link ClusterManager} support and
 * built-in support for querying and rendering a {@link Realm} result list.
 *
 * Any subclasses must provide a class that extends {@link RealmObject} as the generic type and
 * implemented the three abstract methods that provide the title, latitude and longitude column
 * names.
 */
public abstract class RealmClusterMapFragment<M extends RealmObject & ClusterItem> extends Fragment implements OnMapReadyCallback {

    private static final String BUNDLE_LATITUDE = "latitude";
    private static final String BUNDLE_LONGITUDE = "longitude";
    private static final String BUNDLE_ZOOM = "zoom";

    private static final int DEFAULT_MIN_CLUSTER_SIZE = 9;
    private static final double DEFAULT_LATITUDE = 37.791116;
    private static final double DEFAULT_LONGITUDE = -122.403816;
    private static final int DEFAULT_ZOOM = 10;

    private GoogleMap map;
    private CameraUpdate savedCamera;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.realm_cluster_map_fragment, container, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && map != null) {
            savedCamera = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(
                            savedInstanceState.getDouble(BUNDLE_LATITUDE),
                            savedInstanceState.getDouble(BUNDLE_LONGITUDE)),
                    savedInstanceState.getFloat(BUNDLE_ZOOM));
        }

        setUpMapIfNeeded();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map != null) {
            final CameraPosition cameraPosition = map.getCameraPosition();
            outState.putDouble(BUNDLE_LATITUDE, cameraPosition.target.latitude);
            outState.putDouble(BUNDLE_LONGITUDE, cameraPosition.target.longitude);
            outState.putFloat(BUNDLE_ZOOM, cameraPosition.zoom);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Provides the RealmObjects to be displayed on the map. The RealmObjects must implement
     * {@link ClusterItem}.
     */
    protected abstract RealmResults<M> getRealmResults();

    /**
     * Override if a specific minimum cluster size is desired.
     */
    public int getDefaultMinClusterSize() {
        return DEFAULT_MIN_CLUSTER_SIZE;
    }

    /**
     * Override if a specific starting latitude is desired.
     */
    public double getDefaultLatitude() {
        return DEFAULT_LATITUDE;
    }

    /**
     * Override if a specific starting longitude is desired.
     */
    public double getDefaultLongitude() {
        return DEFAULT_LONGITUDE;
    }

    /**
     * Override if a specific starting zoom level is desired.
     */
    public int getDefaultZoom() {
        return DEFAULT_ZOOM;
    }

    @SuppressWarnings("unchecked")
    private void setUpMapIfNeeded() {
        if (map != null) {
            return;
        }
        SupportMapFragment fragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.support_map_fragment);
        if (fragment == null) {
            throw new IllegalStateException("Map fragment not found.");
        }
        fragment.getMapAsync(this);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            throw new IllegalStateException("Map not found in fragment.");
        }
        map = googleMap;

        // Set up cluster manager and renderer
        RealmClusterManager<M> realmClusterManager =
                new RealmClusterManager<>(getActivity(), map);

        DefaultClusterRenderer<RealmClusterItem<M>> renderer =
                new DefaultClusterRenderer<>(getActivity(), map, realmClusterManager);
        renderer.setMinClusterSize(getDefaultMinClusterSize());
        realmClusterManager.setRenderer(renderer);

        RealmResults<M> realmResults = getRealmResults();
        realmClusterManager.updateRealmResults(realmResults);

        // Set up map callbacks
        map.setOnCameraIdleListener(realmClusterManager);
        map.setOnMarkerClickListener(realmClusterManager);
        map.setOnInfoWindowClickListener(realmClusterManager);

        if (savedCamera != null) {
            // Restore camera position/zoom
            map.moveCamera(savedCamera);
            savedCamera = null;
        } else {
            // Configure default camera position/zoom
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(getDefaultLatitude(), getDefaultLongitude()),
                    getDefaultZoom()));
        }
    }
}
