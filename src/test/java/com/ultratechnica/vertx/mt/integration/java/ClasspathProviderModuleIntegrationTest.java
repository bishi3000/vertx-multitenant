package com.ultratechnica.vertx.mt.integration.java;
/*
 *
 */

import com.ultratechnica.vertx.mt.TenantUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.testtools.TestVerticle;

import java.util.Arrays;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Test tenant module connects to AWS S3 bucket and retrieves test
 */
public class ClasspathProviderModuleIntegrationTest extends TestVerticle {

    @Test
    public void testConfigurationLoaded() {
        container.logger().info("in testConfigurationLoaded()");

        ConcurrentSharedMap<String, String> tenants_index = TenantUtil.getIndexMap();

        assertNotNull(tenants_index);
        assertTrue(tenants_index.size() > 0);

        testComplete();
    }

    @Override
    public void start() {

        initialize();

        Buffer buffer = vertx.fileSystem().readFileSync("src/test/resources/mod-default-conf-classpath.json");

        JsonObject config = new JsonObject(buffer.toString());

        container.deployModule(System.getProperty("vertx.modulename"), config.getObject("MultitenantModule"), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {

                assertTrue(asyncResult.succeeded());

                if (!asyncResult.succeeded()) {
                    asyncResult.cause().printStackTrace();
                }

                assertNotNull("deploymentID should not be null", asyncResult.result());

                startTests();
            }
        });
    }
}
