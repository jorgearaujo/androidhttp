package io.araujo.androidhttp.webservice;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import io.araujo.androidhttp.listener.service.BaseServiceListener;
import io.araujo.androidhttp.webservice.error.Failure;
import io.araujo.androidhttp.webservice.request.PersistMode;
import io.araujo.androidhttp.webservice.request.RequestHandler;
import io.araujo.androidhttp.webservice.request.volley.requesthandler.MemoryDatabaseRequestHandler;
import io.araujo.androidhttp.webservice.request.volley.requesthandler.VolleyRequestHandler;

// P: Params
// L: Result on Listener
// E: Error object

public class WebService<P, L, E> {
	public RequestHandler<P, L, E> requestHandler = new VolleyRequestHandler<P, L, E>();
    public RequestHandler<P, L, E> memoryDatabaseRequestHandler = new MemoryDatabaseRequestHandler<P, L, E>(RetrieveFrom.BEFORE_CALL);
	public P paramObject;

	public WebService() {
	}

	/**
	 * Default GET
	 */
	public Method method() {
		return Method.GET;
	}

	/**
	 * Default JSON
	 */
	public MediaType consumes() {
        return MediaType.APPLICATION_JSON_TYPE;
	}

	/**
	 * Default JSON
	 */
	public MediaType produces() {
		return MediaType.APPLICATION_JSON_TYPE;
	}

	/**
	 * Default empty string
	 */
	public String path(P data) {
		return "";
	}

	/**
	 * Default no body
	 */
	public String body(P data) {
		return null;
	}

	/**
	 * Default no body
	 */
	public byte[] bodyBytes(P data) {
		return null;
	}

	/**
	 * Default false
	 */
	public Boolean isCancelable() {
		return Boolean.FALSE;
	}

	/**
	 * Default true
	 */
	public Boolean active() {
		return Boolean.TRUE;
	};

	/**
	 * Default HashMap
	 */
	public Map<String, String> params(P data) {
		return getDefaultAndroidParams();
	}


    // DATABASE AND MEMORY STUFF
    private boolean persistent;
	private boolean sendMessage;
	private Class<?> message;
    private PersistType[] persistTypeList;
    private PersistMode persistMode = PersistMode.CLEAN;
    private RetrieveFrom[] retrieveFromList;
    private String field;
    private Object value;

    public WebService persist(PersistType... persistTypes) {
        this.persistent = true;
        persistTypeList = persistTypes;
        return this;
    }

	public WebService event(Class<?> message) {
		sendMessage = true;
		this.message = message;
		return this;
	}

    public WebService persistMode(PersistMode persistMode) {
        this.persistMode = persistMode;
        return this;
    }

    public PersistMode getPersistMode() {
        return persistMode;
    }

	public boolean isSendMessage() {
		return sendMessage;
	}

	public Class getMessage() {
		return message;
	}

	public boolean isPersistent() {
        return persistent;
    }

    public PersistType[] getPersistTypeList() {
        return persistTypeList;
    }

    public WebService retrieveFrom(RetrieveFrom... retrieveFroms) {
        this.retrieveFromList = retrieveFroms;
        return this;
    }

    public RetrieveFrom[] getRetrieveFromList() {
        return retrieveFromList;
    }

    public WebService findBy(String field, Object value) {
        this.field = field;
        this.value = value;
        return this;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    // END OF DATABASE AND MEMORY STUFF

    /**
	 * Default HashMap
	 */
	public Map<String, String> headers(P data) {
		return getDefaultAndroidHeaders();
	}

	// If force = true, cache will not be used
	public void execute(P params, BaseServiceListener<L> listener) {
		paramObject = params;
		if (listener != null) {
			listener.setDescription(path(params) + " - " + getClass().getSimpleName());
		}

		if (active()) {
            if (retrieveFromList != null) {
                for (RetrieveFrom from : retrieveFromList) {
                    if (from.equals(RetrieveFrom.BEFORE_CALL)) {
                        memoryDatabaseRequestHandler.execute(this, params, listener);
                    }
                }
            }
			requestHandler.execute(this, params, listener);
		} else {
			Failure failure = new Failure();
			failure.setResponse("Request not active");
			listener.callOnFailureAndFinish(null, null, failure);
		}
	}

	protected Map<String, String> getDefaultAndroidParams() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

	protected Map<String, String> getDefaultAndroidHeaders() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

}