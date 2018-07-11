package uk.co.aquaq.kdb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.aquaq.kdb.connection.KdbConnectionWrapper;
import uk.co.aquaq.kdb.converter.ResultFormatter;
import uk.co.aquaq.kdb.request.QueryRequest;
import uk.co.aquaq.kdb.request.FunctionRequest;
import uk.co.aquaq.kdb.request.KdbRequest;
import uk.co.aquaq.kdb.request.KdbRequestBuilder;
import uk.co.aquaq.kdb.security.BasicCredentials;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.*;


@Service
public class KdbService {


    @Autowired
    private KdbConnectionWrapper kdbConnector;
    private static final Logger logger = LoggerFactory.getLogger(KdbConnectionWrapper.class);

    public Map<String, Object>  executeFunction(FunctionRequest functionRequest, BasicCredentials credentialValues){
        String timestamp=Instant.now().toString();
        try {
            validateFunctionRequest(functionRequest);
            KdbRequest kdbRequest= KdbRequestBuilder.buildKdbRequest(functionRequest,credentialValues);
            return formatDeferredSyncResult(timestamp, kdbConnector.executeDeferredSyncFunction(kdbRequest));
        } catch (Exception exception) {
            logger.warn( exception.getMessage());
             return generateFailureMessage(functionRequest.getFunction_name(),timestamp,exception.getMessage());
        }
    }

    public Object executeQuery(QueryRequest queryRequest, BasicCredentials credentialValues){
        String timestamp=Instant.now().toString();
        try {
            validateQueryRequest(queryRequest);
            if (queryRequest.getType().equals("sync") && queryRequest.getResponse().equals("true")) {

                return formatDeferredSyncResult(timestamp, kdbConnector.executeDeferredSyncQuery(queryRequest, credentialValues));
            } else if (queryRequest.getType().equals("async")) {
                create(queryRequest.getQuery());
            }
        }
        catch(Exception e){
            logger.warn(e.getMessage());
            return generateFailureMessage(queryRequest.getQuery(),timestamp, e.getMessage());
        }

        return new ArrayList<>();
    }

    private void validateFunctionRequest(FunctionRequest functionRequest) throws Exception {
        if (null==functionRequest.getFunction_name()|| null==functionRequest.getArguments()){
            throw new Exception("Function request requires a function_name(String) and arguments(key pair values in an object<String,String>) in request");
        }
    }

    private void validateQueryRequest(QueryRequest queryRequest) throws Exception {
        if ((null == queryRequest.getQuery()) || (null == queryRequest.getType())||(null==queryRequest.getResponse())){
            throw new Exception("Query request requires a query (String), type(String) and response(boolean) in request");
        }
    }

    private Map<String, Object> formatDeferredSyncResult(String timestamp, Object result) throws UnsupportedEncodingException {
        ResultFormatter resultFormatter=new ResultFormatter();
        List<Map<String, Object>> results = resultFormatter.formatResult(result);
        Map<String, Object> response= buildResponseMap(results, timestamp);
        return response;
    }

    private void create(String jsonString){
        try {
            kdbConnector.executeAsyncQuery(jsonString);
        } catch (Exception exception) {
            logger.warn(exception.getMessage());
        }
    }

    private Map<String, Object> generateFailureMessage(String jsonString, String startTime, String exceptionMessage) {
        Map<String, Object> resultsMap= new HashMap<>();
        resultsMap.put("result", "Failure in processing the query : "+jsonString+". Error:"+exceptionMessage);
        resultsMap.put("success", false);
        resultsMap.put("requestTime",startTime);
        resultsMap.put("responseTime",Instant.now().toString());

        return resultsMap;
    }

    private Map<String, Object> buildResponseMap(List<Map<String, Object>> results, String startTime ) {
        HashMap<String, Object> responseMap=new HashMap<>();
        updateStatus(results, responseMap);
        responseMap.put("requestTime",startTime);
        responseMap.put("responseTime",Instant.now().toString());
        embedResults(results, responseMap);
        return responseMap;
    }

    private void embedResults(List<Map<String, Object>> results, HashMap<String, Object> responseMap) {
        List<Map<String, Object>> completeResults = new ArrayList<>(results);
        Object result=completeResults.get(0).get("result");
        results.clear();
        if(result!=null){
            responseMap.put("result",result);
        }
        else {
            responseMap.put("result", completeResults);
        }
    }

    private void updateStatus(List<Map<String, Object>> results, HashMap<String, Object> responseMap) {
        if(null !=results.get(0).get("status")) {
            responseMap.put("success", results.get(0).get("status"));
            results.remove(0);
        }
        else{
            responseMap.put("success", "true");
        }
    }
}