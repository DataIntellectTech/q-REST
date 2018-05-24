package uk.co.aquaq.kdb.request;

import com.kx.c;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.aquaq.kdb.security.BasicCredentials;

import java.util.Iterator;
import java.util.Map;
@Component
public class KdbRequestBuilder {

    private KdbRequestBuilder (){}

    private static String functionTemplate;

    @Value("${function}")
    public void setDatabase(String functionTemplate) {
        this.functionTemplate = functionTemplate;
    }

    public static KdbRequest buildKdbRequest(FunctionRequest functionRequest, BasicCredentials basicCredentials){
        KdbRequest kdbRequest = new KdbRequest();
        kdbRequest.setArguments(buildArgString(functionRequest.getArguments()));
        kdbRequest.setFunctionName(functionRequest.getFunction_name());
        kdbRequest.setCredentialDictionary(new c.Dict(new String[]{"user"},new String[]{basicCredentials.getUsername()}));
        kdbRequest.setFunctionTemplate(functionTemplate);

        return kdbRequest;
    }


    private static String buildArgString(Map<String, String> argumentsMap){
        StringBuilder stringBuilder= new StringBuilder("{\"");
        Iterator<Map.Entry<String, String>> iterator = argumentsMap.entrySet().iterator();
        Map.Entry<String, String> entry;
        while(null != (entry = iterator.next())){
            stringBuilder.append(entry.getKey());
            stringBuilder.append("\":");
            stringBuilder.append(entry.getValue());
            if(iterator.hasNext()){
                stringBuilder.append(",\"");
            }
            else break;
        }
        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
