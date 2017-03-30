package io.araujo.androidhttp.util;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class JsonPair {

	Class<?> clazz;
	Type type;
	JsonDeserializer<?> deserializer;
	JsonSerializer<?> serializer;

	public JsonPair(Class<?> clazz, JsonDeserializer<?> deserializer) {
		super();
		this.clazz = clazz;
		this.deserializer = deserializer;
	}

	public JsonPair(Class<?> clazz, JsonSerializer<?> serializer) {
		super();
		this.clazz = clazz;
		this.serializer = serializer;
	}

	public JsonPair(Type type, JsonDeserializer<?> deserializer) {
		super();
		this.type = type;
		this.deserializer = deserializer;
	}

	public JsonPair(Type type, JsonSerializer<?> serializer) {
		super();
		this.type = type;
		this.serializer = serializer;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public JsonDeserializer<?> getDeserializer() {
		return deserializer;
	}

	public JsonSerializer<?> getSerializer() {
		return serializer;
	}

	public Type getType() {
		return type;
	}

}
