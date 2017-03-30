package io.araujo.androidhttp.webservice.request;

import io.araujo.androidhttp.listener.service.BaseServiceListener;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;

public class LiveHandler<P, T> implements Handler<P, T> {

	@Override
	public void execute(BaseServiceListener<T> listener, VolleyRequest<T> request, P param) {
		request.execute();
	}

}
