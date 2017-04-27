package co.moonmonkeylabs.realmmapview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import co.moonmonkeylabs.realmmap.R;
import io.realm.RealmChangeListener;
import io.realm.RealmClusterItem;
import io.realm.RealmClusterManager;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * A fragment that wraps a {@link SupportMapFragment} with added {@link ClusterManager} support and
 * built-in support for rendering a {@link RealmResults} and auto-updating.
 * <p>
 * Subclasses must provide a RealmResults through the {@link #getRealmResults()} abstract method.
 */
public abstract class RealmClusterMapFragment<M extends RealmObject & ClusterItem> extends Fragment implements OnMapReadyCallback {

    private static final String BUNDLE_LATITUDE = "latitude";
    private static final String BUNDLE_LONGITUDE = "longitude";
    private static final String BUNDLE_ZOOM = "zoom";

    private static final int DEFAULT_MIN_CLUSTER_SIZE = 4;
    private static final double DEFAULT_LATITUDE = 29.7530955;
    private static final double DEFAULT_LONGITUDE = -95.3600552;
    private static final float DEFAULT_ZOOM = 10;

    private RealmResults<M> realmResults;
    private RealmChangeListener<RealmResults<M>> changeListener;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private CameraUpdate savedCamera;
    private RealmClusterManager<M> manager;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout)
                inflater.inflate(R.layout.realm_cluster_map_fragment, container, false);
        GoogleMapOptions options = new GoogleMapOptions();
        configureMapOptions(options);
        mapFragment = SupportMapFragment.newInstance(options);
        getChildFragmentManager().beginTransaction().replace(layout.getId(), mapFragment).commit();
        return layout;
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

    @Override
    public void onDestroyView() {
        if (realmResults != null && changeListener != null) {
            realmResults.removeChangeListener(changeListener);
        }
        super.onDestroyView();
    }

    /**
     * Provides the RealmObjects to be displayed on the map. The RealmObjects must implement
     * {@link ClusterItem}.
     */
    protected abstract RealmResults<M> getRealmResults();

    /**
     * Override to set custom options for the map before it is created.
     * UI options such as enabling/disabling controls and gestures go here. For a full list of
     * options, see <a href="https://goo.gl/HP1bjC">GoogleMapOptions documentation</a>.
     * <p>
     * By default this method does nothing.
     */
    protected void configureMapOptions(GoogleMapOptions options) {}

    /**
     * Override to customize the map after it is ready.
     * Add items, set the map type, move the camera, and enable user location here.
     * For a full list of options, see <a href="https://goo.gl/pWVYDT">GoogleMap documentation</a>.
     * <p>
     * By default this will move the camera to either the location set in
     * {@link #onSaveInstanceState(Bundle)} or the default location if a saved instance state is
     * unavailable. If you wish to keep this behavior, call super method before configuring further.
     */
    public void configureMap(GoogleMap googleMap) {
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

    /**
     * Override if a custom {@link ClusterRenderer} is desired.
     */
    protected ClusterRenderer<RealmClusterItem<M>> getClusterRenderer
    (Context context, GoogleMap map,ClusterManager<RealmClusterItem<M>> manager) {
        DefaultClusterRenderer<RealmClusterItem<M>> renderer =
                new DefaultClusterRenderer<>(context, map, manager);
        renderer.setMinClusterSize(getDefaultMinClusterSize());
        return renderer;
    }

    public RealmClusterManager<M> getClusterManager() {
        return manager;
    }

    /**
     * Override if a specific minimum cluster size is desired. This does not apply if
     * {@link #getClusterRenderer} has been overridden.
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
    public float getDefaultZoom() {
        return DEFAULT_ZOOM;
    }

    /**
     * Clears and reloads all map items.
     */
    public void notifyDataSetChanged() {
        if (manager != null) {
            manager.updateRealmResults(realmResults);
        }
    }

    private void setUpMapIfNeeded() {
        if (map != null) {
            return;
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * Sets up clustering, autoUpdate, and listeners.
     * If additional configuration to the map is required, override {@link #configureMap(GoogleMap)}.
     */
    @Override
    public final void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            throw new IllegalStateException("Map not found in fragment.");
        }
        map = googleMap;

        // Set up cluster manager and renderer
        manager = new RealmClusterManager<>(getActivity(), map);
        manager.setRenderer(getClusterRenderer(getActivity(), map, manager));

        realmResults = getRealmResults();
        manager.updateRealmResults(realmResults);

        // Set change listener on results
        if (changeListener == null) {
            changeListener = new RealmChangeListener<RealmResults<M>>() {
                @Override
                public void onChange(RealmResults<M> element) {
                    notifyDataSetChanged();
                }
            };
            realmResults.addChangeListener(changeListener);
        }

        // Set up map callbacks
        map.setOnCameraIdleListener(manager);
        map.setOnMarkerClickListener(manager);
        map.setOnInfoWindowClickListener(manager);

        configureMap(map);
    }
}
