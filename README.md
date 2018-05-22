# kdb rest service

###Pre-requisites
First you need to set your properties file with the connection details your springKdb project will connect to.

There are several properties to set. 

You can find the properties file in your project folder under resources.
select and edit with an editor of your choice the relevant application-{env}.properties file

* Set kdb.host to the IP/hostname of where the kdb host resides:
  * default value: 0.0.0.0 .
* Set kdb.port to the port number your kdb process is listening to from host:
  * default value: 0000 .
* Set kdb.username and kdb.password to the credentials your springKdb project will need to connect to the kdb process:
(if the kdb process has authentication set up)
  * No default credentials.
* Set the server.port to the port your springKdb projects API will be receiving JSON from:
  * default value: 0000.
* Set basicAuthentication.username and basicAuthentication.password to the credentials your springKdb project will
be expecting to receive from the header of your javascript applications POST request:
  * default username value: user.
  * default password value: pass.
* Set freeform.query.enabled to be set to true if you wish to be able to send free form queries:
  * default value: false.

###How is SpringKdb expecting requests from a javascript application?
The Rest API within SpringKdb is expecting a Https POST request call in the form 
https://host:port/endpointName.
with the username and password sent in the header.
SpringKdb has Basic Authentication set up so it wont allow a user to access the API without the correct credentials
(The default being "user" "pass").

If you do add your own security to the rest service please be aware you will need to update getCredentialValues in the rest controller to extract the username for the function to pass this to kdb.

###SSL Certification
SpringKdb already has its own self signed certificate but it is strongly recommended to use your own certificate and update the authentication.

###How should the JSON be structured when being sent to our endpoint?
For the function calls the user should provide the functions name and the arguments that need to be added, endpoint is executeFunction:

e.g.
{
"function" : "plus",
"arguments" : {
              "xarg" : "2",
              "yarg" : "5"
             }
}

For the freeform queries consists of three parts, type is the call async or sync, query the actual query called and response, as in do we expect a response, endpoint is executeQuery

e.g.
{
"type" : "async",
"query" : "select from table",
"response" : true
}

###Deployment
In the project there are dev and test props files which can be used as example, if you need to switch profiles change the profile in your build to "dev" or "test" or whatever new properties file you have added as long as they match the naming pattern - application-ENVNAME.properties .

There is a docker file that can be used to deploy by providing it the correct profile for running e.g.  "dev" or "test".

If deploying without a docker you will still need to provide the profile in the run configuration.