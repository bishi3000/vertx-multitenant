package com.ultratechnica.vertx.mt.integration.java;

import com.ultratechnica.vertx.mt.BusAddress;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.testtools.TestVerticle;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * Title: ForceRefreshS3Test
 * Copyright (c) Keith Bishop 2017
 * Date: 24/01/2017
 *
 * @author RossGlenn
 * @version 1.0
 */
public class ForceRefreshS3Test extends TestVerticle {

    @Test
    public void startTest() {

        container.logger().info("Requesting refresh...");

        vertx.eventBus().send(BusAddress.FORCE_REFRESH.toString(), new JsonObject("{}"), new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {

                container.logger().info("Refresh complete");
                container.logger().info("Message Response:\n" + message.body().encodePrettily());

                testConcurrentRequests();
            }
        });
    }

    private void testConcurrentRequests() {

        final Logger logger = container.logger();

        container.logger().info("Requesting multiple refreshes....");

        ExecutorService service = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 3; i++) {

            service.execute(new Runnable() {

                @Override
                public void run() {

                    final JsonObject jo = new JsonObject();
                    jo.putString("id", UUID.randomUUID().toString());

                    vertx.eventBus().send(BusAddress.FORCE_REFRESH.toString(), jo, new Handler<Message<JsonObject>>() {

                        @Override
                        public void handle(Message<JsonObject> event) {
                            logger.info("Message Response:\n" + event.body().encodePrettily());
                        }
                    });
                }
            });
        }

        try {

            service.shutdown();
            service.awaitTermination(5, TimeUnit.SECONDS);

            testComplete();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

        initialize();

        Buffer buffer = vertx.fileSystem().readFileSync("src/test/resources/mod-default-conf.json");

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
