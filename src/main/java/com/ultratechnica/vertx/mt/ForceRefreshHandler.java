package com.ultratechnica.vertx.mt;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Title: ForceRefreshHandler
 * Copyright (c) Keith Bishop 2017
 * Date: 24/01/2017
 *
 * @author RossGlenn
 * @version 1.0
 */
public class ForceRefreshHandler implements Handler<Message<JsonObject>> {

    @Override
    public void handle(Message<JsonObject> message) {

    }
}
