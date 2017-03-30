package io.araujo.androidhttp.webservice.request.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.security.KeyStore;

import io.araujo.androidhttp.database.PersistenceHandlerI;
import io.araujo.androidhttp.webservice.request.common.EasySSLSocketFactory;
import io.araujo.androidhttp.webservice.request.common.ValidSSLSocketFactory;

public class AndroidHttp {

    private static Context context;
    private static PersistenceHandlerI persistenceHandler;

	public static int TIMEOUT = 30 * 1000;
	private static final int MAX_CONNECTIONS = 30;
	private static final String CHARSET = HTTP.UTF_8;

	private static RequestQueue mRequestQueue;

	private AndroidHttp() {
	}

    public static Context getContext() {
        return context;
    }

    public static PersistenceHandlerI getPersistenceHandler() {
        return persistenceHandler;
    }

    public static void init(Context context, PersistenceHandlerI persistenceHandlerI, KeyStore sslKeyStore) {
        AndroidHttp.context = context;
        AndroidHttp.persistenceHandler = persistenceHandlerI;
        HttpParams httpParams = getHttpParams();
		DefaultHttpClient httpClient = getHttpClient(httpParams, sslKeyStore);
		mRequestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context, new HttpClientStack(httpClient));
	}

	private static DefaultHttpClient getHttpClient(HttpParams params, KeyStore sslKeystore) {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", sslKeystore != null ? ValidSSLSocketFactory.createFactory(sslKeystore) : new EasySSLSocketFactory(), 443));

		ClientConnectionManager clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(clientConnectionManager, params);
	}

	private static HttpParams getHttpParams() {
		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, MAX_CONNECTIONS);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(MAX_CONNECTIONS));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, CHARSET);
		HttpProtocolParams.setHttpElementCharset(params, CHARSET);
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);
		return params;
	}

	public static RequestQueue getRequestQueue() {
		if (mRequestQueue != null) {
			return mRequestQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}
}
