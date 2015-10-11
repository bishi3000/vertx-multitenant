package com.ultratechnica.vertx.mt;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import static com.ultratechnica.vertx.mt.FolderUtil.getTenantMapKey;
import static com.ultratechnica.vertx.mt.MapKey.TENANTS_INDEX;

/**
 * User: keith bishop
 * Date: 29/04/15
 * Time: 23:22
 */
public class TenantUtil {

    private static Vertx vertx = null;

    private static TenantUtil instance;

    private TenantUtil(Vertx vertx) {
        this.vertx = vertx;
    }

    public static void initialise(Vertx vertx) {

        if (instance == null) {
            instance = new TenantUtil(vertx);
        }
    }

    public static <K, V> ConcurrentSharedMap<K, V> getTenantMap(String tenantId) {

        String key = getTenantMapKey(tenantId);

        return vertx.sharedData().getMap(key);
    }

    public static <K, V> ConcurrentSharedMap<K, V> getIndexMap() {
        return vertx.sharedData().getMap(TENANTS_INDEX.key());
    }
}
