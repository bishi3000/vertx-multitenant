package com.ultratechnica.vertx.mt;

/**
 * User: keithbishop
 * Date: 27/04/15
 * Time: 22:02
 */
public enum MapKey {

    TENANTS_INDEX("tenants_index");

    private final String key;

    MapKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
