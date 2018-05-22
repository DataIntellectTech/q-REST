package uk.co.aquaq.kdb.security;

import lombok.Data;
import org.apache.tomcat.util.codec.binary.Base64;

import java.nio.charset.Charset;

@Data
public class BasicCredentials {

    String username;
    String password;

    public BasicCredentials (String authString) {
        String base64Credentials = authString.substring("Basic".length()).trim();
        String credentialsValue = new String(Base64.decodeBase64(base64Credentials),
                Charset.forName("UTF-8"));
        String[] credentials= credentialsValue.split(":",2);
        this.username = credentials[0];
        this.password = credentials[1];
    }
}
