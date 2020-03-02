package com.processingbot.processing;

import java.io.PrintStream;

import processing.core.PApplet;

public class PAppletBot extends PApplet {

	private static final int MAX_WIDTH = 2000;
	private static final int MAX_HEIGHT = 2000;

	private PrintStream outputStream;
	private PrintStream errorStream;
	private Runnable exitListener;

	private void functionUnsupported(String method) {
		errorStream.printf("Function %s() not supported by ProcessingBot\n", method);
	}

	public void setPrintStreams(PrintStream outputStream, PrintStream errorStream) {
		this.outputStream = outputStream;
		this.errorStream = errorStream;
	}
	
	public void setExitListener(Runnable exitListener) {
		this.exitListener = exitListener;
	}
	
	public void warning(String text) {
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
	public void exit() {
		exit(0);
	}
	
	public void exit(int code) {
		if(code != 0) errorStream.println("Sketch exited with code " + code);
		this.exitCalled = true;
		this.exitListener.run();
	}

}