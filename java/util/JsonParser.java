package com.jikexueyuan.jike_chat.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by sun xuan.
 */

public class JsonParser {
    public static String parseIatResult(String json){
        StringBuilder ret = new StringBuilder();
        try{
            JSONTokener tokener = new JSONTokener(json);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray words = jsonObject.getJSONArray("ws");
            int len = words.length();
            for (int i=0;i<len;i++){
                //transcribe the result, using the first result by default.
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject object = items.getJSONObject(0);
                ret.append(object.getString("w"));
                //If need more candidate results, parse the other fields of the array
				/*for(int j = 0; j < items.length(); j++){
					JSONObject obj = items.getJSONObject(j);
					ret.append(obj.getString("w"));
				}*/
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret.toString();
    }
}
