package co.moonmonkeylabs.realmmapview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.moonmonkeylabs.realmmap.R;
import io.realm.Realm;
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
public abstract class RealmClusterMapFragment<M extends RealmObject> extends Fragment {

    private static final String BUNDLE_LATITUDE = "latitude";
    private static final String BUNDLE_LONGITUDE = "longitude";
    private static final String BUNDLE_ZOOM = "zoom";

    private static final double DEFAULT_LATITUDE = 37.791116;
    private static final double DEFAULT_LONGITUDE = -122.403816;
    private static final int DEFAULT_ZOOM = 10;

    private GoogleMap map;
    private Realm realm;
    private Class<M> clazz;

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
        clazz = (Class<M>) getTypeArguments(RealmClusterMapFragment.class, getClass()).get(0);
        realm = Realm.getDefaultInstance();
        setUpMapIfNeeded();

        if (savedInstanceState != null) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(
                            savedInstanceState.getDouble(BUNDLE_LATITUDE),
                            savedInstanceState.getDouble(BUNDLE_LONGITUDE)),
                    savedInstanceState.getFloat(BUNDLE_ZOOM)));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final CameraPosition cameraPosition = getMap().getCameraPosition();
        outState.putDouble(BUNDLE_LATITUDE, cameraPosition.target.latitude);
        outState.putDouble(BUNDLE_LONGITUDE, cameraPosition.target.longitude);
        outState.putFloat(BUNDLE_ZOOM, cameraPosition.zoom);
        maybeCloseRealm();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
        maybeCloseRealm();
    }

    /**
     * Needs to be implemented and provide the column name of the column that should be rendered
     * when a marker is clicked. The column type should be string.
     */
    protected abstract String getTitleColumnName();

    /**
     * Needs to be implemented and provide the column name of the column that contains the latitude
     * value for the extended {@link RealmObject}.
     */
    protected abstract String getLatitudeColumnName();


    /**
     * Needs to be implemented and provide the column name of the column that contains the longitude
     * value for the extended {@link RealmObject}.
     */
    protected abstract String getLongitudeColumnName();

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

    private void maybeCloseRealm() {
        if (realm == null) {
            return;
        }
        realm.close();
        realm = null;
    }

    @SuppressWarnings("unchecked")
    private void setUpMapIfNeeded() {
        if (map != null) {
            return;
        }
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.support_map_fragment);
        if (fragment == null) {
            throw new IllegalStateException("Map fragment not found.");
        }
        map = ((SupportMapFragment) fragment).getMap();
        if (map == null) {
            throw new IllegalStateException("Map not found in fragment.");
        }

        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(getDefaultLatitude(), getDefaultLongitude()),
                getDefaultZoom()));

        RealmClusterManager<M> realmClusterManager =
                new RealmClusterManager<>(getActivity(), getMap());
        RealmResults<M> realmResults = realm.where(clazz).findAll();
        realmClusterManager.updateRealmResults(
                realmResults,
                getTitleColumnName(),
                getLatitudeColumnName(),
                getLongitudeColumnName());

        realmClusterManager.setRenderer(
                new RealmClusterRenderer(getActivity(), getMap(), realmClusterManager));
        getMap().setOnCameraChangeListener(realmClusterManager);
        getMap().setOnMarkerClickListener(realmClusterManager);
        getMap().setOnInfoWindowClickListener(realmClusterManager);
    }

    private GoogleMap getMap() {
        setUpMapIfNeeded();
        return map;
    }

    //
    // The code below is copied from StackOverflow in order to avoid having to pass in the T as a
    // Class for the Realm query/filtering.
    // http://stackoverflow.com/a/15008017
    //
    /**
     * Get the underlying class for a type, or null if the type is a variable
     * type.
     *
     * @param type the type
     * @return the underlying class
     */
    private static Class<?> getClass(Type type)
    {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get the actual type arguments a child class has used to extend a generic
     * base class.
     *
     * @param baseClass the base class
     * @param childClass the child class
     * @return a list of the raw classes for the actual type arguments.
     */
    private static <T> List<Class<?>> getTypeArguments(
            Class<T> baseClass, Class<? extends T> childClass)
    {
        Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
        Type type = childClass;
        // start walking up the inheritance hierarchy until we hit baseClass
        while (!getClass(type).equals(baseClass)) {
            if (type instanceof Class) {
                // there is no useful information for us in raw types, so just keep going.
                type = ((Class) type).getGenericSuperclass();
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> rawType = (Class) parameterizedType.getRawType();

                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                }

                if (!rawType.equals(baseClass)) {
                    type = rawType.getGenericSuperclass();
                }
            }
        }

        // finally, for each actual type argument provided to baseClass, determine (if possible)
        // the raw class for that type argument.
        Type[] actualTypeArguments;
        if (type instanceof Class) {
            actualTypeArguments = ((Class) type).getTypeParameters();
        } else {
            actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        // resolve types by chasing down type variables.
        for (Type baseType : actualTypeArguments) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClass(baseType));
        }
        return typeArgumentsAsClasses;
    }
    //
    // End StackOverflow code
    //
}
