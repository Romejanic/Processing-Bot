package com.processingbot.processing;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.processingbot.processing.SketchQueue.FutureSketch;
import com.processingbot.request.RequestHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import processing.awt.PGraphicsJava2D;
import processing.core.PGraphics;

public class SketchRunner extends Thread {
	
	private static String lastCode = "";

	public static void runCode(String code, String sender, MessageChannel channel, FutureSketch future) {
		final String instanceID = UUID.randomUUID().toString().replaceAll("-", "");
		final String tempDir = System.getProperty("java.io.tmpdir");
		SketchRunner runner = new SketchRunner("Run Sketch " + instanceID) {
			public void run() {
				// first check we can actually run it
				if(code.contains("setup()") || code.contains("draw()")) {
					this.sendDiagnosticEmbed("Sketches run by ProcessingBot must be **static**. You cannot use `setup()` or `draw()`.", channel);
					future.complete(false);
					return;
				}
				
				String sketchCode = this.convertCode(instanceID, code);
				lastCode = sketchCode;
				Path sketchPath = Paths.get(tempDir, "Sketch_" + instanceID + ".java");
				try {
					this.writeToPath(sketchCode, sketchPath);
				} catch (IOException e) {
					System.err.println("Failed to write to file!");
					e.printStackTrace(System.err);
					this.sendDiagnosticEmbed("Error while processing your code. Please submit a bug report and give this error:\n\n`" + e.toString() + "`", channel);
					future.complete(false);
					return;
				}
				CompileResult compileResult = this.compileCode(instanceID, sketchPath);
				if(!compileResult.success) {
					sketchPath.toFile().delete();
					this.sendDiagnosticEmbed("Failed to compile sketch!\nExit code: `"
						+ compileResult.exitCode + "`\n\n```\n" + compileResult.stderr.replace(tempDir, "")
						+ "```", channel);
					future.complete(false);
					return;
				}
				
				Path compiledSketch = compileResult.outputPath;
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
					future.complete(false);
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
					future.complete(false);
					return;
				}

				// delete temporary resources
				sketchPath.toFile().delete();
				compiledSketch.toFile().delete();

				if(PAppletBot.class.isAssignableFrom(sketchClass)) {
					@SuppressWarnings("unchecked")
					Class<? extends PAppletBot> clazz = (Class<? extends PAppletBot>)sketchClass;
					try {
						PAppletBot sketch = clazz.newInstance();
						PGraphics graphics = new PGraphicsJava2D();

						ByteArrayOutputStream sysOut   = new ByteArrayOutputStream();
						ByteArrayOutputStream errorOut = new ByteArrayOutputStream();
						sketch.setPrintStreams(new PrintStream(sysOut), new PrintStream(errorOut));
						
						Consumer<Long> onComplete = (elapsed) -> {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							try {
								ImageIO.write((RenderedImage)graphics.getImage(), "PNG", out);
								sendRunEmbed(out.toByteArray(), sysOut.toString(), errorOut.toString(), sender, instanceID, elapsed, channel);
								future.complete(true);
							} catch (IOException e) {
								System.err.println("Failed to convert image to bytes!");
								e.printStackTrace();
								this.sendDiagnosticEmbed("There was an error sending the output image.\n\n`" + e.toString() + "`", channel);
								future.complete(false);
							}
						};
						sketch.setExitListener(onComplete);

						sketch.settings();
						graphics.setSize(sketch.width, sketch.height);
						graphics.setParent(sketch);
						sketch.g = graphics;
						graphics.beginDraw();
						graphics.background(128f);
						
						final Future<Long> f = Executors.newSingleThreadExecutor().submit(() -> {
							long start = System.currentTimeMillis();
							sketch.setup();
							return System.currentTimeMillis() - start;
						});
						long elapsed = f.get(15, TimeUnit.SECONDS); // restrict to 15 seconds
						
						if(!sketch.exitCalled()) {
							graphics.endDraw();
							onComplete.accept(elapsed);
						}
					} catch (InstantiationException | IllegalAccessException | InterruptedException | ExecutionException e) {
						this.sendDiagnosticEmbed("There was an error running your sketch.\n\n`" + e.toString() + "`", channel);
						future.complete(false);
					} catch (TimeoutException e) {
						this.sendDiagnosticEmbed("Your sketch took longer than 15 seconds to run.", channel);
						future.complete(false);
					}
				} else {
					this.sendDiagnosticEmbed("Error while processing your code. Please submit a bug report and give this error:\n\n`Sketch class was not subclass of PApplet`", channel);
					future.complete(false);
				}
			}
		};
		runner.start();
	}

	public SketchRunner(String name) {
		super(name);
	}

	protected CompileResult compileCode(String instanceID, Path target) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		int result = compiler.run(null, null, stderr, target.toFile().getAbsolutePath());
		if(result != 0) {
			return new CompileResult(result, stderr.toString());
		} else {
			return new CompileResult(target.getParent().resolve("Sketch_" + instanceID + ".class"));
		}
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
		String sizeCall = null;
		if(code.contains("size(")) {
			int idx = code.indexOf("size(");
			sizeCall = code.substring(idx);
			int endIdx = sizeCall.indexOf(')');
			sizeCall = sizeCall.substring(0, endIdx + 1);
			code = code.substring(0, idx) + code.substring(idx + endIdx + 1);
		}

		// make sure that System.exit() calls are replaced with exit()
		if(code.contains("System.exit(")) {
			// get rid of 'System.' so the 'exit()' part stays
			code = code.replaceAll("System.", "warning(\"You cannot use System.exit() in sketches. Use exit() instead.\");");
		}
		// make exit() calls end the program by adding a return statement
		while(code.contains("exit()")) {
			
		}

		// IMPORTS
		codeBuilder.append("import processing.core.*;\n");
		codeBuilder.append("import processing.data.*;\n");
		codeBuilder.append("import processing.event.*;\n");
		codeBuilder.append("import java.util.HashMap;\n");
		codeBuilder.append("import java.util.ArrayList;\n");
//		-----SANDBOXING-----
//		codeBuilder.append("import java.io.File;\n");
//		codeBuilder.append("import java.io.BufferedReader;\n");
//		codeBuilder.append("import java.io.PrintWriter;\n");
//		codeBuilder.append("import java.io.InputStream;\n");
//		codeBuilder.append("import java.io.OutputStream;\n");
		codeBuilder.append("import java.io.IOException;\n");
		codeBuilder.append("import com.processingbot.processing.PAppletBot;\n");
		// CLASS HEADER
		codeBuilder.append("public class Sketch_");
		codeBuilder.append(instanceID);
		codeBuilder.append(" extends PAppletBot {\n");
		// SETUP()
		codeBuilder.append("public void setup() {\n");
		codeBuilder.append(code);
		codeBuilder.append("\nnoLoop();\n");
		codeBuilder.append("}\n");
		// SETTINGS
		codeBuilder.append("public void settings() {");
		if(sizeCall != null) { codeBuilder.append(sizeCall); }
		codeBuilder.append("; }\n");
		// FINAL CODE
		codeBuilder.append("}");
		return codeBuilder.toString();
	}

	protected void sendDiagnosticEmbed(String error, MessageChannel channel) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Error running code", null, RequestHandler.LOGO);
		embed.setDescription(error);
		embed.setColor(Color.red);
		channel.sendMessage(embed.build()).queue(msg -> {
			msg.addReaction("❌").queue();
		});
	}

	protected void sendRunEmbed(byte[] image, String stdout, String stderr, String sender, String id, long elapsed, MessageChannel channel) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Sketch", null, RequestHandler.LOGO);
		embed.setColor(Color.cyan);
		embed.setFooter("Requested by " + sender);
		if(elapsed >= 0) {
			String secs = String.valueOf((float)elapsed / 1000f);
			embed.addField("Time Taken", secs + "s", false);
		}
		if(stdout != null && !stdout.isEmpty()) {
			embed.addField("Output", "```\n" + restrictLength(stdout,1000) + "```", false);
		}
		if(stderr != null && !stderr.isEmpty()) {
			embed.addField("Errors", "```\n" + restrictLength(stderr,1000) + "```", false);
		}
		channel.sendFile(image, id + ".png").queue();
		channel.sendMessage(embed.build()).queue(msg -> {
			msg.addReaction("✅").queue();
		});
	}
	
	private String restrictLength(String str, int length) {
		if(str.length() < length) return str;
		return str.substring(0, length - 4) + "...";
	}

	public static String getLastConvertedCode() {
		return lastCode;
	}
	
}