package io.araujo.androidhttp.webservice.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.araujo.androidhttp.util.ReflectionUtil;
import io.araujo.androidhttp.webservice.Method;
import io.araujo.androidhttp.webservice.WebService;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;

public class RequestBuilder<L> {

	private VolleyRequest<L> request;

	public VolleyRequest<L> build() {
		return request;
	}

	public RequestBuilder(Method method, String path, ErrorListener errorListener) {
        request = new VolleyRequest<L>(method(method), path, errorListener);
	}


	private int method(Method method) {
		if (method.equals(Method.GET)) {
			return com.android.volley.Request.Method.GET;
		} else if (method.equals(Method.POST)) {
			return com.android.volley.Request.Method.POST;
		} else if (method.equals(Method.PUT)) {
			return com.android.volley.Request.Method.PUT;
		} else if (method.equals(Method.DELETE)) {
			return com.android.volley.Request.Method.DELETE;
		} else if (method.equals(Method.HEAD)) {
            return com.android.volley.Request.Method.HEAD;
        }else if (method.equals(Method.OPTIONS)) {
            return com.android.volley.Request.Method.OPTIONS;
        }else if (method.equals(Method.PATCH)) {
            return com.android.volley.Request.Method.PATCH;
        }else if (method.equals(Method.TRACE)) {
            return com.android.volley.Request.Method.TRACE;
        }
		return 0;
	}

	public RequestBuilder<L> webService(WebService<?, ?, ?> webService) {
		request.setWebService(webService);
		calculateReturnClass(webService.getClass());
		calculateErrorClass(webService.getClass());
		return this;
	}

	protected void calculateReturnClass(Class<?> cl) {
		Class<?> clazz = null;
		Type type = null;

		Type pt = ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
		if (pt instanceof GenericArrayType) {
			type = ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
			this.isList(Boolean.TRUE);
			this.returnList(type);
		} else if (ReflectionUtil.getRawClass(pt).equals(List.class) || ReflectionUtil.getRawClass(pt).equals(Set.class)) {
			type = (ParameterizedType) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
			this.isList(Boolean.TRUE);
			this.returnList(type);
		} else {
			clazz = (Class<?>) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
			this.isList(Boolean.FALSE);
			this.returnClass(clazz);
		}
	}

	private void calculateErrorClass(Class<?> cl) {
		if ((cl.getGenericSuperclass() instanceof ParameterizedType) == false) {
			throw new RuntimeException("calculateErrorClass error for " + cl + ", " + cl.getSuperclass() + " " + cl.getSuperclass().getClass());
		}

		if (((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments().length >= 3) {
			errorClass((Class<?>) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[2]);
		} else {
			calculateErrorClass(cl.getSuperclass());
		}
	}

    public RequestBuilder<L> contentType(String contentType) {
        request.setBodyContentType(contentType);
        return this;
    }

	public RequestBuilder<L> dataType(String dataType) {
		request.setDataType(dataType);
		return this;
	}

	public RequestBuilder<L> returnClass(Class<?> clazz) {
		request.setReturnClass(clazz);
		return this;
	}

	public RequestBuilder<L> errorClass(Class<?> clazz) {
		request.setErrorClass(clazz);
		return this;
	}

	public RequestBuilder<L> returnList(Type type) {
		request.setType(type);
		return this;
	}

	public RequestBuilder<L> isList(Boolean bool) {
		request.setIsList(bool);
		return this;
	}

	public RequestBuilder<L> successListener(Listener<L> listener) {
		request.setListener(listener);
		return this;
	}

	public RequestBuilder<L> body(String body) {
		request.setBody(body);
		return this;
	}
	
	public RequestBuilder<L> bodyBytes(byte[] body) {
		request.setBodyBytes(body);
		return this;
	}

	public RequestBuilder<L> params(Map<String, String> params) {
		request.setParams(params);
		return this;
	}

	public RequestBuilder<L> headers(Map<String, String> headers) {
		request.setHeaders(headers);
		return this;
	}

	public RequestBuilder<L> cancelable(Boolean cancelable) {
		request.setCancelable(cancelable);
		return this;
	}

}
