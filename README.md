#kdb-rest-service

This is an open source rest service written in java which can be used to connect to an instance of kdb using json. The rest service can provide a single query to run or call a function predefined on the instance of kdb.

##Pre-requisite Configuration 
#####Properties
There is an application.properties file in the resources folder, connect the rest service and the instance of kdb by updating the properties below:

kdb.host=localhost

kdb.port=6007

kdb.username=admin

kdb.password=admin

server.port=8080


#####EndPoints
The kdb-rest-service provides two endpoints:executeFunction and executeQuery. 

The executeFunction provides a means to call a predefined function and pass parameters to the kdb instance. 
The format of the request:

e.g.
{
"function_name" : "plus",
"arguments" : {
              "xarg" : "96.3",
              "yarg" : "9.7"
             }
}

The executeQuery provides a means to provide a query to the kdb instance, by default this endpoint is disabled using the property freeform.query.mode.enabled, to enable change the value to true. 
The format of the request:

e.g.
{
"type" : "sync",
"query" : "select from table",
"response" : true
}

#####Certificates
The requests are sent in HTTPS format and to provide this the project has a self-signed certifiate embedded within. It is strongly recommended that you add your own certificate. Updating the certificate will require and update to the following properties in application.properties:

security.require-ssl=true

server.ssl.key-store-type=PKCS12

server.ssl.key-store=classpath:keystore.p12

server.ssl.key-store-password=aquaq2018

server.ssl.key-alias=tomcat

#####Authentication
The rest service uses basic authentication and is using a single username and password which are configured in the applicationm.properties file and provided within the header of the request; again it is strongly recommeded to invoke your own security if you use the project.

basic.authentication.user=user
basic.authentication.password=pass

##Deploying 

There is a DockerFile within the project for deploying the project on docker.

Alternatively it can be run locally by providing the appropriate build configuration via command line or ide. 

##Built with 
Maven

##Licensing
Please see license file.

##Contributing 
The branch is currently locked down and will require a pull request reviewed by a member of the aquaq team before any changes can be committed.