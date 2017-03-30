package io.araujo.androidhttp.webservice.error;

import com.android.volley.VolleyError;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.araujo.androidhttp.util.JsonUtil;
import io.araujo.androidhttp.util.Utils;
import io.araujo.androidhttp.webservice.BaseMessage;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;

public class Failure extends BaseMessage<Failure> {
	public static interface FailureHandler {
		public void print(Failure failure, String reason);

		public void crash(Failure failure, String reason);
	}

	private static List<FailureHandler> handlers = new ArrayList<FailureHandler>();

	public static void addHandler(FailureHandler handler) {
		handlers.add(handler);
	}

	private FailureType type;
	private Integer httpStatus;
	private Map<String, String> headers;
	private String response;
	private Object responseObject;
	private VolleyError volleyError;
	private VolleyRequest<?> request;
	private Throwable throwable;
	private boolean noNetworkAvailable;

	public void setNoNetworkAvailable(boolean noNetworkAvailable) {
		this.noNetworkAvailable = noNetworkAvailable;
	}

	public boolean isNoNetworkAvailable() {
		return noNetworkAvailable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setRequest(VolleyRequest<?> request) {
		this.request = request;
	}

	public VolleyRequest<?> getRequest() {
		return request;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public boolean equalsHttpStatus(Integer httpStatus) {
		return this.httpStatus != null && this.httpStatus.equals(httpStatus);
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;

		try {
			Class<?> cl = getErrorClass(request.getWebService().getClass());

			if (!cl.equals(Void.class)) {
				responseObject = JsonUtil.getJsonParser().fromJson(response, cl);
			}
		} catch (Throwable t) {
		}
	}

	public FailureType getType() {
		return type;
	}

	public void setType(FailureType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("httpStatus", httpStatus);
		map.put("headers", headers);
		map.put("response", response);
		map.put("responseObject", responseObject);
		map.put("volleyError", volleyError);
		map.put("request", request);
		map.put("throwable", throwable);

		StringBuffer buf = new StringBuffer();

		for (String key : map.keySet()) {
			if (map.get(key) != null) {
				if (buf.length() > 0)
					buf.append(",");
				buf.append(key + "=" + map.get(key));
			}
		}

		return "Failure [" + buf + "]";
	}

	public VolleyError getVolleyError() {
		return volleyError;
	}

	public void setVolleyError(VolleyError volleyError) {
		this.volleyError = volleyError;

		if (volleyError != null) {
			if (volleyError.networkResponse != null) {
				setHttpStatus(volleyError.networkResponse.statusCode);
				setHeaders(volleyError.networkResponse.headers);
				setType(FailureType.HTTP_ERROR);
				setResponse(Utils.getDataAsString(volleyError.networkResponse));
			} else {
				setType(FailureType.OTHER_ERROR);
				setResponse(volleyError.getMessage());
			}
		}
	}

	public Object getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(Object responseObject) {
		this.responseObject = responseObject;
	}

	private Class<?> getErrorClass(Class<?> cl) {
		if (((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments().length >= 3) {
			return (Class<?>) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[2];
		} else {
			return getErrorClass(cl.getSuperclass());
		}
	}

	// rename to log or report
	public void print() {
		print(null);
	}

	// rename to log or report
	public void print(String reason) {
		// disabled this since CrashReporting also logs to System.err, not need in printing twice
		// Log.e("Failure", (reason != null ? reason + "\n" : "") + "Failure: "
		// + this, new Exception());
		//
		// if (throwable != null) {
		// throwable.printStackTrace();
		// }

		for (FailureHandler handler : handlers) {
			handler.print(this, reason);
		}
	}

	public void crash() {
		crash(null);
	}

	public void crash(String reason) {
		for (FailureHandler handler : handlers) {
			handler.crash(this, reason);
		}

		throw new RuntimeException((reason != null ? reason + "\n" : "") + "Failure: " + this);
	}
}
