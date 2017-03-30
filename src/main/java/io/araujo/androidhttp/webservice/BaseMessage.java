package io.araujo.androidhttp.webservice;

/**
 * Created by jorge.araujo on 1/4/2016.
 */
public abstract class BaseMessage<T> {

    protected T mObject;
    protected RetrieveFrom mFrom;
    protected boolean mOk;

    public BaseMessage setOk() {
        mOk = true;
        return this;
    }

    public BaseMessage setNotOk() {
        mOk = false;
        return this;
    }

    public BaseMessage setObject(T object) {
        mObject = object;
        return this;
    }

    public BaseMessage setFrom(RetrieveFrom from) {
        mFrom = from;
        return this;
    }

    public RetrieveFrom getFrom() {
        return mFrom;
    }


    public T getObject() {
        return mObject;
    }

    public boolean isOk() {
        return mOk;
    }
}
