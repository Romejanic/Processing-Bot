package com.processingbot.request;

import java.util.UUID;

public class SketchRunner {

	public static void runCode(String code) {
		StringBuilder codeBuilder = new StringBuilder();
		String instanceID = UUID.randomUUID().toString();
		// IMPORTS
		codeBuilder.append("import processing.core.*;\n");
		codeBuilder.append("import processing.data.*;\n");
		codeBuilder.append("import processing.event.*;\n");
		codeBuilder.append("import java.util.HashMap;\n");
		codeBuilder.append("import java.util.ArrayList;\n");
		codeBuilder.append("import java.io.File;\n");
		codeBuilder.append("import java.io.BufferedReader;\n");
		codeBuilder.append("import java.io.PrintWriter;\n");
		codeBuilder.append("import java.io.InputStream;\n");
		codeBuilder.append("import java.io.OutputStream;\n");
		codeBuilder.append("import java.io.IOException;\n");
		// CLASS HEADER
		codeBuilder.append("public class Sketch_");
		codeBuilder.append(instanceID);
		codeBuilder.append(" extends PApplet {\n");
		// SETUP()
		codeBuilder.append("public void setup() {\n");
		codeBuilder.append(code);
		codeBuilder.append("noLoop()\n");
		codeBuilder.append("}\n");
		// SETTINGS
		codeBuilder.append("public void settings() {}\n");
		// FINAL CODE
		code = codeBuilder.toString();
		
		System.out.println(code);
	}
	
}