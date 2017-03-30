package io.araujo.androidhttp.webservice.request.volley.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import io.araujo.androidhttp.webservice.WebService;
import io.araujo.androidhttp.webservice.request.common.Header;
import io.araujo.androidhttp.webservice.request.volley.AndroidHttp;
import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;

public class VolleyRequest<T> extends Request<T> {
	private static final String PROTOCOL_CHARSET = "utf-8";

	private long initRequestMs;
	private long endParsingMs;

	protected WebService<?, ?, ?> webService;
    protected String bodyContentType;
	protected String dataType;
	protected int method;
	protected String path;
	protected Class<?> returnClass;
	protected Class<?> errorClass;
	protected Type type;
	protected Boolean isList;
	protected Listener<T> listener;
	protected String body;
	protected byte[] bodyBytes;
	protected Map<String, String> params;
	protected Map<String, String> headers;
	protected Boolean cancelable;

	public VolleyRequest(int method, String url, ErrorListener errorListener) {
		super(method, url, errorListener);

		initThrowable = new Throwable();
		initRequestMs = System.currentTimeMillis();

		this.method = method;
		this.path = url;

		setShouldCache(false);
		setRetryPolicy(new DefaultRetryPolicy(AndroidHttp.TIMEOUT, 0, 0));
		generatePath(url);
	}

	protected String getStringResponse(NetworkResponse response) throws UnsupportedEncodingException {
		return new String(response.data, "UTF-8");
	}

    private void log(String text) {
        Log.d("WebService", text);

    }

	public void execute() {
        log("=== WebService "+ this.hashCode() + " ===");
        log(methodToString() + " " + path);
        {
            String headers = headersToString();
            log("Headers: " + headers);
        }
        {
			String params = paramsToString();
            log("Params: " + params);
        }


		try {
			if (body != null) {
				byte[] body = getBody();
				String strBody = body != null ? new String(body, "UTF-8") : null;
                log("Body: " + strBody);
			} else if (bodyBytes != null) {
				String strBody = bodyBytes != null ? new String(bodyBytes, "UTF-8") : null;
                log("Body: " + strBody);
			}
		} catch (AuthFailureError e) {
		} catch (UnsupportedEncodingException e) {
		}
        log("======");
		AndroidHttp.getRequestQueue().add(this);
	}

	@Override
	public Map<String, String> getParams() throws AuthFailureError {
		return params;
	}

	public Map<String, String> getRawParams() {
		return params;
	}

	@Override
	public byte[] getPostBody() throws AuthFailureError {
		return getBody();
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		if (body != null) {
			try {
				return body.getBytes(PROTOCOL_CHARSET);
			} catch (UnsupportedEncodingException e) {
				return super.getBody();
			}
		} else if (bodyBytes != null) {
			return bodyBytes;
		} else {
			return super.getBody();
		}
	}

	public String getStringBody() {
		return body;
	}

    @Override
    public String getBodyContentType() {
            return bodyContentType;
    }

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
        if (headers != null) {
            if (headers.get(Header.CONTENT_TYPE) == null)
                headers.put(Header.CONTENT_TYPE, bodyContentType);

            if (headers.get(Header.ACCEPT) == null)
                headers.put(Header.ACCEPT, dataType);
        }
		return headers;
	}

    public Map<String, String> getRawHeaders() {
        return headers;
    }

    public String getStringHeaders() {
        try {
            return getStringFromMap(getHeaders());
        } catch (AuthFailureError e) {
            return null;
        }
    }

    public String getStringParams() {
        try {
            return getStringFromMap(getParams());
        } catch (AuthFailureError e) {
            return null;
        }
    }

    private String getStringFromMap(Map<String, String> map) {
        String value = "";
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            value += entry.getKey() + "=" + entry.getValue() + ",";
        }
        return value;
    }

	@Override
	public void deliverResponse(T response) {
		endParsingMs = (new Date()).getTime();
		listener.onResponse(response);
	}

	private final Throwable initThrowable;
	
	public Throwable getInitThrowable() {
		return initThrowable;
	}

	public void deliverError(VolleyError error) {
		super.deliverError(error);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
		try {
			String response = getStringResponse(networkResponse);
		} catch (UnsupportedEncodingException e) {
		}
		return (Response<T>) Response.success(networkResponse, HttpHeaderParser.parseCacheHeaders(networkResponse));
	}

	@SuppressWarnings("rawtypes")
	private String getFormatted(Map<String, String> whatToFormat) throws AuthFailureError {
		if (whatToFormat == null || whatToFormat.isEmpty()) {
			return null;
		}

		StringBuilder buf = new StringBuilder();

		Iterator it = whatToFormat.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();

			if (buf.length() > 0)
				buf.append(";");

			buf.append(pairs.getKey() + "=" + pairs.getValue());
		}

		return buf.toString();
	}


	private String getRequestBody() {
		return this.body;
	}

	public void setParams(Map<String, String> params) {
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (entry.getKey() == null)
				throw new NullPointerException("Key = null for param");
			if (entry.getValue() == null)
				throw new NullPointerException("Value = null for param " + entry.getKey());
		}
		this.params = params;
		generatePath(path);
	}

	@SuppressWarnings("rawtypes")
	public String headersToString() {
		try {
			if (getHeaders() != null && !getHeaders().isEmpty()) {
				StringBuilder buf = new StringBuilder();

				Iterator it = getHeaders().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();

					if (buf.length() > 0)
						buf.append(", ");

					buf.append(pairs.getKey() + "=" + pairs.getValue());
				}

				return buf.toString();
			} else {
				return "";
			}
		} catch (AuthFailureError e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	public String paramsToString() {
		try {
			if (getParams() != null && !getParams().isEmpty()) {
				StringBuilder buf = new StringBuilder();

				Iterator it = getParams().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();

					if (buf.length() > 0)
						buf.append(", ");

					buf.append(pairs.getKey() + "=" + pairs.getValue());
				}

				return buf.toString();
			} else {
				return "";
			}
		} catch (AuthFailureError e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		String headers = headersToString();

		if (headers.isEmpty() == false) {
			headers = "headers: " + headers;
		}

		return "[VolleyRequest " + methodToString() + " " + path + " " + headers + "]";
	}

	public String methodToString() {
		if (this.method == Method.GET) {
			return "GET";
		} else if (this.method == Method.POST) {
			return "POST";
		} else if (this.method == Method.PUT) {
			return "PUT";
		} else if (this.method == Method.DELETE) {
			return "DELETE";
		} else if (this.method == Method.HEAD) {
            return "HEAD";
        }else if (this.method == Method.OPTIONS) {
            return "OPTIONS";
        }else if (this.method == Method.PATCH) {
            return "PATCH";
        }else if (this.method == Method.TRACE) {
            return "TRACE";
        }else {
			return "";
		}
	}

	public String getUrl() {
		return this.path;
	}

	public void generatePath(String finalPath) {
		if (path == null) {
			return;
		}

		UriBuilder uriBuilder = UriBuilder.create(path);
		QueryBuilder query = QueryBuilder.create();
		if (params != null && !params.isEmpty()) {
			Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pairs = it.next();

				query.addParam(pairs.getKey(), pairs.getValue());
			}
		}
		URI uri = uriBuilder.setQuery(query).build();
		finalPath = uri.toString();

		//finalUrl = finalUrl.replace("%2C", ",");
		finalPath = finalPath.replace("%24", "$");
		this.path = finalPath;
	}

    public WebService<?, ?, ?> getWebService() {
        return webService;
    }

    public void setWebService(WebService<?, ?, ?> webService) {
		this.webService = webService;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public Class<?> getReturnClass() {
		return returnClass;
	}

	public void setReturnClass(Class<?> returnClass) {
		this.returnClass = returnClass;
	}

	public void setErrorClass(Class<?> errorClass) {
		this.errorClass = errorClass;
	}

	public Class<?> getErrorClass() {
		return errorClass;
	}

	public Type getTypeToken() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setIsList(Boolean isList) {
		this.isList = isList;
	}

	public void setListener(Listener<T> listener) {
		this.listener = listener;
	}

	public Boolean isCancelable() {
		return cancelable;
	}

	public void setCancelable(Boolean cancelable) {
		this.cancelable = cancelable;
	}

	// @Override
	// protected Map<String, String> getParams() throws AuthFailureError {
	// return params;
	// }

	public void setPath(String path) {
		this.path = path;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setBodyBytes(byte[] bytes) {
		this.bodyBytes = bytes;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setBodyContentType(String bodyContentType) {
		this.bodyContentType = bodyContentType;
	}

	public Boolean isList() {
		return isList;
	}

}
