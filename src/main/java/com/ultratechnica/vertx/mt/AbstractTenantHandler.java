package com.ultratechnica.vertx.mt;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Title: AbstractTenantHandler
 * Copyright (c) Keith Bishop 2017
 * Date: 24/01/2017
 *
 * @author RossGlenn
 * @version 1.0
 */
public abstract class AbstractTenantHandler implements TenantHandler {

    protected ConcurrentLinkedQueue refreshQueue = new ConcurrentLinkedQueue();

    public void queueRefresh() {
        refreshQueue.offer(new Object());
    }
}
