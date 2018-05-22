package uk.co.aquaq.kdb.connection;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import com.kx.c;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.co.aquaq.kdb.request.KdbRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.UnknownHostException;

@Component
@Data
public class KdbConnectionWrapper {

    private static final Logger logger = LoggerFactory.getLogger(KdbConnectionWrapper.class);
    private c connectionToKdb;
    @Value("${kdb.host}")
    private String hostname;
    @Value("${kdb.port}")
    private Integer port;
    @Value("${kdb.username}")
    private String username;
    @Value("${kdb.password}")
    private String password;

    @PostConstruct
    public void postConstruct() {
        open();
    }

    @PreDestroy
    public void preDestroy()  throws IOException {
        close();
    }

    private void open(){
        try {
            connectionToKdb = new c(hostname, port, getCredentials());
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

    private void close() throws IOException{
        if (connectionToKdb != null) {
                connectionToKdb.close();
                connectionToKdb = null;
        } else {
            throw new KdbConnectionException("Cannot close Connection");
        }
    }

    public void executeAsyncQuery(String query) throws IOException {
        connectionToKdb.ks(query);
    }

    public Object executeDeferredSyncFunction(KdbRequest kdbRequest) throws c.KException , IOException{
        connectionToKdb.ks(kdbRequest.getFunctionTemplate(),
                new Object[]{kdbRequest.getFunctionName().toCharArray(),
                        kdbRequest.getArguments().toCharArray()},
                kdbRequest.getCredentialDictionary());

        return connectionToKdb.k();
    }

    public Object syncQuery(String query) throws IOException, c.KException {
        return c.td(connectionToKdb.k(query));
    }
}