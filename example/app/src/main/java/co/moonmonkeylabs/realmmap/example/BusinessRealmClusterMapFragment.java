package co.moonmonkeylabs.realmmap.example;

import co.moonmonkeylabs.realmsfrestaurantdata.model.Business;
import co.moonmonkeylabs.realmmapview.RealmClusterMapFragment;

public class BusinessRealmClusterMapFragment extends RealmClusterMapFragment<Business> {

    @Override
    protected String getTitleColumnName() {
        return "name";
    }

    @Override
    protected String getLatitudeColumnName() {
        return "latitude";
    }

    @Override
    protected String getLongitudeColumnName() {
        return "longitude";
    }
}
