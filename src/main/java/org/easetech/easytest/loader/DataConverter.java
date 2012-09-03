package org.easetech.easytest.loader;

import java.util.Collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataConverter {
    
    public static Map<String , List<Map<String , Object>>> convert(Map<String , List<Map<String , Object>>> from){
        Map<String , List<Map<String , Object>>> result = new HashMap<String, List<Map<String,Object>>>();
        for(String method : from.keySet()){
            List<Map<String , Object>> value = from.get(method);
            for(Map<String , Object> singleTestMethod : value){
                result.put(method.concat(singleTestMethod.toString()), Collections.singletonList(singleTestMethod));
            }
            
        }
        return result;
    }

}
