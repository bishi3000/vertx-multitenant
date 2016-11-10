package com.ultratechnica.vertx.mt;

import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;

/**
 * User: keith bishop
 * Date: 10/11/16
 * Time: 18:01
 */
public interface TenantHandler {

    void getTenantConfig(Future<Void> result, JsonObject config) throws Exception;
}
