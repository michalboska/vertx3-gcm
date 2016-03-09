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
 
In order to start the service, a [configuration object](./src/main/asciidoc/dataobjects.adoc#gcmserviceconfig) with some basic settings must be provided. 
The <code>apiKey</code> field is always mandatory, the <code>address</code> field is only mandatory when deploying the service as a separate verticle.

### (alternative 1) Using the service locally ###

To create and use the service locally, you have to create an instance of the service and then invoke methods directly on that instance.
Before being able to send notifications, you have to call the <code>startLocally</code> method.

<pre>
<code>
GcmService service = GcmService.

</code>
</pre>

## Calling the service ##

The service is made to be compatible with [vert.x service proxy](http://vertx.io/docs/vertx-service-proxy/java/) way of calling the service. 
This means that you can use the generated stubs, namely the <code>GcmServiceVertxEBProxy</code> class when using the standalone verticle mode. This is, in fact, the recommended way when deploying the service as a separate verticle.
If you still want to send the eventbus messages manually, see [this section in vert.x documentation](http://vertx.io/docs/vertx-service-proxy/java/#_convention_for_invoking_services_over_the_event_bus_without_proxies)

When using the service locally, you can directly invoke the service object's methods or optionally use the <code>GcmService</code> class in the <code>gcm.rxjava</code> package if you want to use the Rx way instead of handler callbacks.  


vertx-gcm mod configuration can be used to manage GCM default settings, this to decouple the code from  any future changes in default settings done by Google. All of configuration parameters, but "address", are optional. 
<pre>
<code>
{
    "address" : &lt;address&gt;
    "gcm_registration_ids_limit ": &lt;gcm_registration_ids_limit&gt;,
    "gcm_max_seconds_to_leave": &lt;gcm_max_seconds_to_leave&gt;,
    "gcm_backoff_retries": &lt;gcm_backoff_retries&gt;,
    "gcm_url": &lt;gcm_url&gt;
}
</code>
</pre>

Let's take a look at each field in turn.


- `address` The main address for the vert.x busmod.

- `gcm_registration_ids_limit` vertx-gcm incorporates GCM multicast messaging architecture, meaning that a notification message is sent to multiple devices in one request. The number of devices that can be targeted per request is limited. Currently defaults to 1000.

- `gcm_max_seconds_to_leave` How long (in seconds) the message should be kept on GCM storage if the device is offline. Currently defaults to 2419200 (four weeks).

- `gcm_backoff_retries` GCM specs require 3-rd party servers to use exponential back-off algorithm while attempting to re-submit notifications following some predefined errors. The max number of retries currently defaults to 5.
gcm_url: the URL of GCM gateway. Currently defaults to https://android.googleapis.com/gcm/send 


## Sending notifications ##

The traffic between vertx-gcm and GCM gateway is done using JSON. 

**Examples**

Request to GCM

<pre>
<code>
{
  "api_key": "AxDcG345Fxcv5"
	"notification":	{ 
	  "collapse_key": "score_update",
	  "time_to_live": 108,
	  "delay_while_idle": true,
	  "dry_run": false,
 	  "data": {
	    	"score": "4x8",
	    	"time": "15:16.2342"
	  },
	  "registration_ids":["4", "8", "15", "16", "23", "42"]
	}
}
</code>
</pre>

Let's take a look at each field in turn


- `api_key` Project specific key provided by Google

- `registration_ids`  A string array with the list of devices (registration IDs) receiving the message. It must contain at least 1 and at most 1000 registration IDs. Mandatory.

- `collapse_key`	A string (such as "Updates Available") that is used to collapse a group of like messages when the device is offline, so that only the last message gets sent to the client. This is intended to avoid sending too many messages to the phone when it comes back online. Note that since there is no guarantee of the order in which messages get sent, the "last" message may not actually be the last message sent by the application server. Optional.

- `data`	A JSON object whose fields represents the key-value pairs of the message's payload data. If present, the payload data it will be included in the Intent as application data, with the key being the extra's name. For instance, `"data":{"score":"3x1"}` would result in an intent extra named score whose value is the string 3x1. There is no limit on the number of key/value pairs, though there is a limit on the total size of the message (4kb). The values could be any JSON object, but we recommend using strings, since the values will be converted to strings in the GCM server anyway. If you want to include objects or other non-string data types (such as integers or booleans), you have to do the conversion to string yourself. Also note that the key cannot be a reserved word (from or any word starting with google.). To complicate things slightly, there are some reserved words (such as collapse_key) that are technically allowed in payload data. However, if the request also contains the word, the value in the request will overwrite the value in the payload data. Hence using words that are defined as field names in this table is not recommended, even in cases where they are technically allowed. Optional.

- `delay_while_idle`	If included, indicates that the message should not be sent immediately if the device is idle. The server will wait for the device to become active, and then only the last message for each collapse_key value will be sent. Optional. The default value is false, and must be a JSON boolean.

- `time_to_live`	How long (in seconds) the message should be kept on GCM storage if the device is offline. Optional (default time-to-live is 4 weeks, and must be set as a JSON number).

- `restricted_package_name`	A string containing the package name of your application. When set, messages will only be sent to registration IDs that match the package name. Optional. 

- `dry_run`	If included, allows developers to test their request without actually sending a message. Optional. The default value is false, and must be a JSON boolean. 


Response from GCM

<pre><code>
{
 	"multicast_id": 216,
	  "success": 3,
	  "failure": 3,
	  "canonical_ids": 1,
	  "results": [
	    	{ "message_id": "1:0408" },
		    { "error": "Unavailable" },
		    { "error": "InvalidRegistration" },
		    { "message_id": "1:1516" },
		    { "message_id": "1:2342", "registration_id": "32" },
		    { "error": "NotRegistered"}
	  ]
}
</code></pre>

Fields description


- `multicast_id`	Unique ID (number) identifying the multicast message.

- `success`	Number of messages that were processed without an error.

- `failure`	Number of messages that could not be processed.
canonical_ids	Number of results that contain a canonical registration ID. 
results	Array of objects representing the status of the messages processed. The objects are listed in the same order as the request (i.e., for each registration ID in the request, its result is listed in the same index in the response) and they can have these fields:
	
	- `message_id` String representing the message when it was successfully processed.

	- `registration_id` If set, means that GCM processed the message but it has another canonical registration ID for that device, so sender should replace the IDs on future requests (otherwise they might be rejected). This field is never set if there is an error in the request.

	- `error` String describing an error that occurred while processing the message for that recipient. The possible values are the same as documented in the above table, plus "Unavailable" (meaning GCM servers were busy and could not process the message for that particular recipient after all retry attempts defined in ).



More information and examples, together with GCM advanced tasks, can be found at [here](http://developer.android.com/guide/google/gcm/gcm.html).
