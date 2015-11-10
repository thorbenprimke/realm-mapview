package io.realm;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.internal.ColumnType;
import io.realm.internal.Row;
import io.realm.internal.Table;

/**
 * An implementation of the {@link ClusterManager} that handles processing a {@link RealmResults}
 * list and lookup of the respective columnName/Index to query the lat/long in order to store them
 * in the {@link RealmClusterWrapper}.
 */
public class RealmClusterManager<M extends RealmObject>
        extends ClusterManager<RealmClusterWrapper<M>> {

    private long titleColumnIndex = -1;

    public RealmClusterManager(Context context, GoogleMap map) {
        super(context, map);
    }

    public RealmClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        super(context, map, markerManager);
    }

    @Override
    public void addItems(Collection<RealmClusterWrapper<M>> items) {
        throw new IllegalStateException("Use updateRealmResults instead");
    }

    @Override
    public void addItem(RealmClusterWrapper<M> myItem) {
        throw new IllegalStateException("Use addRealmResultItems instead");
    }

    public void updateRealmResults(
            RealmResults<M> realmResults,
            String titleColumnName,
            String latitudeColumnName,
            String longitudeColumnName) {
        super.clearItems();
        final Table table = realmResults.getTable().getTable();

        titleColumnIndex = table.getColumnIndex(titleColumnName);
        if (titleColumnIndex == Table.NO_MATCH) {
            throw new IllegalStateException("titleColumnName not valid.");
        }
        long latIndex = table.getColumnIndex(latitudeColumnName);
        if (latIndex == Table.NO_MATCH) {
            throw new IllegalStateException("latitudeColumnName not valid.");
        }
        long longIndex = table.getColumnIndex(longitudeColumnName);
        if (longIndex == Table.NO_MATCH) {
            throw new IllegalStateException("longitudeColumnName not valid.");
        }

        List<RealmClusterWrapper<M>> wrappedItems = new ArrayList<>(realmResults.size());
        for (M realmResult : realmResults) {
            RealmClusterWrapper<M> wrappedItem = new RealmClusterWrapper<>(
                    realmResult,
                    getValue(realmResult.row, table.getColumnType(latIndex), latIndex),
                    getValue(realmResult.row, table.getColumnType(longIndex), longIndex));
            wrappedItems.add(wrappedItem);
        }
        super.addItems(wrappedItems);
    }

    private double getValue(Row row, ColumnType columnType, long columnIndex) {
        if (columnType == ColumnType.DOUBLE) {
            return row.getDouble(columnIndex);
        } else if (columnType == ColumnType.FLOAT) {
            return row.getFloat(columnIndex);
        } else if (columnType == ColumnType.INTEGER) {
            return row.getLong(columnIndex);
        }
        throw new IllegalStateException("The value type needs to be of double, float or int");
    }

    public long getTitleRealmColumnIndex() {
        return titleColumnIndex;
    }
}
