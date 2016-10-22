package com.di7ak.spaces.api;

import org.json.JSONException;
import org.json.JSONObject;

public class UserData {
    public String id;
    public String name;
    public long captchaTime;
    
    public static UserData fromJson(JSONObject json) throws SpacesException {
        UserData result = new UserData();
        try {
            if(json.has("id")) result.id = json.getString("id");
            if(json.has("name")) result.name = json.getString("name");
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return result;
    }
}
