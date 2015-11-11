package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import co.moonmonkeylabs.realmsfrestaurantdata.SFRestaurantDataLoader;
import co.moonmonkeylabs.realmsfrestaurantdata.SFRestaurantModule;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Clear the realm of any previous data
        resetRealm();
        // Sets default realm with sample data module
        Realm.setDefaultConfiguration(getRealmConfig());
        // Loads and adds sample data to realm
        new SFRestaurantDataLoader().loadBusinessSmallDataSet(this);
        // Sets layout with map fragment
        setContentView(R.layout.main_activity);
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
