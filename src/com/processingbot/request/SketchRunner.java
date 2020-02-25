package com.processingbot.request;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PGraphics;

public class SketchRunner extends Thread {

	public static void runCode(String code, MessageChannel channel) {
		final String instanceID = UUID.randomUUID().toString().replaceAll("-", "");
		SketchRunner runner = new SketchRunner("Run Sketch " + instanceID) {
			public void run() {
				String sketchCode = this.convertCode(instanceID, code);
				Path sketchPath = Paths.get(System.getProperty("java.io.tmpdir"), "Sketch_" + instanceID + ".java");
				try {
					this.writeToPath(sketchCode, sketchPath);
				} catch (IOException e) {
					System.err.println("Failed to write to file!");
					e.printStackTrace(System.err);
					this.sendDiagnosticEmbed("Error while processing your code. Please submit a bug report and give this error:\n\n`" + e.toString() + "`", channel);
					return;
				}
				Path compiledSketch = this.compileCode(instanceID, sketchPath);
				
				URL classURL = null;
				try {
					classURL = compiledSketch.getParent().toUri().toURL();
				} catch (MalformedURLException e) {
					System.err.println("Failed to convert path to URL!");
					e.printStackTrace(System.err);
					this.sendDiagnosticEmbed("Error while processing your code. Please submit a bug report and give this error:\n\n`" + e.toString() + "`", channel);
					// delete temp resources
					sketchPath.toFile().delete();
					compiledSketch.toFile().delete();
					return;
				}
				
				Class<?> sketchClass = null;
				try {
					URLClassLoader classloader = URLClassLoader.newInstance(new URL[] { classURL });
					sketchClass = Class.forName("Sketch_" + instanceID, true, classloader);
				} catch (ClassNotFoundException e) {
					System.err.println("Failed to load sketch class!");
					e.printStackTrace(System.err);
					this.sendDiagnosticEmbed("Error while processing your code. Please submit a bug report and give this error:\n\n`" + e.toString() + "`", channel);
					// delete temp resources
					sketchPath.toFile().delete();
					compiledSketch.toFile().delete();
					return;
				}
				
				// delete temporary resources
				sketchPath.toFile().delete();
				compiledSketch.toFile().delete();
				
				if(PApplet.class.isAssignableFrom(sketchClass)) {
					@SuppressWarnings("unchecked")
					Class<? extends PApplet> clazz = (Class<? extends PApplet>)sketchClass;
					try {
						PApplet sketch = clazz.newInstance();
						PGraphics graphics = new PGraphicsJava2D();
						sketch.g = graphics;
						sketch.setup();
						ImageIO.write((BufferedImage)graphics.getImage(), "PNG", new File("test.png"));
					} catch (InstantiationException | IllegalAccessException | IOException e) {
						System.err.println("Error occurred while running sketch!");
						e.printStackTrace(System.err);
						this.sendDiagnosticEmbed("There was an error running your sketch.\n\n`" + e.toString() + "`", channel);
					}
				} else {
					this.sendDiagnosticEmbed("Error while processing your code. Please submit a bug report and give this error:\n\n`Sketch class was not subclass of PApplet`", channel);
				}
			}
		};
		runner.start();
	}
	
	public SketchRunner(String name) {
		super(name);
	}
	
	protected Path compileCode(String instanceID, Path target) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, null, null, target.toFile().getAbsolutePath()); // TODO: log compilation errors
		return target.getParent().resolve("Sketch_" + instanceID + ".class");
	}
	
	protected void writeToPath(String data, Path path) throws IOException {
		FileOutputStream out = new FileOutputStream(path.toFile());
		out.write(data.getBytes(), 0, data.length());
		out.flush();
		out.close();
	}
	
	protected String convertCode(String instanceID, String code) {
		StringBuilder codeBuilder = new StringBuilder();
		
		// firstly, isolate the size() call
//		if(code.contains("size(")) {
//			int idx = code.indexOf("size(");
//			String call = 
//		}
		
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
		codeBuilder.append("\nnoLoop();\n");
		codeBuilder.append("}\n");
		// SETTINGS
		codeBuilder.append("public void settings() {}\n");
		// FINAL CODE
		codeBuilder.append("}");
		return codeBuilder.toString();
	}
	
	protected void sendDiagnosticEmbed(String error, MessageChannel channel) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Error running code", null, RequestHandler.LOGO);
		embed.setDescription(error);
		embed.setColor(Color.red);
		channel.sendMessage(embed.build()).queue();
	}
	
}