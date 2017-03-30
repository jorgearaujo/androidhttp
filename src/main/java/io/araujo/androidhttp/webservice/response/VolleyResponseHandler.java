package io.araujo.androidhttp.webservice.response;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;

import io.araujo.androidhttp.listener.service.BaseServiceListener;
import io.araujo.androidhttp.webservice.request.volley.request.VolleyRequest;

public class VolleyResponseHandler<T> {

	public void onSuccess(final BaseServiceListener<T> listener, final VolleyRequest<T> request, final T object) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                NetworkResponse response = (NetworkResponse) object;
                log("=== WebService "+ request.hashCode() + " ===");
                log("Response: " + getStringResponse(response));
                log("======");
                if (listener != null) {
                    listener.callOnResponse(request, response);
                }
                return null;
            }
        }.execute();
	}

	public void onError(final BaseServiceListener<T> listener, final VolleyRequest<T> request, final VolleyError error) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                log("=== WebService "+ request.hashCode() + " ===");
                log("Response: " + (error != null ? getStringResponse(error.networkResponse) : "ERROR"));
                log("======");
                if (listener != null) {
                    listener.callOnFailureAndFinish(request, getStringResponse(error.networkResponse), error);
                } else {
                }
                return null;
            }
        }.execute();
	}

	protected String getStringResponse(NetworkResponse response) {
        try {
            return (response == null || response.data == null) ? null : new String(response.data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
	}

    private void log(String text) {
        Log.d("WebService", text);

    }

}
