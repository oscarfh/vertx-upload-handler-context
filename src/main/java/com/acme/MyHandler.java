package com.acme;

import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.rxjava.core.Context;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.RoutingContext;
import rx.Single;

public class MyHandler implements Handler<RoutingContext> {
    @Override public void handle(RoutingContext routingContext) {
        Logger L = Logger.getLogger("MyHandler");

        Context context = Vertx.currentContext();
        L.warning("Context in handler: " + context);
        if (context == null) {
            throw new IllegalStateException("context should not be null");
        }

        final HttpServerRequest event = routingContext.request().getDelegate();
        event.setExpectMultipart(true);

        event.uploadHandler(event1 -> {
            Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
                Context context2 = Vertx.currentContext();
                L.warning("Context in uploadHandler:" + context2);
                if (context2 == null) {
                    throw new IllegalStateException("context should not be null inside the upload handler");
                }
                fut.handle(Future.succeededFuture());
                })).subscribe(o -> routingContext.end(), throwable -> routingContext.fail(500));
        });

    }
}
