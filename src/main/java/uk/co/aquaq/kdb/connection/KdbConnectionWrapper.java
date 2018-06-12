package uk.co.aquaq.kdb.connection;

import kx.c;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.co.aquaq.kdb.request.KdbRequest;
import uk.co.aquaq.kdb.request.QueryRequest;
import uk.co.aquaq.kdb.security.BasicCredentials;

import java.io.IOException;
import java.net.UnknownHostException;

@Component
@Data
public class KdbConnectionWrapper {

    private static final Logger logger = LoggerFactory.getLogger(KdbConnectionWrapper.class);
    @Value("${kdb.host}")
    private String hostname;
    @Value("${kdb.port}")
    private Integer port;
    @Value("${kdb.username}")
    private String username;
    @Value("${kdb.password}")
    private String password;

    private c open(){
        try {
            return new c(hostname, port, getCredentials());
        } catch (UnknownHostException unknownHostException) {
            String message = String.format("Unable to contact KDB host %s. Original message: %s", hostname, unknownHostException.getMessage());
            logger.error(message);
            throw new KdbConnectionException("Unable to contact KDB host, unknown host", unknownHostException);
        } catch (IOException iOException) {
            throw new KdbConnectionException("Unable to contact KDB host", iOException);
        } catch (c.KException kException) {
            throw new KdbConnectionException("Access to KDB denied for user", kException);
        }
    }

    private String getCredentials() {
        return (username != null ? username : "") + ':' + (password != null ? password : "");
    }

    public void executeAsyncQuery(String query) throws IOException {
        c connectionToKdb=open();
        try {
            connectionToKdb.ks(query);
        }
        finally {
            connectionToKdb.close();

        }
    }

    public Object executeDeferredSyncFunction(KdbRequest kdbRequest) throws c.KException , IOException{
        c connectionToKdb=open();
        try {
            connectionToKdb.ks(kdbRequest.getGatewayFunction(),
                    new Object[]{kdbRequest.getFunctionName().toCharArray(),
                            kdbRequest.getArguments().toCharArray()},
                    kdbRequest.getCredentialDictionary());
            return  connectionToKdb.k();
        }finally {
            connectionToKdb.close();
        }
    }

    public Object executeDeferredSyncQuery(QueryRequest queryRequest,BasicCredentials credentialValues) throws c.KException , IOException{
        c connectionToKdb=open();
        try {
            String value="value";
            connectionToKdb.ks(queryRequest.getGatewayFunction(),
                    new Object[]{value.toCharArray(),queryRequest.getQuery().toCharArray()},new c.Dict(new String[]{"user"},new String[]{credentialValues.getUsername()}));
            return  connectionToKdb.k();
        }finally {
            connectionToKdb.close();
        }
    }

}