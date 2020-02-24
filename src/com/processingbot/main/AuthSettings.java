package com.processingbot.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

public class AuthSettings {
	
	private static final String AUTH_FILE = "auth.properties";
	private static final String TOKEN_PLACEHOLDER = "YOUR-TOKEN-HERE";

	public String botToken;
	
	protected boolean loadSettings() {
		try {
			Properties props = new Properties();
			File file = new File(AUTH_FILE);
			if(!file.exists()) {
				props.put("token", TOKEN_PLACEHOLDER);
				props.store(new FileOutputStream(file), "Processing Bot Auth File");
				return printNoTokenMessage();
			}
			props.load(new FileReader(file));
			this.botToken = props.getProperty("token");
			if(this.botToken.equalsIgnoreCase(TOKEN_PLACEHOLDER)) {
				return printNoTokenMessage();
			}
			return true;
		} catch(Exception e) {
			System.err.println("=================================");
			System.err.println("ERROR WHILE READING AUTH FILE");
			System.err.print("Caused by ");
			e.printStackTrace(System.err);
			System.err.println("=================================");
			return false;
		}
	}
	
	private boolean printNoTokenMessage() {
		System.out.println("=================================");
		System.out.println("NO BOT TOKEN PROVIDED!");
		System.out.println("Please edit the auth.properties file and add your bot's token.");
		System.out.println("=================================");
		return false;
	}
	
}