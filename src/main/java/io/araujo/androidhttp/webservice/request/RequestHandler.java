package io.araujo.androidhttp.webservice.request;

import io.araujo.androidhttp.database.PersistentObject;
import io.araujo.androidhttp.listener.service.BaseServiceListener;
import io.araujo.androidhttp.webservice.WebService;

public abstract class RequestHandler<P, L, E> {
	public static boolean Verbose = true;

	public abstract <Z extends PersistentObject> void execute(WebService<P, L, E> webService, P params, BaseServiceListener<L> listener);
}
