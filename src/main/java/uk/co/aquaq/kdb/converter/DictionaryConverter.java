package uk.co.aquaq.kdb.converter;

import kx.c;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryConverter {

    public List<Map<String, Object>> formatDictionary(c.Dict result) throws UnsupportedEncodingException {
        List<Map<String, Object>> results= new ArrayList<>();
        String[] keys=(String[])result.x;
        Object[] values=(Object[])result.y;
        for(int count=0; count<keys.length; count++){
            createResultsMap(results, keys[count], values[count]);
        }
        return results;
    }

    private void createResultsMap(List<Map<String, Object>> results, String key, Object resultValue) throws UnsupportedEncodingException {
        Map<String, Object> resultsMap= new HashMap<>();
        if(isFlippableDictionary(resultValue) ||resultValue instanceof c.Flip ) {
            c.Flip flip = c.td(resultValue);
            List<Map<String, Object>> flipResults = convertFlip(flip);
            formatFlipResultsToMap(results, flipResults);
        }
        else {
            resultsMap.put(key, resultValue);
            results.add(resultsMap);
        }
    }

    private List<Map<String, Object>> convertFlip(c.Flip functionResult) {
        FlipConverter flipConverter = new FlipConverter();
        return flipConverter.convertFlipToRecordList(functionResult);
   }

    public List<Map<String, Object>>  formatFlipResultsToMap(List<Map<String, Object>> results, List<Map<String, Object>> flipResults) {
        Map<String, Object> resultsMap;
        for(Map<String, Object> flipMap : flipResults) {
            resultsMap= new HashMap<>();

            for (String key : flipMap.keySet()) {
                resultsMap.put(key, flipMap.get(key));
            }
            results.add(resultsMap);
        }
        return results;
    }

    private boolean isFlippableDictionary(Object valueResult) {
        return valueResult instanceof c.Dict && (((c.Dict)valueResult).x instanceof c.Flip) && (((c.Dict)valueResult).y instanceof c.Flip );
    }
}


