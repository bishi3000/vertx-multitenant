package com.ultratechnica.vertx.mt;

/**
 * User: keithbishop
 * Date: 27/04/15
 * Time: 22:33
 */
public class FolderUtil {

    private static FolderUtil instance;

    private final String bucket;

    private final String folder;

    public static void initialise(String bucket, String folder) {

        if (instance == null) {
            instance = new FolderUtil(bucket, folder);
        }
    }

    public static FolderUtil getInstance() {

        if (instance == null) {
            throw new IllegalStateException("Unable to return singleton instance, please initialise it first");
        }

        return instance;
    }

    private FolderUtil(String bucket, String folder) {
        this.bucket = bucket;
        this.folder = folder;
    }
    public static String getTenantMapKey(String tenantId) {
        return "tenants/" + tenantId;
    }

    public static String getConfigFolderKey(String tenantId) {
        return instance.folder + "/" + tenantId + "/config/";
    }

    public static String getHashcodeFolderKey(String tenantId) {
        return instance.folder + "/" + tenantId + "/hashcode/";
    }

    public static String getIndexKey() {
        return instance.folder + "/" + "index.json";
    }

    public static String getHashcodeKey(String key) {
        return key.replace("config", "hashcode");
    }
}
