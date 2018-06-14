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

    public List<Map<String, Object>>  executeFunction(FunctionRequest functionRequest, BasicCredentials credentialValues){
        String timestamp=Instant.now().toString();
        KdbRequest kdbRequest= KdbRequestBuilder.buildKdbRequest(functionRequest,credentialValues);
        try {
            return formatDeferredSyncResult(timestamp, kdbConnector.executeDeferredSyncFunction(kdbRequest));
        } catch (Exception exception) {
            logger.warn( exception.getMessage());
            generateFailureMessage(functionRequest.getFunction_name(),timestamp,exception.getMessage());
        }
        return null;
    }

    public Object executeQuery(QueryRequest queryRequest, BasicCredentials credentialValues){
        String timestamp=Instant.now().toString();
        try {
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


    private List<Map<String, Object>> formatDeferredSyncResult(String timestamp, Object result) throws UnsupportedEncodingException {
        ResultFormatter resultFormatter=new ResultFormatter();
        List<Map<String, Object>> results = resultFormatter.formatResult(result);
        addSuccessResponse(results, timestamp);
        return results;
    }

    private void create(String jsonString){
        try {
            kdbConnector.executeAsyncQuery(jsonString);
        } catch (Exception exception) {
            logger.warn(exception.getMessage());
        }
    }

    private List<Map<String, Object>> generateFailureMessage(String jsonString, String startTime, String exceptionMessage) {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> resultsMap= new HashMap<>();
        resultsMap.put("FailureMessage", "Failure in processing the query : "+jsonString+". Error:"+exceptionMessage);
        resultsMap.put("Success", false);
        resultsMap.put("RequestTime",startTime);
        resultsMap.put("ResponseTime",Instant.now().toString());
        results.add(resultsMap);

        return results;
    }

    private void addSuccessResponse(List<Map<String, Object>> results,String startTime ) {
        HashMap<String, Object> responseMap=new HashMap<>();
        updateStatus(results, responseMap);
        responseMap.put("RequestTime",startTime);
        responseMap.put("ResponseTime",Instant.now().toString());
        embedResults(results, responseMap);
        results.add(0,responseMap);

    }

    private void embedResults(List<Map<String, Object>> results, HashMap<String, Object> responseMap) {
        List<Map<String, Object>> completeResults = new ArrayList<>(results);
        Object result=completeResults.get(0).get("result");
        results.clear();
        if(result!=null){
            responseMap.put("Results",result);
        }
        else {
            responseMap.put("Results", completeResults);
        }
    }

    private void updateStatus(List<Map<String, Object>> results, HashMap<String, Object> responseMap) {
        if(null !=results.get(0).get("status")) {
            responseMap.put("Success", results.get(0).get("status"));
            results.remove(0);
        }
        else{
            responseMap.put("Success", "true");
        }
    }
}