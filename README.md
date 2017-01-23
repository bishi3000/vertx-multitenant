# Vert.x Multi-tenant module

This project is intended to provide a very basic service bootstrapping for Vert.x applications which need to run in a
multi-tenanted environment.

This module provides two very basic functions:

1) A mechanism for configuring where to lookup Tenant configuration
2) The framework for resolving requests from a particular Tenant

## How it works

The module expects to see a folder/directory structure which contains an index.json file (this contains a map of all the tenants against the URLs for those tenants)

The index.json file is scanned and for every tenant name it finds, it then tried to locate a folder of that same name. This folder is then scanned and any files within it are then loaded into Vert.x's shared data.

The module also creates a hash of the objects is adds to the shared data map, so that other modules can detect changes in the hashcode and only process files upon a change.

## Including this module in your project

This module needs to be deployed programmatically via a Vert.x verticle. the deploy time configuration for the module should be passed
at this point like so:

```java

JsonObject config = container.config();

container.deployModule("com.ultratechnica.vertx~multitenant~1.1", config.getObject("MultitenantModule"), new AsyncResultHandler<String>() {
    @Override
    public void handle(AsyncResult<String> result) {

        assertTrue(result.succeeded());
        assertNotNull("deploymentID should not be null", result.result());

        if (result.failed()) {
            System.out.println("Unable to deploy module, cause: " + result.cause());
        }

        container.deployModule(System.getProperty("vertx.modulename"), config.getObject("UwSessionModule"), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {

                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());

                startTests();
            }
        });
    }
});
```

The particular configuration required for the multitenant module depends upon the data source being used

## Data sources

This module support loading tenant configuration from two sources:

1) Amazon S3
2) The Java classpath

## AWS S3 provider use

Below is an example of a typical s3 configuration

```json

{
    "MultitenantModule" : {

        "TenantVerticle" : {

            "provider" : "S3",

            "refreshInterval": 60000,

            "s3connectionDetails" : {
                "bucket" : "ut-multitenant-test",
                "folder" : "tenants"
            }
        }
    }
}
```
NOTE: the "provider" parameter is optional for the S3 configuration as S3 is the default provider


## Classpath provider use

```json
{
    "MultitenantModule" : {

        "TenantVerticle" : {

            "provider" : "ClassPath",

            "refreshInterval": 60000,

            "configDirName" : "/testTenantConfig"
        }
    }
}
```

Here the provider "ClassPath" has been specified. the ClassPath provider requires a parameter "configDirName" which
is the name of the folder in the ClassPath which contains the index.json

NOTE : the configDirName begins with a "/" this is important as omitting it will result in your configs not being loaded.


## Force Refresh

Sometimes it may be required to load (refresh) the tenant data in between the refresh interval. An example of this would be a config update via a front end interface. After the changes have been made, 
you may want to see those changes immediately after reloading the front end user interface. The following code shows how this can be done.
 
```java

vertx.eventBus().send(BusAddress.FORCE_REFRESH.toString(), new JsonObject("{}"), new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
        container.logger().info("Message Response:\n" + message.body().encodePrettily());        
    }
});
```

A few things to note. The return message will contain whatever message you send via the JsonObject. If there is already a load in progress. The request will be added to a queue which will be processed after
the current load is complete. This is done to ensure change made during a load will still be refreshed. Multiple instructions to reload the data can be stacked. Instructions to refresh the tenant data are
thread safe.



