package com.gentics.cr.http;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequest.DEFAULT_ATTRIBUTES;
import com.gentics.cr.exceptions.CRException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds all parameters which are not in the list of default parameters in the
 * {@link CRRequest} ({@link DEFAULT_ATTRIBUTES}) or in the list of default
 * URL parameters in the {@link AbstractHTTPClientRequestProcessor} ({@link DEFAULT_URL_PARAMETERS})
 */
public class HTTPClientAllParamsRequestProcessor  extends AbstractHTTPClientRequestProcessor {
    
        public HTTPClientAllParamsRequestProcessor(CRConfig config) throws CRException {
		super(config);
	}

	@Override
	protected void appendCustomGetParam(GetUrlBuilder urlBuilder, CRRequest request) {
            for(String key : request.getParameterKeys()) {
                if(!enumContains(DEFAULT_URL_PARAMETERS.class, key) && !enumContains(DEFAULT_ATTRIBUTES.class, key)) {
                    urlBuilder.appendSkipEmpty(request, key);
                }
            }
	}
        
        
        private static final Map<Class, List<String>> enumMap = new HashMap<>(); 

       /**
        * Searches an array of enum-values for a certain value. This method
        * compares the value with the return value of the enums the "toString()"
        * method.
        * 
        * @param <T> The enum type
        * @param enumClass the values array of the enum
        * @param value the value to search for
        * @return true if the value was found
        */
        public static <T extends Enum> Boolean enumContains(Class<T> enumClass, String value) {
            // return false if null or an empty array was passed
            if(enumClass == null) {
                return Boolean.FALSE;
            }
            // save a string value list for each enum to the static map
            if(enumMap.get(enumClass) == null) {
                T[] enumValues;
                try {
                    Method method = enumClass.getMethod("values");
                    enumValues = (T[]) method.invoke(null);
                } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    return Boolean.FALSE;
                }
                List<String> stringValues = new ArrayList<>();
                for(T item : enumValues) {
                    stringValues.add(item.toString());
                }
                enumMap.put(enumClass, stringValues);
            }
            return enumMap.get(enumClass).contains(value);
        }
}
