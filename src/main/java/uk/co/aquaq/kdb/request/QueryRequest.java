package uk.co.aquaq.kdb.request;

import lombok.Data;

@Data
public class QueryRequest {
    
    private String query;
    private String type;
    private String response;
}


