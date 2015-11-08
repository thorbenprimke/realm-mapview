package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import co.moonmonkeylabs.realmmap.example.models.Business;
import io.realm.Realm;
import io.realm.RealmClusterManager;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private Realm realm;
    private GoogleMap map;

    private RealmClusterManager<Business> realmClusterManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        resetRealm();
        realm = Realm.getInstance(this);

        realm.beginTransaction();
        final List<Business> businesses = loadBusinessesData();
        realm.copyToRealm(businesses);
        realm.commitTransaction();

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (map != null) {
            return;
        }
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (map != null) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.791116, -122.403816), 10));

            realmClusterManager = new RealmClusterManager<>(this, getMap());
            RealmResults<Business> businessModel =
                    realm.where(Business.class).findAll();
            realmClusterManager.addRealmResultItems(businessModel);

            getMap().setOnCameraChangeListener(realmClusterManager);
        }
    }

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return map;
    }


    public final List<Business> loadBusinessesData() {
        List<Business> businesses = new ArrayList<>();

        InputStream is = getResources().openRawResource(R.raw.businesses);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null && lineNumber < 500) {
                if (lineNumber++ == 0) {
                    continue;
                }

                String[] rowData = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (rowData[6].isEmpty()) {
                    continue;
                }

                businesses.add(new Business(
                        Integer.parseInt(rowData[0]),
                        removeQuotes(rowData[1]),
                        Float.parseFloat(removeQuotes(rowData[6])),
                        Float.parseFloat(removeQuotes(rowData[7]))));
            }
        }
        catch (IOException ex) {}
        finally {
            try {
                is.close();
            }
            catch (IOException e) {}
        }
        return businesses;
    }

    private String removeQuotes(String original) {
        return original.subSequence(1, original.length() - 1).toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    private void resetRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.deleteRealm(realmConfig);
    }
}
