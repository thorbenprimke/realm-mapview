package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        resetRealm();
        Realm.setDefaultConfiguration(getRealmConfig());
        loadDataIntoRealm();
        setContentView(R.layout.main_activity);
    }

    private void loadDataIntoRealm() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        final List<Business> businesses = new SFRestaurantDataLoader().loadBusinessesData(this);
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
