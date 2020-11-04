package com.acme;

import java.util.logging.Logger;

import io.vertx.core.Promise;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;

public class MyModule extends AbstractVerticle {
    Logger L = Logger.getLogger("MyModule");
    @Override
    public void start(Promise startedResult) {
        Vertx vertx = new io.vertx.rxjava.core.Vertx(getVertx());
        Router router = Router.router(vertx);
        L.warning("running");
        router.route().handler(new MyHandler());

        HttpServer httpServer = vertx.createHttpServer();

        httpServer.requestHandler(router).rxListen(8080).doOnSuccess(httpServer1 -> startedResult.complete()).subscribe();
    }
}
