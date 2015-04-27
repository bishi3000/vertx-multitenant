package com.ultratechnica.vertx.mt;

/**
 * User: keithbishop
 * Date: 27/04/15
 * Time: 22:33
 */
public class FolderUtil {

    public static String getTenantMapKey(String tenantId) {
        return "tenants/" + tenantId;
    }

    public static String getConfigFolderKey(String folder, String tenantId) {
        return folder + "/" + tenantId + "/config/";
    }

    public static String getIndexKey(String folder) {
        return folder + "/" + "index.json";
    }
}
