package io.araujo.androidhttp.webservice.request;

import io.araujo.androidhttp.listener.service.BaseServiceListener;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;

public interface Handler<P, T> {

	public void execute(BaseServiceListener<T> listener, VolleyRequest<T> request, P param);
}
