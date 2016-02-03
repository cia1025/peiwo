package me.peiwo.peiwo.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

public class PPBaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String getJsonValue(JSONObject obj, String key) {
        if (obj == null)
            return "";
        String temp = obj.optString(key, "");
        return "null".equalsIgnoreCase(temp) ? "" : temp;
    }

    protected int getJsonInt(JSONObject obj, String key) {
        if (obj == null)
            return 0;
        return obj.optInt(key, 0);
    }

    protected JSONArray getJsonArray(JSONObject obj, String key) {
        if (obj == null)
            return new JSONArray();
        JSONArray array = obj.optJSONArray(key);
        return array == null ? new JSONArray() : array;
    }
}
