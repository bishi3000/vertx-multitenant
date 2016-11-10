package com.ultratechnica.vertx.mt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.vertx.java.core.Future;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.platform.Container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class S3TenantHandler implements TenantHandler {

    private final Vertx vertx;

    private final Container container;

    private AmazonS3 s3Client;

    boolean initialised;

    public S3TenantHandler(Vertx vertx, Container container) {
        this.vertx = vertx;
        this.container = container;
    }

    @Override
    public void getTenantConfig(Future<Void> result, JsonObject config) {

        JsonObject s3connectionDetails = config.getObject("s3connectionDetails");
        String bucket = s3connectionDetails.getString("bucket");
        String folder = s3connectionDetails.getString("folder");

        container.logger().info("Conecting to bucket [" + s3connectionDetails + "]");

        s3Client = new AmazonS3Client();

        TenantUtil.initialise(vertx);
        FolderUtil.initialise(bucket, folder);

        String indexKey = FolderUtil.getIndexKey();
        String tenantIndex = getConfig(bucket, indexKey);
        JsonObject index = new JsonObject(tenantIndex);
        Set<String> fieldNames = index.getFieldNames();

        ConcurrentSharedMap<String, String> tenantsIndex = TenantUtil.getIndexMap();

        for (String fieldName : fieldNames) {

            container.logger().info("loading tenant information for [" + fieldName + "]");

            String tenantId = index.getString(fieldName);
            tenantsIndex.put(fieldName, tenantId);

            ObjectListing objectListing = s3Client.listObjects(bucket, FolderUtil.getConfigFolderKey(tenantId));
            List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();

            for (S3ObjectSummary objectSummary : objectSummaries) {

                String key = objectSummary.getKey();
                String tenantConfig = getConfig(bucket, key);

                if (!initialised) {
                    container.logger().debug("loading config [" + key + "]");
                }

                ConcurrentSharedMap<String, String> map = TenantUtil.getTenantMap(tenantId);

                map.put(key, tenantConfig);
                map.put(FolderUtil.getHashcodeKey(key), objectSummary.getETag());
            }
        }

        result.setResult(null);
    }

    String getConfig(String bucket, String key) {

        S3Object object = s3Client.getObject(bucket, key);
        S3ObjectInputStream objectContent = object.getObjectContent();

        return readConfig(objectContent);
    }

    String readConfig(S3ObjectInputStream objectContent) {

        String tenantConfig;
        ByteArrayOutputStream boas = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int result;
        try {
            while ((result = objectContent.read(buf)) > -1) {
                boas.write(buf, 0, result);
            }
        } catch (IOException e) {
            container.logger().error("Unable to locate tenant config", e);
        }

        tenantConfig = boas.toString();

        return tenantConfig;
    }
}