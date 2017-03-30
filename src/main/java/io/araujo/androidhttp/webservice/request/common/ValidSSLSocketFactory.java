package io.araujo.androidhttp.webservice.request.common;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ValidSSLSocketFactory extends SSLSocketFactory {
	protected SSLContext sslContext = SSLContext.getInstance("TLS");

	// in some future version, have a addKeyStore method in this class, is easier
	public ValidSSLSocketFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		super(null, null, null, null, null, null);
		sslContext.init(null, new TrustManager[] { new AdditionalKeyStoresTrustManager(keyStore) }, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
		return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}

	@Override
	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		return super.isSecure(sock);
	}

	public static class AdditionalKeyStoresTrustManager implements X509TrustManager {
		protected ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();

		protected AdditionalKeyStoresTrustManager(KeyStore... additionalkeyStores) {
			final ArrayList<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();

			try {
				final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				original.init((KeyStore) null);
				factories.add(original);

				for (KeyStore keyStore : additionalkeyStores) {
					final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					additionalCerts.init(keyStore);
					factories.add(additionalCerts);
				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			for (TrustManagerFactory tmf : factories)
				for (TrustManager tm : tmf.getTrustManagers())
					if (tm instanceof X509TrustManager)
						x509TrustManagers.add((X509TrustManager) tm);

			if (x509TrustManagers.size() == 0)
				throw new RuntimeException("Couldn't find any X509TrustManagers");
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
			defaultX509TrustManager.checkClientTrusted(chain, authType);
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			for (X509TrustManager tm : x509TrustManagers) {
				try {
					tm.checkServerTrusted(chain, authType);
					return;
				} catch (CertificateException e) {
					// ignore
				}
			}
			throw new CertificateException();
		}

		public X509Certificate[] getAcceptedIssuers() {
			final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
			for (X509TrustManager tm : x509TrustManagers)
				list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
			return list.toArray(new X509Certificate[list.size()]);
		}
	}

	public static ValidSSLSocketFactory createFactory(KeyStore keyStore) {
		try {
			return new ValidSSLSocketFactory(keyStore);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}