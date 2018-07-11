package uk.co.aquaq.kdb.request;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
public class FunctionRequest {

    private String function_name;
    private Map<String, String> arguments;
}
