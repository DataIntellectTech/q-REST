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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class KdbService {

    @Autowired
    private KdbConnectionWrapper kdbConnector;
    private static final Logger logger = LoggerFactory.getLogger(KdbConnectionWrapper.class);

    public List<Map<String, Object>>  executeFunction(FunctionRequest functionRequest, BasicCredentials credentialValues){
        String timestamp=Instant.now().toString();
        KdbRequest kdbRequest= KdbRequestBuilder.buildKdbRequest(functionRequest,credentialValues);
        try {
            Object functionResult= kdbConnector.executeDeferredSyncFunction(kdbRequest);
            List<Map<String, Object>>  results=formatFunctionResult(functionResult, timestamp);
            addSuccessResponse(results,timestamp);
            return results;
        } catch (Exception exception) {
            logger.warn( exception.getMessage());
            generateFailureMessage(functionRequest.getFunction_name(),timestamp,exception.getMessage());
        }
        return null;
    }

    private List<Map<String, Object>> formatFunctionResult(Object functionResult, String timeStamp) {
        List<Map<String, Object>> results=null;
        if(functionResult instanceof c.Flip){
            results=handleFlipFormat((c.Flip) functionResult);
        }
        else if(functionResult instanceof c.Dict &&(((c.Dict) functionResult).x instanceof String[])) {
            results = handleDictionaryResult((c.Dict) functionResult, timeStamp);
        }
        else{
            results=handleResult(functionResult);
        }
        return results;
    }

    private List<Map<String, Object>>  handleFlipFormat(c.Flip functionResult) {
        c.Flip flip= functionResult;
        FlipConverter flipConverter = new FlipConverter();
        return flipConverter.convertFlipToRecordList(flip);
    }

    public Object executeQuery(QueryRequest queryRequest, BasicCredentials credentialValues){
        String timestamp=Instant.now().toString();
        try {
            if (queryRequest.getType().equals("sync") && queryRequest.getResponse().equals("true")) {
                Object queryResult = kdbConnector.executeDeferredSyncQuery(queryRequest, credentialValues);
                List<Map<String, Object>> results=formatFunctionResult(queryResult, timestamp);
                addSuccessResponse(results,timestamp);

                return results;
            } else if (queryRequest.getType().equals("async")) {
                create(queryRequest.getQuery());
            }
        }
        catch(Exception e){
            logger.warn(e.getMessage());
            generateFailureMessage(queryRequest.getQuery(),timestamp, e.getMessage());
        }

        return new ArrayList<>();
    }

    private void create(String jsonString){
        try {
            kdbConnector.executeAsyncQuery(jsonString);
        } catch (Exception exception) {
            logger.warn(exception.getMessage());
        }
    }



    private List<Map<String,Object>> handleDictionaryResult(c.Dict result, String timestamp) {
        List<Map<String, Object>> results= new ArrayList<>();
        Map<String, Object> resultsMap= new HashMap();
        String[] keys=(String[])result.x;
        Object[] values=(Object[])result.y;
        for(int count=0; count<keys.length; count++){
            resultsMap.put(keys[count], values[count]);
        }
        results.add(resultsMap);
        return results;
    }

    private List<Map<String, Object>> handleResult(Object result) {
        List<Map<String, Object>> results= new ArrayList<>();
        Map resultsMap= new HashMap();
        resultsMap.put("result", result);
        results.add(resultsMap);
        return results;
    }

    private List<Map<String, Object>> generateFailureMessage(String jsonString, String startTime, String exceptionMessage) {
        List<Map<String, Object>> results = new ArrayList<>();
        Map resultsMap= new HashMap();
        resultsMap.put("FailureMessage", "Failure in processing the query : "+jsonString+". Error:"+exceptionMessage);
        resultsMap.put("Success", false);
        resultsMap.put("RequestTime",startTime);
        resultsMap.put("ResponseTime",Instant.now().toString());
        results.add(resultsMap);
        return results;
    }

    private void addSuccessResponse(List<Map<String, Object>> results,String startTime ) {
        HashMap responseMap=new HashMap();
        responseMap.put("Success", results.get(0).get("status"));
        results.get(0).remove("status");
        results.add(0,responseMap);
        responseMap.put("RequestTime",startTime);
        responseMap.put("ResponseTime",Instant.now().toString());
    }
}