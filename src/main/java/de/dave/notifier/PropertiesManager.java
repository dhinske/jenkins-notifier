package de.dave.notifier;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

public class PropertiesManager {

	private static Properties properties = new Properties(System.getProperties());

	static final String URL = "url";
	static final String USERNAME = "username";
	static final String TOKEN = "token";

	private PropertiesManager() {
		throw new IllegalAccessError("Utility class");
	}

	static void writeProperties(String url, String username, String token) {
		try (Writer writer = new FileWriter("properties.txt")) {
			properties.setProperty(URL, url);
			properties.setProperty(USERNAME, username);
			properties.setProperty(TOKEN, token);
			properties.store(writer, "Updating Properties");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static Properties getProperties() {
		try (Reader reader = new FileReader("properties.txt")) {
			properties.load(reader);
			properties.list(System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}
