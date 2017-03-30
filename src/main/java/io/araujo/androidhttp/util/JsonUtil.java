package io.araujo.androidhttp.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

	protected static Gson gson;
	protected static List<JsonPair> pairs;

	protected static GsonBuilder getJsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		if (pairs != null) {
			for (JsonPair pair : pairs) {
				gsonBuilder.registerTypeAdapter(pair.getClazz() != null ? pair.getClazz() : pair.getType(),
						pair.getDeserializer() != null ? pair.getDeserializer() : pair.getSerializer());
			}
		}
		return gsonBuilder;
	}

	public static Gson getJsonParser() {
		if (gson == null) {
			gson = getJsonBuilder().create();
		}
		return gson;
	}

	public static void setJsonParser(Gson jsonParser) {
		gson = jsonParser;
	}

	public static void addDeserializers(JsonPair pair) {
		if (JsonUtil.pairs == null) {
			JsonUtil.pairs = new ArrayList<JsonPair>();
		}
		JsonUtil.pairs.add(pair);
	}

	public static void addSerializer(JsonPair pair) {
		if (JsonUtil.pairs == null) {
			JsonUtil.pairs = new ArrayList<JsonPair>();
		}
		JsonUtil.pairs.add(pair);
	}

}
