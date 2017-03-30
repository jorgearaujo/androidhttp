package io.araujo.androidhttp.webservice.response;

import java.lang.reflect.Type;

import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;

public interface VolleyParser<T> {
	public T parse(VolleyRequest<T> request, String response);
    public T parse(String response, boolean isList, Type typeToken, Class clazz);
}
