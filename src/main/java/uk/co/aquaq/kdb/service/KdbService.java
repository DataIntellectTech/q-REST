package uk.co.aquaq.kdb.service;

import com.kx.c;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class KdbService {

    @Autowired
    private KdbConnectionWrapper kdbConnector;
    private static final Logger logger = LoggerFactory.getLogger(KdbConnectionWrapper.class);

    public Object executeFunction(FunctionRequest functionRequest, BasicCredentials credentialValues){
        KdbRequest kdbRequest= KdbRequestBuilder.buildKdbRequest(functionRequest,credentialValues);
        try {
            Object functionResult= kdbConnector.executeDeferredSyncFunction(kdbRequest);
            return formatFunctionResult(functionResult);
        } catch (Exception exception) {
            logger.warn( exception.getMessage());
        }
        return null;
    }

    private Object formatFunctionResult(Object functionResult) {
        if(functionResult instanceof c.Flip){
            c.Flip flip= (c.Flip)functionResult;
            FlipConverter flipConverter = new FlipConverter();
            functionResult= flipConverter.convertFlipToRecordList(flip);
        }

        return functionResult;
    }

    public List<Map<String, String>> executeQuery(QueryRequest queryRequest){
        if (queryRequest.getType().equals("sync") && queryRequest.getResponse().equals("true")) {
            return syncRead(queryRequest.getQuery());
        } else if (queryRequest.getType().equals("async")) {
            create(queryRequest.getQuery());
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

    private List<Map<String,String>> syncRead(String jsonString ){
        List<Map<String,String>> results = null;
        try{
           c.Flip flip = (c.Flip)kdbConnector.syncQuery(jsonString);
            FlipConverter flipConverter = new FlipConverter();
            results= flipConverter.convertFlipToRecordList(flip);
        }
        catch (Exception exception) {
            logger.warn(exception.getMessage());
        }

        return results;
    }
}