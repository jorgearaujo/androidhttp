package io.araujo.androidhttp.listener.service;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import io.araujo.androidhttp.webservice.BaseMessage;
import io.araujo.androidhttp.webservice.RetrieveFrom;
import io.araujo.androidhttp.webservice.error.Failure;

// This listener gives you 2 callbacks on success:
// 1. onSuccesBackground in which you can perform tasks in a background task
// 2. onSuccesUi in which you can perform tasks on the UI thread
// the return of onSuccessBackground will be given to the onSuccess
public class ServiceListener<T> extends BaseServiceListener<T> implements ServiceListenerI<T> {
    public ServiceListener() {
        super();
    }

    @Override
    public T onSuccessBackground(T object) {
        return object;
    }

    @Override
    public void onSuccess(T object, RetrieveFrom from) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onResponse(T object, final RetrieveFrom from) {
        new AsyncTask<T, Void, T>() {
            private Failure failure;

            @Override
            protected T doInBackground(T... params) {
                return onSuccessBackground(params[0]);
            }

            @Override
            protected void onPostExecute(T result) {
                if (failure != null) {
                    onFailure(failure);
                    if (getRequest().getWebService().isSendMessage()) {
                        try {
                            EventBus.getDefault().post(((BaseMessage) getRequest().getWebService().getMessage().newInstance())
                                    .setObject(failure).setNotOk().setFrom(from));
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (getRequest().getWebService().isSendMessage()) {
                        try {
                            if (result != null) {
                                EventBus.getDefault().post(((BaseMessage) getRequest().getWebService().getMessage().newInstance())
                                        .setObject(result).setOk().setFrom(from));
                            }
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    onSuccess(result, from);
                }
                onFinish();
            }
        }.execute(object);
    }
}
