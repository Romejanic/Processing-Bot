package com.processingbot.processing;

import java.awt.Frame;
import java.io.File;
import java.io.PrintStream;
import java.util.function.Consumer;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.data.StringList;

public class PAppletBot extends PApplet {

	private static final int MAX_WIDTH = 2000;
	private static final int MAX_HEIGHT = 2000;

	private static PrintStream outputStream;
	private static PrintStream errorStream;
	private Consumer<Long> exitListener;

	private void functionUnsupported(String method) {
		errorStream.printf("Function %s() is not supported by ProcessingBot\n", method);
	}

	public void setPrintStreams(PrintStream outputStream, PrintStream errorStream) {
		PAppletBot.outputStream = outputStream;
		PAppletBot.errorStream = errorStream;
	}

	public void setExitListener(Consumer<Long> exitListener) {
		this.exitListener = exitListener;
	}

	static void warning(String text) {
		errorStream.println(text);
	}

	//---------------------------------------------------------------------------

	@Override
	public void setup() {}

	@Override
	public final void size(int width, int height) {
		boolean flag = false;
		if(width > MAX_WIDTH) {
			width = MAX_WIDTH;
			flag = true;
		}
		if(height > MAX_HEIGHT) {
			height = MAX_HEIGHT;
			flag = true;
		}
		this.width = width;
		this.height = height;
		if(flag) {
			errorStream.printf("For the sake of performance, the largest size supported by this bot is %dx%d\n", MAX_WIDTH, MAX_HEIGHT);
		}
	}

	@Override
	public final void size(int width, int height, String renderer) {
		this.size(width, height);
		warning("Warning: changing the renderer is not supported at this time");
	}

	public final void setSize(int width, int height) {
		this.functionUnsupported("setSize");
	}

	public final void start() {
		this.functionUnsupported("start");
	}

	public final void stop() {
		this.functionUnsupported("stop");
	}

	@Override
	public final void exit() {
		exit(0);
	}

	@Override
	public final void exitActual() {
		this.functionUnsupported("exitActual");
	}

	public final void exit(int code) {
		if(code != 0) errorStream.println("Sketch exited with code " + code);
		this.exitCalled = true;
		if(this.exitListener != null) this.exitListener.accept(-1l);
	}

	static public void selectImpl(final String prompt,
			final String callbackMethod,
			final File defaultSelection,
			final Object callbackObject,
			final Frame parentFrame,
			final int mode,
			final PApplet sketch) {
		warning("Opening dialogs is not supported by ProcessingBot.");
	}

	@Override
	public final void dispose() {
		this.functionUnsupported("dispose");
	}

	@Override
	public void link(String url) {
		this.functionUnsupported("link");
	}

	static public Process launch(String... args) {
		warning("You cannot launch external processes on ProcessingBot for security reasons.");
		return null;
	}

	static public Process exec(String... args) {
		warning("You cannot launch external processes on ProcessingBot for security reasons.");
		return null;
	}

	static public int exec(StringList stdout, StringList stderr, String... args) {
		warning("You cannot launch external processes on ProcessingBot for security reasons.");
		return -1;
	}

	static public int shell(StringList stdout, StringList stderr, String... args) {
		warning("You cannot launch external processes on ProcessingBot for security reasons.");
		return -1;
	}

	@Override
	public final PGraphics beginRecord(String renderer, String filename) {
		this.functionUnsupported("beginRecord");
		return g;
	}

	@Override
	public final void beginRecord(PGraphics recorder) {
		this.functionUnsupported("beginRecord");
	}

	@Override
	public final void endRecord() {
		this.functionUnsupported("endRecord");
	}

	@Override
	public final void frameMoved(int x, int y) {
		errorStream.println(EXTERNAL_MOVE + " " + x + " " + y);
		errorStream.flush();  // doesn't seem to help or hurt
	}

	static public void runSketch(final String[] args, final PApplet constructedSketch) {
		warning("runSketch() is unsupported.");
	}

	// prepare for print hell
	static public void print(byte what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(boolean what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(char what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(int what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(long what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(float what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(double what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(String what) {
		outputStream.print(what);
		outputStream.flush();
	}

	static public void print(Object... variables) {
		StringBuilder sb = new StringBuilder();
		for (Object o : variables) {
			if (sb.length() != 0) {
				sb.append(" ");
			}
			if (o == null) {
				sb.append("null");
			} else {
				sb.append(o.toString());
			}
		}
		outputStream.print(sb.toString());
	}

	static public void println() {
		outputStream.println();
	}

	static public void println(byte what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(boolean what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(char what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(int what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(long what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(float what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(double what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(String what) {
		outputStream.println(what);
		outputStream.flush();
	}

	static public void println(Object... variables) {
		print(variables);
		println();
	}

	static public void println(Object what) {
		if (what == null) {
			outputStream.println("null");
		} else if (what.getClass().isArray()) {
			printArray(what);
		} else {
			outputStream.println(what.toString());
			outputStream.flush();
		}
	}

	static public void printArray(Object what) {
		if (what == null) {
			// special case since this does fuggly things on > 1.1
			outputStream.println("null");

		} else {
			String name = what.getClass().getName();
			if (name.charAt(0) == '[') {
				switch (name.charAt(1)) {
				case '[':
					// don't even mess with multi-dimensional arrays (case '[')
					// or anything else that's not int, float, boolean, char
					outputStream.println(what);
					break;

				case 'L':
					// print a 1D array of objects as individual elements
					Object poo[] = (Object[]) what;
					for (int i = 0; i < poo.length; i++) {
						if (poo[i] instanceof String) {
							outputStream.println("[" + i + "] \"" + poo[i] + "\"");
						} else {
							outputStream.println("[" + i + "] " + poo[i]);
						}
					}
					break;

				case 'Z':  // boolean
					boolean zz[] = (boolean[]) what;
					for (int i = 0; i < zz.length; i++) {
						outputStream.println("[" + i + "] " + zz[i]);
					}
					break;

				case 'B':  // byte
					byte bb[] = (byte[]) what;
					for (int i = 0; i < bb.length; i++) {
						outputStream.println("[" + i + "] " + bb[i]);
					}
					break;

				case 'C':  // char
					char cc[] = (char[]) what;
					for (int i = 0; i < cc.length; i++) {
						outputStream.println("[" + i + "] '" + cc[i] + "'");
					}
					break;

				case 'I':  // int
					int ii[] = (int[]) what;
					for (int i = 0; i < ii.length; i++) {
						outputStream.println("[" + i + "] " + ii[i]);
					}
					break;

				case 'J':  // int
					long jj[] = (long[]) what;
					for (int i = 0; i < jj.length; i++) {
						outputStream.println("[" + i + "] " + jj[i]);
					}
					break;

				case 'F':  // float
					float ff[] = (float[]) what;
					for (int i = 0; i < ff.length; i++) {
						outputStream.println("[" + i + "] " + ff[i]);
					}
					break;

				case 'D':  // double
					double dd[] = (double[]) what;
					for (int i = 0; i < dd.length; i++) {
						outputStream.println("[" + i + "] " + dd[i]);
					}
					break;

				default:
					outputStream.println(what);
				}
			} else {  // not an array
				outputStream.println(what);
			}
		}
		outputStream.flush();
	}

	static public void debug(String msg) {
		println("[DEBUG] " + msg);
	}

}