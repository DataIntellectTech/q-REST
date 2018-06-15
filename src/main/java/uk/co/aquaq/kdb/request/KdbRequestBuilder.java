package uk.co.aquaq.kdb.request;

import kx.c;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.aquaq.kdb.security.BasicCredentials;

import java.util.Iterator;
import java.util.Map;
@Component
public class KdbRequestBuilder {

    private KdbRequestBuilder (){}
    private static String gatewayFunction;

    @Value("${gateway.function}")
    public void setGatewayFunction(String gatewayFunctionProp) {
        gatewayFunction = gatewayFunctionProp;
    }

    public static KdbRequest buildKdbRequest(FunctionRequest functionRequest, BasicCredentials basicCredentials){
        KdbRequest kdbRequest = new KdbRequest();
        kdbRequest.setArguments(buildArgString(functionRequest.getArguments()));
        kdbRequest.setFunctionName(functionRequest.getFunction_name());
        kdbRequest.setCredentialDictionary(new c.Dict(new String[]{"user"},new String[]{basicCredentials.getUsername()}));
        kdbRequest.setGatewayFunction(gatewayFunction);

        return kdbRequest;
    }


    private static String buildArgString(Map<String, String> argumentsMap){
        StringBuilder stringBuilder= new StringBuilder("{");
        Iterator<Map.Entry<String, String>> iterator = argumentsMap.entrySet().iterator();
        Map.Entry<String, String> entry;
        while(argumentsMap.size()!=0 && null != (entry = iterator.next())){
            stringBuilder.append("\"");
            stringBuilder.append(entry.getKey());
            stringBuilder.append("\":\"");
            stringBuilder.append(entry.getValue());
            if(iterator.hasNext()){
                stringBuilder.append("\",\"");
            }
            else {
                stringBuilder.append("\"");
                break;
            }
        }
        stringBuilder.append("\"}");

        return stringBuilder.toString();
    }
}
