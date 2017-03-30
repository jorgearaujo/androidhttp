package io.araujo.androidhttp.webservice.request.volley.requesthandler;

import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import java.lang.annotation.Annotation;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.araujo.androidhttp.database.PersistentObject;
import io.araujo.androidhttp.listener.service.BaseServiceListener;
import io.araujo.androidhttp.util.ReflectionUtil;
import io.araujo.androidhttp.webservice.Method;
import io.araujo.androidhttp.webservice.WebService;
import io.araujo.androidhttp.webservice.annotation.Cancelable;
import io.araujo.androidhttp.webservice.request.Handler;
import io.araujo.androidhttp.webservice.request.LiveHandler;
import io.araujo.androidhttp.webservice.request.RequestHandler;
import io.araujo.androidhttp.webservice.request.volley.AndroidHttp;
import io.araujo.androidhttp.webservice.request.RequestBuilder;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;
import io.araujo.androidhttp.webservice.response.VolleyResponseHandler;

public class VolleyRequestHandler<P, L, E> extends RequestHandler<P, L, E> {

	private VolleyRequest<L> request;
	final private VolleyResponseHandler<L> responseHandler = new VolleyResponseHandler<L>();
	Handler<P, L> liveHandler = new LiveHandler<P, L>();

    @Override
    public <Z extends PersistentObject> void execute(WebService<P, L, E> webService, P params, BaseServiceListener<L> listener) {
        RequestBuilder<L> requestBuilder = new RequestBuilder<L>(getMethod(webService, webService.getClass()), getPath(webService, webService.getClass(), params),
                getFromErrorListener(listener));
        requestBuilder = requestBuilder.successListener((Listener<L>) getFromListener(listener)).body(webService.body(params)).bodyBytes(webService.bodyBytes(params))
                .params(webService.params(params)).headers(webService.headers(params)).webService(webService);
        addContentType(webService, webService.getClass(), requestBuilder);
        addDataType(webService, webService.getClass(), requestBuilder);
        addCancelable(webService, webService.getClass(), requestBuilder);
        request = requestBuilder.build();

        handleRequest(webService, listener, request, params);
    }

	private String getPath(WebService<P, L, E> webService, Class<?> webServiceClass, P params) {
		Annotation annotation = ReflectionUtil.findAnnotation(webServiceClass, Path.class);
		if (annotation != null) {
			return ((Path) annotation).value();
		} else {
			if (webServiceClass.equals(Object.class)) {
				return webService.path(params);
			} else {
				return getPath(webService, webServiceClass.getSuperclass(), params);
			}
		}
	}

	private Method getMethod(WebService<P, L, E> webService, Class<?> webServiceClass) {
        Annotation annotation = ReflectionUtil.findAnnotation(webServiceClass, GET.class);
        annotation = annotation == null ? ReflectionUtil.findAnnotation(webServiceClass, POST.class) : annotation;
        annotation = annotation == null ? ReflectionUtil.findAnnotation(webServiceClass, PUT.class) : annotation;
        annotation = annotation == null ? ReflectionUtil.findAnnotation(webServiceClass, DELETE.class) : annotation;
		if (annotation != null) {
            return getMethodValueFromAnnotation(annotation);
		} else {
			if (webServiceClass.equals(Object.class)) {
				return webService.method();
			} else {
				return getMethod(webService, webServiceClass.getSuperclass());
			}
		}
	}

   private Method getMethodValueFromAnnotation(Annotation annotation) {
        if (annotation.annotationType().equals(GET.class)) {
            return Method.GET;
        } else if (annotation.annotationType().equals(POST.class)) {
           return Method.POST;
       } else if (annotation.annotationType().equals(GET.class)) {
           return Method.PUT;
       } else if (annotation.annotationType().equals(GET.class)) {
           return Method.DELETE;
       } else {
            return null;
        }
    }

	private RequestBuilder<L> addContentType(WebService<P, L, E> webService, Class<?> webServiceClass, RequestBuilder<L> requestBuilder) {
		Annotation annotation = ReflectionUtil.findAnnotation(webServiceClass, Produces.class);
		if (annotation != null) {
			requestBuilder.contentType(((Produces) annotation).value()[0]);
            return requestBuilder;
		} else {
			if (webServiceClass.equals(Object.class)) {
                requestBuilder.contentType(webService.produces().getType() + "/" + webService.produces().getSubtype());
                return requestBuilder;
			} else {
				return addContentType(webService, webServiceClass.getSuperclass(), requestBuilder);
			}
		}
	}

    private RequestBuilder<L> addDataType(WebService<P, L, E> webService, Class<?> webServiceClass, RequestBuilder<L> requestBuilder) {
        Annotation annotation = ReflectionUtil.findAnnotation(webServiceClass, Consumes.class);
        if (annotation != null) {
            requestBuilder.dataType(((Consumes) annotation).value()[0]);
            return requestBuilder;
        } else {
            if (webServiceClass.equals(Object.class)) {
                requestBuilder.dataType(webService.consumes().getType() + "/" + webService.consumes().getSubtype());
                return requestBuilder;
            } else {
                return addDataType(webService, webServiceClass.getSuperclass(), requestBuilder);
            }
        }
    }

	private RequestBuilder<L> addCancelable(WebService<P, L, E> webService, Class<?> webServiceClass, RequestBuilder<L> requestBuilder) {
		Annotation annotation = ReflectionUtil.findAnnotation(webServiceClass, Cancelable.class);
		if (annotation != null) {
			requestBuilder.cancelable(Boolean.TRUE);
			return requestBuilder;
		} else {
			if (webServiceClass.equals(Object.class)) {
				requestBuilder.cancelable(webService.isCancelable());
				return requestBuilder;
			} else {
				return addCancelable(webService, webServiceClass.getSuperclass(), requestBuilder);
			}
		}
	}

	private void handleRequest(final WebService<P, L, E> webService, final BaseServiceListener<L> listener, final VolleyRequest<L> request,
			final P neededInformation) {
		if (request != null) {
			if (request.isCancelable()) {
				request.setTag(webService.getClass().getCanonicalName());
			}

			if (listener != null)
				listener.onStart();

			if (request.isCancelable()) {
				AndroidHttp.getRequestQueue().cancelAll(webService.getClass().getCanonicalName());
			}
			liveHandler.execute(listener, request, neededInformation);
		}
	}

	private Response.Listener<?> getFromListener(final BaseServiceListener<L> listener) {
		return new Response.Listener<L>() {
			@Override
			public void onResponse(L object) {
				responseHandler.onSuccess(listener, request, object);
			}
		};
	}

	private Response.ErrorListener getFromErrorListener(final BaseServiceListener<L> listener) {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseHandler.onError(listener, request, error);
			}
		};
	}

}
