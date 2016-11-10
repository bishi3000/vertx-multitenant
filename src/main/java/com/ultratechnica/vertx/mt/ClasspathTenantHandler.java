package com.ultratechnica.vertx.mt;

import org.vertx.java.core.Future;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.platform.Container;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import java.util.Set;

import static com.ultratechnica.vertx.mt.FolderUtil.*;
import static com.ultratechnica.vertx.mt.TenantUtil.*;
import static com.ultratechnica.vertx.mt.TenantUtil.initialise;

/**
 * A handler for retrieving the tenant configuration from a classpath location. Thus removing the need to pull
 * the tenant config from s3 (and preventing the clobbering of s3 tenant configs by various projects)
 * <p>
 * This module looks on the local classpath (e.g. "src/main/resources" or "src/test/resources")
 * for the tenant configurations.
 *
 * User: keith bishop
 * Date: 10/11/16
 * Time: 17:40
 */
public class ClasspathTenantHandler implements TenantHandler {

    private final Vertx vertx;

    private final Container container;

    public ClasspathTenantHandler(Vertx vertx, Container container) {

        this.vertx = vertx;
        this.container = container;
    }

    public void getTenantConfig(Future<Void> result, JsonObject config) throws Exception {

        String configDirName = config.getString("configDirName");

        container.logger().info("Retrieving tenant config from Classpath location [" + configDirName + "]");

        initialise(vertx);
        FolderUtil.initialise("", configDirName);

        String indexKey = getIndexKey();
        String tenantIndex = getConfig(indexKey);
        JsonObject index = new JsonObject(tenantIndex);
        Set<String> fieldNames = index.getFieldNames();

        ConcurrentSharedMap<String, String> tenantsIndex = getIndexMap();

        for (String fieldName : fieldNames) {

            String tenantId = index.getString(fieldName);
            tenantsIndex.put(fieldName, tenantId);

            File[] files = scanDir(getConfigFolderKey(tenantId));

            for (File file : files) {

                String configKey = getConfigFolderKey(tenantId) + file.getName();
                String tenantConfig = getConfig(configKey);

                container.logger().debug("loading config [" + configKey + "]");

                ConcurrentSharedMap<String, String> map = getTenantMap(tenantId);

                map.put(configKey, tenantConfig);
                map.put(getHashcodeKey(configKey), Integer.toString(tenantConfig.hashCode()));
            }
        }

        result.setResult(null);
    }

    private String getConfig(String fileName) throws FileNotFoundException {

        return readConfig(fileName);
    }

    private String readConfig(String name) throws FileNotFoundException {

        InputStream inputStream = getClass().getResourceAsStream(name);

        return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
    }

    private File[] scanDir(String dirName) throws URISyntaxException, IOException {

        URL resource = getClass().getResource(dirName);
        File dir = new File(resource.toURI());

        return dir.listFiles();
    }

}
