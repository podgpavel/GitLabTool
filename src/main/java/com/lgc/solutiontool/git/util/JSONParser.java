package com.lgc.solutiontool.git.util;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class JSONParser {

    private static final Gson _gson = new Gson();
    private static final Type _mapType = new TypeToken<Map<String, Object>>() {}.getType();

    /**
     * Parses from json to map
     *
     * @param json data
     * @return Map<String, Object> or null, if json equals null
     */
    public static Map<String, Object> parseToMap(String json) {
        if (json != null) {
            try {
                return _gson.fromJson(json, _mapType);
            } catch (JsonSyntaxException ex) {
                System.err.println("!ERROR: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Parses from map to json
     *
     * @param data map with data
     * @return json or null, if map equals null
     */
    public static String parseToJson(Map<String, Object> data) {
        if (data != null) {
            return _gson.toJson(data);
        }
        return null;
    }

    /**
     * Parses from object to json
     *
     * @param obj object that will be parsed to json
     * @return json or null if invalid data
     */
    public static String parseToJson(Object obj) {
        if (obj != null) {
            return _gson.toJson(obj);
        }
        return null;
    }

    /**
     * Parses from json to object of T class
     *
     * @param json string of json with data object
     * @param classObject type object
     *
     * @return T object or null, if transferred incorrect data
     */
    public static <T> T parseToObject(Object json, Class<T> classObject) {
        if (classObject != null || json != null) {
            try {
                return _gson.fromJson((String) json, classObject);
            } catch (JsonSyntaxException ex) {
                System.err.println("!ERROR: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Parses from json to collection of object's T class
     *
     * @param json string of json with data objects
     * @param typeClass type of objects collection
     *
     * @return collection of object's T class or null, if transferred incorrect data
     */
    public static <T> Collection<T> parseToCollectionObjects(Object json, Type typeClass) {
        if (typeClass != null || json != null) {
            try {
                return _gson.fromJson((String) json, typeClass);
            } catch (JsonSyntaxException ex) {
                System.err.println("!ERROR: " + ex.getMessage());
            }
        }
        return null;
    }
}
