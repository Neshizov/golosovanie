package com.example.golosovanie;

import android.content.Context;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONObject;

public class APIClient {
    private static final String BASE_URL = "http://monarch-api.ru";
    private static RequestQueue requestQueue;

    public static void init(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
    }

    public static void get(String endpoint, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        if (requestQueue == null) {
            throw new IllegalStateException("APIClient not initialized. Call init() first.");
        }
        StringRequest request = new StringRequest(Request.Method.GET, BASE_URL + endpoint, listener, errorListener);
        requestQueue.add(request);
    }

    public static void delete(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        if (requestQueue == null) {
            throw new IllegalStateException("APIClient not initialized. Call init() first.");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, BASE_URL + url, listener, errorListener);
        requestQueue.add(stringRequest);
    }

    public static void post(String endpoint, JSONObject data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        if (requestQueue == null) {
            throw new IllegalStateException("APIClient not initialized. Call init() first.");
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL + endpoint, data, listener, errorListener);
        requestQueue.add(request);
    }
}
