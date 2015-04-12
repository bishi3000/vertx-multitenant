package com.ultratechnica.vertx.mt.integration.java;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Example Java integration test that deploys the module that this project builds.
 * <p/>
 * Quite often in integration tests you want to deploy the same module for all tests and you don't want tests
 * to start before the module has been deployed.
 * <p/>
 * This test demonstrates how to do that.
 */
public class ModuleIntegrationTest extends TestVerticle {

    @Test
    public void testConfigurationLoaded() {
        container.logger().info("in testConfigurationLoaded()");

        ConcurrentSharedMap<String, String> tenants_index = vertx.sharedData().getMap("tenants_index");

        assertNotNull(tenants_index);
        assertTrue(tenants_index.size() > 0);

        testComplete();
    }

    @Override
    public void start() {

        initialize();

        Buffer buffer = vertx.fileSystem().readFileSync("src/test/resources/mod-default-conf.json");

        JsonObject config = new JsonObject(buffer.toString());

        container.deployModule(System.getProperty("vertx.modulename"), config.getObject("TenantVerticle"), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {

                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());

                startTests();
            }
        });
    }
}
