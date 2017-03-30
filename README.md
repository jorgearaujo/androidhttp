# androidhttp

Simple library for Android that allows to make REST API requests in a quick way, and allows to automatically store thre result in the database or memory for a future use.

#### 1. Set up database

Modify the `AndroidManifest.xml` (app level) with the database:

```xml
<meta-data
            android:name="DATABASE"
            android:value="androidhttp_example.db" />
        <meta-data
            android:name="VERSION"
            android:value="1" />
        <meta-data
            android:name="QUERY_LOG"
            android:value="true" />
        <meta-data
            android:name="DOMAIN_PACKAGE_NAME"
            android:value="com.araujo.androidhttp.example" />
```

`DATABASE` is the name of the database file. `DOMAIN_PACKAGE_NAME` is the package where the models are. The classes in this package should extend from `PersistentObject`.

#### 1. Create your Service

For that, you need to create a class that extends from WebService. WebService has three generic type arguments:
- First one: Object with parameters.
- Second one: Object that will contain the response. If you want to store this value in Database or Memory, this class needs to extend from PersistentObject.
- Third one: Object that will contain the response in case of an error.

You can override the following methods:
- `consumes()`: To specify the kind of data that the web service will receive.
- `produces()`: To specify the kind of data that the web service will sent.
- `method()`: To specify the method of the REST API.
- `path(Param)`: To specify the URL of the REST service.
- `params(Param)`: To specify the query params.
- `headers(Param)`: To specify the head params.
- `body(Param)`: To specify the body (string).
- `bodyString(Param)`: To specify the body (bytes).

An example:

```java
public class TestService extends WebService<Void, Ip, Void> {

	@Override
	public MediaType consumes() {
		return MediaType.APPLICATION_JSON_TYPE;
	}

	@Override
	public Method method() {
		return Method.GET;
	}

	@Override
	public String path(Void data) {
		return "https://api.ipify.org/";
	}

	@Override
	public Map<String, String> params(Void data) {
		Map<String, String> map = super.params(data);
		map.put("format", "json");
		return map;
	}
}
```

#### 2. Call the web service the way you want!

To call a web service, you just to instantiate it and call execute. This method has two params, the first one with the object that the web service requires, and the second one is the Listener that will be waiting for the response.

```java
new TestService().execute(null, getServiceListener());
```

This will basically retrieve the info from the server, but you can specify some options in the middle:

- `retrieveFrom(RetrieveFrom)`: It specifies where the information comes from. Possible values are `SERVER` (default), `BEFORE_CALL` (retrieves the value from database/memory and then from server), and `AFTER_CALL_ONLY_IF_SERVICE_FAILS` (it returns the database/memory value only if the service fails).
- `persist(PersistType...)`: It specifies where to store the result. Possible values are `DATABASE` and `MEMORY`.
- `persistMode(PersistMode)`: It specifies how the data is going to be stored. Possible values are `CLEAN` (removes everything before saving), `UPDATE` (updates the values that share the ID, requires the model to implement methods `getIdField()` and `getIdValue()`) and `ADD` (adds new items to the stored results).
- `event(Message)`: This library supports EventBus, so you can specify an event with a message that contains the response of the web service and it will called automatically.

#### 4. Example
You have available an example in the [following link](https://github.com/jorgearaujo/androidhttp-example).
