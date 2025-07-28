package com.HomyStayWeb.Tools;

import com.google.gson.JsonObject;

public class JsonMsg {

    public static JsonObject responseMsg(int code,String msg) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("code", code);
        jsonObject.addProperty("msg", msg);
        return jsonObject;
    }
}
