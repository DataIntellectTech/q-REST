package uk.co.aquaq.kdb.converter;

import com.kx.c;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultFormatter {


    public List<Map<String, Object>> formatResult(Object functionResult) throws UnsupportedEncodingException {
        List<Map<String, Object>> results= new ArrayList<>();
        if(functionResult instanceof c.Flip){
            results= convertFlip((c.Flip) functionResult);
        }
        else if(isDictionaryWithStringKey(functionResult)) {
            DictionaryConverter dictionaryConverter = new DictionaryConverter();
            results = dictionaryConverter.formatDictionary((c.Dict) functionResult);
        }
        else if(isFlippableDictionary(functionResult)) {
            results = flipDictionary(functionResult, results);
        }
        else{
            results=handleResult(functionResult);
        }
        return results;
    }


    private List<Map<String, Object>> flipDictionary(Object functionResult, List<Map<String, Object>> results) throws UnsupportedEncodingException {
        c.Flip flip = c.td(functionResult);
        List<Map<String, Object>> flipResults = convertFlip(flip);
        DictionaryConverter dictionaryConverter = new DictionaryConverter();
        results =dictionaryConverter.formatFlipResultsToMap(results, flipResults);
        return results;
    }

    private List<Map<String, Object>> handleResult(Object result) {
        List<Map<String, Object>> results= new ArrayList<>();
        Map<String, Object> resultsMap= new HashMap<>();
        resultsMap.put("result", result);
        results.add(resultsMap);

        return results;
    }

    private List<Map<String, Object>> convertFlip(c.Flip functionResult) {
        FlipConverter flipConverter = new FlipConverter();
        return flipConverter.convertFlipToRecordList(functionResult);
    }

    private List<Map<String, Object>> convertDictionary(c.Dict functionResult) throws UnsupportedEncodingException {
        List<Map<String, Object>> results;DictionaryConverter dictionaryConverter= new DictionaryConverter();
        results = dictionaryConverter.formatDictionary(functionResult);
        return results;
    }

    private boolean isFlippableDictionary(Object valueResult) {
        return valueResult instanceof c.Dict && (((c.Dict)valueResult).x instanceof c.Flip) && (((c.Dict)valueResult).y instanceof c.Flip );
    }

    private boolean isDictionaryWithStringKey(Object functionResult) {
        return functionResult instanceof c.Dict &&(((c.Dict) functionResult).x instanceof String[]) &&((c.Dict) functionResult).y instanceof Object[] ;
    }
}
