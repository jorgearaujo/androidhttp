package io.araujo.androidhttp.listener.service;

import java.sql.SQLException;

import io.araujo.androidhttp.webservice.RetrieveFrom;

public interface ServiceListenerI<T> {
	// should return object, should never return null
	public abstract T onSuccessBackground(T object) throws SQLException;

    public abstract void onSuccess(T object, RetrieveFrom from);
}
