package io.araujo.androidhttp.util;

import com.android.volley.NetworkResponse;

import java.io.UnsupportedEncodingException;

public class Utils {

	public static String getDataAsString(NetworkResponse response) {
		try {
			return (response.data == null) ? null : new String(response.data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
