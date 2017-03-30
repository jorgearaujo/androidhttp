package io.araujo.androidhttp.webservice.response;

import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

import io.araujo.androidhttp.util.JsonUtil;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;

public class VolleyJsonParser<T> implements VolleyParser<T> {

	public T parse(VolleyRequest<T> request, String response) {
		try {
            System.out.println();
			return JsonUtil.getJsonParser().fromJson(response, request.isList() ? request.getTypeToken() : request.getReturnClass());
		} catch (IllegalStateException e) {
			throw new IllegalStateException("parseJson failed for " + request, e);
		} catch (JsonSyntaxException e) {
			throw new RuntimeException("parseJson failed for " + request, e);
		}
	}

    public T parse(String response, boolean isList, Type typeToken, Class clazz) {
        return JsonUtil.getJsonParser().fromJson(response, isList ? typeToken : clazz);
    }
}
