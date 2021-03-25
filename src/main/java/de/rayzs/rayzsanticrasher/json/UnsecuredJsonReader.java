package de.rayzs.rayzsanticrasher.json;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;

public class UnsecuredJsonReader {
	
	private String result;
	
	public UnsecuredJsonReader(String url) {
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Accept", "text/html");
			result = IOUtils.toString(con.getInputStream());
		} catch (IOException error) { error.printStackTrace(); }
	}

	public String get() {
		try {
			if (result.isEmpty() || result == null)
				return "invalid name";
			return result;
		} catch (Exception error) {
			return "error -> " + error.getMessage();
		}
	}
}