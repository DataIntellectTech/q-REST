package uk.co.aquaq.kdb.service;

import kx.c;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.aquaq.kdb.connection.KdbConnectionWrapper;
import uk.co.aquaq.kdb.converter.FlipConverter;
import uk.co.aquaq.kdb.request.QueryRequest;
import uk.co.aquaq.kdb.request.FunctionRequest;
import uk.co.aquaq.kdb.request.KdbRequest;
import uk.co.aquaq.kdb.request.KdbRequestBuilder;
import uk.co.aquaq.kdb.security.BasicCredentials;

import java.io.IOException;
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
            List<Map<String, Object>> results = executeDefferedSyncQuery(timestamp, kdbConnector.executeDeferredSyncFunction(kdbRequest));
            return results;
        } catch (Exception exception) {
            logger.warn( exception.getMessage());
            generateFailureMessage(functionRequest.getFunction_name(),timestamp,exception.getMessage());
        }
        return null;
    }

    private List<Map<String, Object>> formatResult(Object functionResult) throws UnsupportedEncodingException {
        List<Map<String, Object>> results=null;
        if(functionResult instanceof c.Flip){
            results=handleFlipFormat((c.Flip) functionResult);
        }
        else if(functionResult instanceof c.Dict &&(((c.Dict) functionResult).x instanceof String[])) {
            results = formatDictionary((c.Dict) functionResult);
        }
        else{
            results=handleResult(functionResult);
        }
        return results;
    }

    private List<Map<String, Object>>  handleFlipFormat(c.Flip functionResult) {
        FlipConverter flipConverter = new FlipConverter();
        return flipConverter.convertFlipToRecordList(functionResult);
    }

    public Object executeQuery(QueryRequest queryRequest, BasicCredentials credentialValues){
        String timestamp=Instant.now().toString();
        try {
            if (queryRequest.getType().equals("sync") && queryRequest.getResponse().equals("true")) {
                List<Map<String, Object>> results = executeDefferedSyncQuery(timestamp, kdbConnector.executeDeferredSyncQuery(queryRequest, credentialValues));

                return results;
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

    private List<Map<String, Object>> executeDefferedSyncQuery(String timestamp, Object o) throws UnsupportedEncodingException {
        Object queryResult = o;
        List<Map<String, Object>> results = formatResult(queryResult);
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

    private List<Map<String, Object>> formatDictionary(c.Dict result) throws UnsupportedEncodingException {
        List<Map<String, Object>> results= new ArrayList<>();
        String[] keys=(String[])result.x;
        Object[] values=(Object[])result.y;
        for(int count=0; count<keys.length; count++){
            Map<String, Object> resultsMap= new HashMap<>();

            Object valueResult =values[count];
            if(isFlippableDictionary(valueResult) ||valueResult instanceof c.Flip ) {
                c.Flip flip = c.td(valueResult);
                List<Map<String, Object>> flipResults = handleFlipFormat(flip);
                formatFlipResultsToMap(results, flipResults);
            }
            else {
                resultsMap.put(keys[count], valueResult);
                results.add(resultsMap);
            }
        }
        return results;
    }

    private void formatFlipResultsToMap(List<Map<String, Object>> results, List<Map<String, Object>> flipResults) {
        Map<String, Object> resultsMap;
        for(Map<String, Object> flipMap : flipResults) {
            resultsMap= new HashMap<>();

            for (String key : flipMap.keySet()) {
                resultsMap.put(key, flipMap.get(key));
            }
            results.add(resultsMap);
        }
    }

    private boolean isFlippableDictionary(Object valueResult) {
        return valueResult instanceof c.Dict && (((c.Dict)valueResult).x instanceof c.Flip) && (((c.Dict)valueResult).y instanceof c.Flip );
    }

    private List<Map<String, Object>> handleResult(Object result) {
        List<Map<String, Object>> results= new ArrayList<>();
        Map<String, Object> resultsMap= new HashMap<>();
        resultsMap.put("result", result);
        results.add(resultsMap);

        return results;
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
        responseMap.put("Success", results.get(0).get("status"));
        results.remove(0);
        responseMap.put("RequestTime",startTime);
        responseMap.put("ResponseTime",Instant.now().toString());
        results.add(0,responseMap);

    }
}