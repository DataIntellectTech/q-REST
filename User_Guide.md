# q-REST User Guide

## Overview

q-REST is a java service that can connect to an instance of KDB and pass queries or functions with arguments. The q-REST service offers the option make asynchronous or sunchronous calls if using the query or sychronous if using a query. The call for either synchronous call is actually a deferred synchronous whereby we use an asynchronous call to send the query followed by a synchronous call to collect the response. The response has a predefined format which is shown in the `Response` section at the bottom of the document.


## Defining the KDB Function
On the kdb instance, the user will have to define a function to call. The function could be for any purpose within kdb but must accept an object , which a two element list where element 1 is the function name and element 2 is the arguments for the function. This object is sent along with the `gateway.function` and the `username` in the c.jar c.ks call.


By default the q-REST service has a property called  `gateway.function` in the `application.properties` file. This property sets the wrapping function for the function on the KDB instance.

This value is defaulted to: 
    
    gateway.function={[request;properties] @[value;`.aqrest.execute;{[e;request;properties] @[neg .z.w;`status`result!@[{(1b;value x)};request;{(0b;"error: ",x)}]]}] . (request;properties)}

This gateway function will let the KDB instance know that the result of the function/query sent should be returned in a  dictionary containing two objects, a boolean to denote success or failure and results which could either be an object with the result query or an error. This object is then formatted to the response returned by the java.
 


## Function endpoint 

To call a KDB+ function using the q-REST interface the user will have a to pass a Json object which will comprise of two parts- `function_name` and `arguments`. The `function_name` is the name of the function the user wishes the rest api to hit on the KDB+ instance. The `arguments` are the applicable arguments passed in by the user, the arguments list needs to be passed in even if the procedure being called does not accept arguments.

#### Function Request with arguments

    {
        "function_name" : "hello-world",
        "arguments" : {
            "startdate" : "2015.01.07",
            "enddate" : "2015.01.08"
         }
    }
#### Function Request with no arguments

    {
        "function_name" : "hello-world",
        "arguments" :   {
                }
    }

## Query endpoint
To call a KDB+ query using the q-REST interface the user will have a to pass a Json object which will comprise of three parts- `type`, `response` and `query`. The `type` referes to whether the user wishes for the call to be sychronous or asynchronous, `response` refers to whether the user expects a response to be returned and `query` is the query the user wishes to run on the KDB+ instance.

#### Query Request
   
    {
	    "query" : "([]a:enlist \"hello world\")" ,    
	    "type" : "sync",
        "response" : true
    }


## Response returned to user 

For any query/function call requiring a response to be returned to the user, the java code calls a deffered sync (first an async call is made with the request and then a sync call is made to collect the response). Regardless of whether the call is successful or unsuccessful the response should have 4 parts :`success, responseTime, requestTime and results` (as is common with json do not rely on the order).

  The call are by default wrapped in a  kdb function which will return a status denoting whether or not the call was successful as well as a result object. For successful calls the object will contain the data the user requested. This is returned in a dictionary and the java parse it back to a more readable output. 
    
    [
        {
            "requestTime": "2018-06-15T10:41:50.136Z",
            "result": [
                {
                    "a": "hello world"
                }
            ],
            "success": true,
            "responseTime": "2018-06-15T10:41:50.154Z"
        }
    ]

 If the call is for any reason unsuccessful the `success` property in the response will be set to false and the error will be provided. This is applicable for java and kdb errors.

    [
        {
            "requestTime": "2018-06-15T11:10:36.134Z",
            "result": "error: illegal char y at 16",
            "success": false,
            "responseTime": "2018-06-15T11:10:36.149Z"
        }
    ]

If a user fails to provide the correct parameters to the q-REST service an error will be thrown:

#### Invalid Function Request
    { 
        "arguments":{ 
            "xarg":"7.3", 
            "yarg":"8.7" 
        } 
    }


#### Function Validation Failure Response
    [
        {
            "result": "Failure in processing the query : null. Error:Function request requires a function_name(String) and arguments(key pair values in an object<String,String>) in request",
            "requestTime": "2018-06-15T11:25:16.863Z",
            "success": false,
            "responseTime": "2018-06-15T11:25:16.864Z"
        }
    ]


#### Invalid Query Request
    {
	    "query" : "([]a:enlist \"hello world\")" ,    
        "response" : true
    }

#### Query Validation Failure Response
    [
        {
            "result": "Failure in processing the query : ([]a:enlist \"hello world\"). Error:Query request requires a query (String), type(String) and response(boolean) in request",
            "requestTime": "2018-06-15T11:25:49.810Z",
            "success": false,
            "responseTime": "2018-06-15T11:25:49.810Z"
        }
    ]
 