package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import co.moonmonkeylabs.realmsfrestaurantdata.SFRestaurantDataLoader;
import co.moonmonkeylabs.realmsfrestaurantdata.SFRestaurantModule;
import co.moonmonkeylabs.realmsfrestaurantdata.model.Business;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Realm reset/defaultConfig settings/data loading needs to happen before the view is set
        // in order for the map fragment to read the Realm when the data is already present.
        resetRealm();
        Realm.setDefaultConfiguration(getRealmConfig());
        loadDataIntoRealm();
        setContentView(R.layout.main_activity);
    }

    private void loadDataIntoRealm() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        final List<Business> businesses =
                new SFRestaurantDataLoader().loadBusinessSmallDataSet(this);
        realm.copyToRealm(businesses);
        realm.commitTransaction();
        realm.close();
    }

    private void resetRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.deleteRealm(realmConfig);
    }

    private RealmConfiguration getRealmConfig() {
        return new RealmConfiguration
                .Builder(this)
                .setModules(Realm.getDefaultModule(), new SFRestaurantModule())
                .build();
    }
}
