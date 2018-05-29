# kdb-rest-service

An open source REST service written in java used to connect to an instance of kdb using JSON. The REST service can provide a single query to run or call a function predefined on the instance of kdb.

## Pre-requisite Configuration 
##### Properties
There is an `application.properties` file in the resources folder, connect the rest service and the instance of kdb by updating the properties below:

    kdb.host=localhost
    kdb.port=6007
    kdb.username=admin
    kdb.password=admin
    server.port=8080


##### EndPoints
The kdb-rest-service provides two endpoints:executeFunction and executeQuery. 

The executeFunction provides a means to call a predefined function and pass parameters to the kdb instance. 
For example this is the format of a request call a function called plus which passes two arguments labelled "xarg" and "yarg" with values 96.3 and 9.7:

e.g.
    
    {
    "function_name" : "plus",
    "arguments" : 
            {
                "xarg" : "96.3",
                "yarg" : "9.7"
             }
    }

The executeQuery provides a means to provide a query to the kdb instance, by default this endpoint is disabled using the property freeform.query.mode.enabled, to enable change the value to true. 
For example this is the format of a synchronous query request where the user expects a response to be returned:

e.g.
       

    {
        "type" : "sync",   
        "query" : "select from table",
        "response" : true
    }

##### Certificates
The requests are sent in HTTPS format and to provide this the project has a self-signed certifiate embedded within. It is strongly recommended that you add your own certificate. Updating the certificate will require an update to the following properties in `application.properties`:

    security.require-ssl=true
    server.ssl.key-store-type=PKCS12
    server.ssl.key-store=classpath:keystore.p12
    server.ssl.key-store-password=aquaq2018
    server.ssl.key-alias=tomcat

##### Authentication
The rest service uses basic authentication and is using a single username and password which are configured in the `application.properties` file:

    basic.authentication.user=user
    basic.authentication.password=pass

These value are provided within the header of the request, it is strongly recommended to invoke your own security if you use the project.


## Deploying 

There is a DockerFile within the project for deploying the project on docker.

Alternatively it can be run locally by providing the appropriate build configuration via command line or IDE. 

## Contributing 
The branch is currently locked down and will require a pull request reviewed by a member of the AquaQ team before any changes can be committed.