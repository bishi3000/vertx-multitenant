package com.ultratechnica.vertx.mt;

import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * User: keith bishop
 * Date: 05/08/2014
 * Time: 00:20
 */
public class TenantVerticle extends Verticle {

    public final int DEFAULT_REFRESH_INTERVAL = 10000;

    @Override
    public void start(final Future<Void> result) {

        JsonObject config = container.config();
        final JsonObject tenantVerticleConfig = config.getObject("TenantVerticle");

        Number refreshInterval = tenantVerticleConfig.getNumber("refreshInterval");

        if (refreshInterval == null) {
            container.logger().warn("No [refreshInterval] parameter was specified for [TenantVerticle] configuration, defaulting to [" + DEFAULT_REFRESH_INTERVAL + "] millis");
            refreshInterval = DEFAULT_REFRESH_INTERVAL;
        }

        final TenantHandler handler = getTenantHandler(tenantVerticleConfig.getString("provider"));

        try {
            handler.getTenantConfig(result, tenantVerticleConfig);
        } catch (Exception e) {
            e.printStackTrace();
            container.logger().error("Unable to load tenant configuration [" + e.getMessage() + "]");
        }

        vertx.setPeriodic(refreshInterval.longValue(), new Handler<Long>() {
            @Override
            public void handle(Long aLong) {

                try {
                    handler.getTenantConfig(result, tenantVerticleConfig);
                } catch (Exception e) {
                    container.logger().error("Unable to load tenant configuration [" + e.getMessage() + "]");
                }
            }
        });
    }

    private TenantHandler getTenantHandler(String providerName) {

        if (providerName != null) {

            Provider provider = Provider.valueOf(providerName);

            switch (provider) {
                case ClassPath: return new ClasspathTenantHandler(vertx, container);
                case S3:        return new S3TenantHandler(vertx, container);
                default:        return new S3TenantHandler(vertx, container);
            }
        } else {
            return new S3TenantHandler(vertx, container);
        }
    }
}
