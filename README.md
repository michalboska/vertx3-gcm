# Vert.x 3 Google Cloud Messaging for Android service #

This service provides a Google Cloud Messaging for Vert.x 3. 
Google Cloud Messaging for Android (GCM) is a service that delivers push data from your application's backend servers to Android devices. These push notification can then be processed by applications installed on Android devices. 

The notification can be a lightweight message telling the Android application that there is new data to be fetched from the server (for instance, a movie uploaded by a friend),
or it could itself be a complete message containing up to 4kb of payload data (so apps like instant messaging can consume the message directly). 
The GCM service handles all aspects of queuing of messages and delivery to the target Android application running on the target device.

## Dependencies ##

This service has no dependencies on any external GCM libraries (not on the official ones from Google either). It only uses core vert.x API, with its Rx-ified version, therefore it only depends on vert.x core and Rx libraries.

You will still need to enable GCM service in Google's API console. For more info please follow the instructions [here](http://developer.android.com/guide/google/gcm/index.html).

## Configuration and setup ##

The service is designed to either run locally in some external Verticle instance, from with it can be used with standard invocations (method calls directly from the containing Verticle code),
or it can be deployed as a separate verticle.

### Configuration object ###
 
In order to start the service, a [configuration object](src/main/asciidoc/dataobjects.adoc#gcmserviceconfig) with some basic settings must be provided. 
The <code>apiKey</code> field is always mandatory, the <code>address</code> field is only required when deploying the service as a separate verticle. 
If you don't fill in any address, the fully qualified class name of the <code>GcmService</code> interface will be used.

### (alternative 1) Using the service locally ###

To create and use the service locally, you have to create an instance of the service and then invoke methods directly on that instance. 
You can also optionally use the <code>GcmService</code> class in the <code>gcm.rxjava</code> package if you want to use the Rx way instead of handler callbacks.

<pre>
<code>
GcmService service = GcmService.create(vertx, new GcmServiceConfig("my GCM API key"));
//you can now invoke methods on the service object
</code>
</pre>

### (alternative 2) Using the service remotely, as a separately deployed verticle(s) ###

The service is made to be compatible with [vert.x service proxy](http://vertx.io/docs/vertx-service-proxy/java/) way of calling services. 

This means that you can use the generated stubs when using the standalone verticle mode. This is, in fact, the recommended way when deploying the service as a separate verticle.
If you still want to send eventbus messages manually, see [this section in vert.x documentation](http://vertx.io/docs/vertx-service-proxy/java/#_convention_for_invoking_services_over_the_event_bus_without_proxies)

<pre>
<code>
GcmService serviceProxy = GcmService.createRemote(vertx, new GcmServiceConfig("my GCM API key"), ar -> {
    if (ar.succeeded()) {
        String deploymentId = ar.result(); //you can save this somewhere and use it later to undeploy the service
        //you can start using the service now
    } else {
        //implement error handling
    }
});
</code>
</pre>

## Sending notifications ##

After having acquired a service instance object in either of the two ways, you send a GCM notification by calling the <code>sendNotification</code> method.
This method takes a <code>GcmNotification</code> argument and a completion handler. For more information on what data the <code>GcmNotification</code> can contain, [see the documentation](src/main/asciidoc/dataobjects.adoc#gcmnotification).

The completion handler will complete immediately after receiving a response from the server, or, if some errors were encountered and the service is [configured to do so](src/main/asciidoc/dataobjects.adoc#gcmserviceconfig), after retrying certain number of times.

The completion handler will provide a [GcmResponse](src/main/asciidoc/dataobjects.adoc#gcmresponse) object that contains summary information as well as per-device-ID results.


## Example ##

Simple notification:
<pre>
<code>

GcmNotification notification = new GcmNotification(Arrays.asList("123", "456"));
gcmService.sendNotification(notification, ar -> {
    if (ar.succeeded()) {
        GcmResponse result = ar.result();
        Integer successCount = result.getSuccessCount();
        Integer failureCount = result.getFailureCount();
        Map<String, SingleMessageResult> deviceResults = result.getDeviceResults();
    } else {
        Throwable cause = ar.cause();
        //error handling
    }
});
</code>
</pre>

Or, using the Rx-ified version:
<pre>
<code>

com.github.michalboska.vertx3.gcm.rxjava.GcmService rxGcmService = new com.github.michalboska.vertx3.gcm.rxjava.GcmService(this.gcmService);
rxGcmService.sendNotificationObservable(notification)
        .subscribe(result -> {
            Integer successCount = result.getSuccessCount();
            Integer failureCount = result.getFailureCount();
            Map<String, SingleMessageResult> deviceResults = result.getDeviceResults();                    
        }, throwable -> {
            //error handling              
        });

</code>
</pre>

## References ##

More information and examples, together with GCM advanced tasks, [can be found here](http://developer.android.com/guide/google/gcm/gcm.html).

This service is inspired by the [original vert.x 2 mod](https://github.com/ashertarno/vertx-gcm)