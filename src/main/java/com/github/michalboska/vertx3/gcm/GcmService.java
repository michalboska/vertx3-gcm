package com.github.michalboska.vertx3.gcm;

import com.github.michalboska.vertx3.gcm.internal.GcmServiceImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;

@ProxyGen
@VertxGen
public interface GcmService {

    /**
     * Creates a local instance of the service. You can use this instance directly by calling its methods from your verticle.
     *
     * @param vertx
     * @param config
     * @return
     */
    static GcmService create(Vertx vertx, GcmServiceConfig config) {
        return new GcmServiceImpl(vertx, config).startLocally();
    }

    /**
     * Deploys one instance of the GCM service as a separate verticle. Returns a proxy object, so that you can directly invoke methods on it as if it were a local service instance.
     *
     * Note that you need to wait for the {@code handler} to complete, only then will the service be ready. Handler will contain the deployment ID, in case you need to undeploy the service later.
     * @param vertx Your vert.x instance
     * @param config
     * @param handler Handler that returns a Deployment ID. Only after this handler has completed, the service is ready for use.
     * @return Proxy object. You can call methods of this object and the messages will automatically be sent to the service over the event bus.
     */
    static GcmService createRemote(Vertx vertx, GcmServiceConfig config, Handler<AsyncResult<String>> handler) {
        return createRemoteMultipleInstances(vertx, config, 1, handler);
    }

    /**
     * Deploys any number of instances of the GCM service as separate verticles. Returns a proxy object, so that you can directly invoke methods on it as if it were a local service instance.
     *
     * Note that you need to wait for the {@code handler} to complete, only then will the service be ready. Handler will contain the deployment ID, in case you need to undeploy the service later.
     * @param vertx Your vert.x instance
     * @param config
     * @param instances A number of verticle instances to be created. If null, the current number of CPU cores will be used.
     * @param handler Handler that returns a Deployment ID. Only after this handler has completed, the service is ready for use.
     * @return Proxy object. You can call methods of this object and the messages will automatically be sent to one of the deployed service instances over the event bus.
     */
    static GcmService createRemoteMultipleInstances(Vertx vertx, GcmServiceConfig config, Integer instances, Handler<AsyncResult<String>> handler) {
        GcmServiceImpl impl = new GcmServiceImpl(vertx, config);
        if (instances == null) {
            instances = Runtime.getRuntime().availableProcessors();
        }

        vertx.deployVerticle(impl, new DeploymentOptions().setInstances(instances), handler);
        return createProxy(vertx, config.getAddress());
    }

    /**
     * Creates a remote proxy object. You can call methods of this object and the messages will automatically be sent to one of the deployed service instances over the event bus, listening on {@code address}.
     * @param vertx Your vert.x instance
     * @param address Address the messages should be sent to
     * @return
     */
    static GcmService createProxy(Vertx vertx, String address) {
        return new GcmServiceVertxEBProxy(vertx, address);
    }

    @Fluent
    GcmService sendNotification(GcmNotification notification, Handler<AsyncResult<GcmResponse>> handler);

}
