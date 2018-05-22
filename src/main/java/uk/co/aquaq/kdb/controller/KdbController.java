package uk.co.aquaq.kdb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.*;
import uk.co.aquaq.kdb.request.QueryRequest;
import uk.co.aquaq.kdb.request.FunctionRequest;
import uk.co.aquaq.kdb.security.BasicCredentials;
import uk.co.aquaq.kdb.service.KdbService;
import java.util.List;
import java.util.Map;


@RestController
public class KdbController {
    @Autowired
    KdbService kdbService;
    @Value("${freeform.query.mode.enabled}")
    boolean freeFormQueryEnabled;

    @CrossOrigin
    @RequestMapping(value = "/executeFunction", method = RequestMethod.POST)
    public Object functionalRead(@RequestBody FunctionRequest functionRequest, @RequestHeader("Authorization") String authString) {
        BasicCredentials authDetails = BasicCredentials.getInstance(authString);
        return kdbService.executeFunction(functionRequest, authDetails);
    }

    @CrossOrigin
    @RequestMapping(value = "/executeQuery", method = RequestMethod.POST)
    public List<Map<String, String>> executeQuery(@RequestBody QueryRequest queryRequest){
        if (freeFormQueryEnabled) {
            return kdbService.executeQuery(queryRequest);

        } else {
            throw new InvalidRequestException("Invalid Request Free form queries not enabled.");
        }
    }
}
