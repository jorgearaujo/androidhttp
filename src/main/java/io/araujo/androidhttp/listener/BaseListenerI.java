package io.araujo.androidhttp.listener;

import io.araujo.androidhttp.webservice.error.Failure;

//all 4 methods, onStart, onDatabaseSuccess, onServerSuccess and onFinish are executed on the main/ui thread
//onFailure could be either from main or from background thread
public interface BaseListenerI {
	public abstract void onStart();

	public abstract void onFinish();

	public abstract void onFailure(Failure failure);
}
