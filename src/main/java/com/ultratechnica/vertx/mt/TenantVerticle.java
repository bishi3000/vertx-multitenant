package com.ultratechnica.vertx.mt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.platform.Verticle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.ultratechnica.vertx.mt.FolderUtil.*;
import static com.ultratechnica.vertx.mt.TenantUtil.*;
import static com.ultratechnica.vertx.mt.TenantUtil.initialise;

/**
 * User: keithbishop
 * Date: 05/08/2014
 * Time: 00:20
 */
public class TenantVerticle extends Verticle {

    public final int DEFAULT_REFRESH_INTERVAL = 10000;

    private AmazonS3 s3Client;

    @Override
    public void start(final Future<Void> result) {

        JsonObject config = container.config();
        final JsonObject tenantVerticleConfig = config.getObject("TenantVerticle");

        Number refreshInterval = tenantVerticleConfig.getNumber("refreshInterval");

        if (refreshInterval == null) {
            container.logger().warn("No [refreshInterval] parameter was specified for [TenantVerticle] configuration, defaulting to [" + DEFAULT_REFRESH_INTERVAL + "] millis");
            refreshInterval = DEFAULT_REFRESH_INTERVAL;
        }

        getTenantConfig(result, tenantVerticleConfig);

        vertx.setPeriodic(refreshInterval.longValue(), new Handler<Long>() {
            @Override
            public void handle(Long aLong) {
                getTenantConfig(result, tenantVerticleConfig);
            }
        });
    }

    private void getTenantConfig(Future<Void> result, JsonObject config) {
        JsonObject s3connectionDetails = config.getObject("s3connectionDetails");
        String bucket = s3connectionDetails.getString("bucket");
        String folder = s3connectionDetails.getString("folder");

        container.logger().info("Conecting to bucket [" + s3connectionDetails + "]");

        s3Client = new AmazonS3Client();

        initialise(vertx);
        FolderUtil.initialise(bucket, folder);

        String indexKey = getIndexKey();
        String tenantIndex = getConfig(bucket, indexKey);
        JsonObject index = new JsonObject(tenantIndex);
        Set<String> fieldNames = index.getFieldNames();

        ConcurrentSharedMap<String, String> tenantsIndex = getIndexMap();

        for (String fieldName : fieldNames) {

            container.logger().info("loading tenant information for [" + fieldName + "]");

            String tenantId = index.getString(fieldName);
            tenantsIndex.put(fieldName, tenantId);

            ObjectListing objectListing = s3Client.listObjects(bucket, getConfigFolderKey(tenantId));
            List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();

            for (S3ObjectSummary objectSummary : objectSummaries) {

                String key = objectSummary.getKey();
                String tenantConfig = getConfig(bucket, key);
                container.logger().info("loading config [" + key + "]");

                ConcurrentSharedMap<String, String> map = getTenantMap(tenantId);

                map.put(key, tenantConfig);
                map.put(getHashcodeKey(key), objectSummary.getETag());
            }
        }

        result.setResult(null);
    }


    private String getConfig(String bucket, String key) {

        S3Object object = s3Client.getObject(bucket, key);
        S3ObjectInputStream objectContent = object.getObjectContent();

        return readConfig(objectContent);
    }

    private String readConfig(S3ObjectInputStream objectContent) {
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
