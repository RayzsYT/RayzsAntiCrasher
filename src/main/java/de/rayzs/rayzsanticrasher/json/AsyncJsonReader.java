package de.rayzs.rayzsanticrasher.json;

import java.io.IOException;
import org.apache.http.client.fluent.Request;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class AsyncJsonReader {

	private String result;

	public AsyncJsonReader(String url) {
		try {
			result = Request.Get(url).execute().returnContent().asString();
		} catch (IOException error) {
		}
	}

	public String get(String search) {
		try {
			if (result.isEmpty())
				return "invalid";
			JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(result);
			return UUIDObject.get(search).toString();
		} catch (ParseException error) { return "error"; }
	}
}