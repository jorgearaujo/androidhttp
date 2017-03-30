package io.araujo.androidhttp.listener.service;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.ws.rs.core.MediaType;

import io.araujo.androidhttp.database.PersistentObject;
import io.araujo.androidhttp.listener.BaseListener;
import io.araujo.androidhttp.listener.BaseListenerI;
import io.araujo.androidhttp.util.Utils;
import io.araujo.androidhttp.webservice.PersistType;
import io.araujo.androidhttp.webservice.RetrieveFrom;
import io.araujo.androidhttp.webservice.error.Failure;
import io.araujo.androidhttp.webservice.request.PersistMode;
import io.araujo.androidhttp.webservice.request.volley.AndroidHttp;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;
import io.araujo.androidhttp.webservice.request.volley.requesthandler.MemoryDatabaseRequestHandler;
import io.araujo.androidhttp.webservice.response.VolleyJsonParser;
import io.araujo.androidhttp.webservice.response.VolleyParser;
import io.araujo.androidhttp.webservice.response.VolleyStringParser;
import io.araujo.androidhttp.webservice.response.VolleyXmlParser;

public abstract class BaseServiceListener<T> extends BaseListener implements BaseListenerI {

    protected VolleyParser<T> jsonParser = new VolleyJsonParser<T>();
    protected VolleyParser<T> xmlParser = new VolleyXmlParser<T>();
    protected VolleyParser<T> stringParser = new VolleyStringParser<T>();

    private String name;
    private String description;
    private VolleyRequest<T> request;
    private NetworkResponse response;

    public BaseServiceListener() {
        super();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "[MGService name=" + name + " description=" + description + "]";
    }

    public abstract void onResponse(T object, RetrieveFrom from);

    // called when the response comes from offline or cache
    public void callOnResponse(VolleyRequest<T> request, String object) {
        try {
            T fromJson = null;
            if (object != null && !object.isEmpty()) {
                fromJson = getParser(request).parse(request, object);
                persist(fromJson);
            }
            onResponse(fromJson, RetrieveFrom.SERVER);
        } catch (RuntimeException e) {
            handleParseFailure(request, e);
        }
    }

    private void persist(T object) {
        if (request.getWebService().isPersistent()) {
            if (request.getWebService().getPersistTypeList() != null) {
                for (PersistType mode : request.getWebService().getPersistTypeList()) {
                    if (mode.equals(PersistType.CUSTOM)) {
                        AndroidHttp.getPersistenceHandler().persist(object);
                    } else if (mode.equals(PersistType.MEMORY)) {
                        if (request.isList()) {
                            ParameterizedType listType = (ParameterizedType) request.getTypeToken();
                            Class listClass = (Class<?>) listType.getActualTypeArguments()[0];

                            if (PersistMode.ADD.equals(request.getWebService().getPersistMode())) {
                                for (T item : ((List<T>) object)) {
                                    ((PersistentObject<T>) item).saveInMemory();
                                }
                            } else if (PersistMode.UPDATE.equals(request.getWebService().getPersistMode())) {
                                for (T item : ((List<T>) object)) {
                                    List<T> memoryItems = PersistentObject.findAllFromMemory(listClass);
                                    T itemToRemove = null;
                                    if (memoryItems != null && !memoryItems.isEmpty()) {
                                        for (T memoryItem : memoryItems) {
                                            if (((PersistentObject) memoryItem).getIdValue().equals(((PersistentObject) item).getIdValue())) {
                                                itemToRemove = memoryItem;
                                                break;
                                            }
                                        }
                                    }
                                    if (itemToRemove != null) memoryItems.remove(itemToRemove);
                                    ((PersistentObject<T>) item).saveInMemory();
                                }
                            } else {
                                PersistentObject.deleteAllFromMemory(listClass);
                                for (T item : ((List<T>) object)) {
                                    ((PersistentObject<T>) item).saveInMemory();
                                }
                            }
                        } else {
                            if (PersistMode.ADD.equals(request.getWebService().getPersistMode())) {
                                ((PersistentObject<T>) object).saveInMemory();
                            } else if (PersistMode.UPDATE.equals(request.getWebService().getPersistMode())) {
                                List<T> memoryItems = PersistentObject.findAllFromMemory(object.getClass());
                                T itemToRemove = null;
                                if (memoryItems != null && !memoryItems.isEmpty()) {
                                    for (T memoryItem : memoryItems) {
                                        if (((PersistentObject) memoryItem).getIdValue().equals(((PersistentObject) object).getIdValue())) {
                                            itemToRemove = memoryItem;
                                            break;
                                        }
                                    }
                                }
                                if (itemToRemove != null) memoryItems.remove(itemToRemove);
                                ((PersistentObject<T>) object).saveInMemory();
                            } else {
                                PersistentObject.deleteAllFromMemory((Class) object.getClass());
                                ((PersistentObject<T>) object).saveInMemory();
                            }
                        }
                    } else if (mode.equals(PersistType.DATABASE)) {
                        if (request.isList()) {
                            ParameterizedType listType = (ParameterizedType) request.getTypeToken();
                            Class listClass = (Class<?>) listType.getActualTypeArguments()[0];
                            if (PersistMode.ADD.equals(request.getWebService().getPersistMode())) {
                                for (T item : ((List<T>) object)) {
                                    ((PersistentObject<T>) item).save();
                                }
                            } else if (PersistMode.UPDATE.equals(request.getWebService().getPersistMode())) {
                                for (T item : ((List<T>) object)) {
                                    List<T> dbItems = PersistentObject.find(listClass, ((PersistentObject<T>) item).getIdField() + " = ?", String.valueOf(((PersistentObject<T>) item).getIdValue()));
                                    if (dbItems != null && !dbItems.isEmpty()) {
                                        T dbItem = dbItems.get(0);
                                        ((PersistentObject<T>) item).setId(((PersistentObject<T>) dbItem).getId());
                                    }
                                    ((PersistentObject<T>) item).save();
                                }
                            } else if (PersistMode.CLEAN.equals(request.getWebService().getPersistMode())) {
                                for (T item : (List<T>) PersistentObject.listAll(listClass)) {
                                    ((PersistentObject) item).delete();
                                }
                                for (T item : ((List<T>) object)) {
                                    ((PersistentObject<T>) item).save();
                                }
                            }
                        } else {
                            if (PersistMode.ADD.equals(request.getWebService().getPersistMode())) {
                                ((PersistentObject<T>) object).save();
                            } else if (PersistMode.UPDATE.equals(request.getWebService().getPersistMode())) {
                                List<T> dbItems = PersistentObject.find((Class) object.getClass(), ((PersistentObject<T>) object).getIdField() + " = ?", String.valueOf(((PersistentObject<T>) object).getIdValue()));
                                if (dbItems != null && !dbItems.isEmpty()) {
                                    T dbItem = dbItems.get(0);
                                    ((PersistentObject<T>) object).setId(((PersistentObject<T>) dbItem).getId());
                                }
                                ((PersistentObject<T>) object).save();
                            } else if (PersistMode.CLEAN.equals(request.getWebService().getPersistMode())) {
                                for (T item : (List<T>) PersistentObject.listAll((Class) object.getClass())) {
                                    ((PersistentObject) item).delete();
                                }
                                ((PersistentObject<T>) object).save();
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleParseFailure(VolleyRequest<?> request, RuntimeException e) {

        Failure failure = new Failure();

        failure.setRequest(request);
        failure.setThrowable(e);

        callOnFailureAndFinish(failure);
    }

    // called when the response comes from network, for now just redirects to the callOnResponse above
    public final void callOnResponse(VolleyRequest<T> request, NetworkResponse networkResponse) {
        this.request = request;
        this.response = networkResponse;
        callOnResponse(request, Utils.getDataAsString(networkResponse));
    }

    public VolleyParser<T> getParser(VolleyRequest<T> request) {
        if (request.getDataType().equals(MediaType.APPLICATION_JSON)) {
            return jsonParser;
        } else if (request.getDataType().equals(MediaType.APPLICATION_XML)) {
            return xmlParser;
        } else if (request.getDataType().equals(MediaType.TEXT_PLAIN) || request.getDataType().equals(MediaType.TEXT_HTML)) {
            return stringParser;
        }

        return null;
    }

    public void callOnFailureAndFinish(VolleyRequest<T> request, String object, VolleyError error) {
        this.request = request;
        callOnFailureAndFinish(request, object, buildError(request, error));
    }

    public void callOnFailureAndFinish(VolleyRequest<T> request, String object, Failure failure) {
        this.request = request;
        callOnFailureAndFinish(failure);
    }

    public void callOnFailureAndFinish(Failure failure) {
        if (request.getWebService().getRetrieveFromList() != null) {
            for (RetrieveFrom retrieveFrom : request.getWebService().getRetrieveFromList()) {
                if (retrieveFrom.equals(RetrieveFrom.AFTER_CALL_ONLY_IF_SERVICE_FAILS)) {
                    MemoryDatabaseRequestHandler databaseRequestHandler = new MemoryDatabaseRequestHandler(retrieveFrom);
                    databaseRequestHandler.execute(request.getWebService(), null, this);
                }
            }
        }
        onFinish();
    }

    public Failure buildError(VolleyRequest<T> request, VolleyError volleyError) {
        Failure failure = new Failure();

        failure.setRequest(request);
        failure.setVolleyError(volleyError);

        return failure;
    }

    public VolleyRequest<T> getRequest() {
        return request;
    }

    public NetworkResponse getResponse() {
        return response;
    }
}
