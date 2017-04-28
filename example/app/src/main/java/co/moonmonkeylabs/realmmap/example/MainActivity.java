package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    public static final double DEFAULT_LATITUDE = 29.7530955;
    public static final double DEFAULT_LONGITUDE = -95.3600552;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        Realm.deleteRealm(config);

        // Add 100 places to realm
        createDataSet();

        // Sets layout with map fragment
        setContentView(R.layout.main_activity);
        BusinessRealmClusterMapFragment fragment = new BusinessRealmClusterMapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void createDataSet() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Random r = new Random();
                for (int i = 0; i < 100; i++) {
                    Business business = new Business();
                    business.id = i;
                    business.name = "Location " + i;
                    business.description = "Description";

                    float offset1 = r.nextFloat() / 2 - 0.25f;
                    float offset2 = r.nextFloat() / 2 - 0.25f;
                    business.latitude = ((float) DEFAULT_LATITUDE) + offset1;
                    business.longitude = ((float) DEFAULT_LONGITUDE) + offset2;
                    realm.insert(business);
                }
            }
        });
        realm.close();
    }
}
