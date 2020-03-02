package com.processingbot.processing;

import java.io.PrintStream;

import processing.core.PApplet;

public class PAppletBot extends PApplet {

	private static final int MAX_WIDTH = 2000;
	private static final int MAX_HEIGHT = 2000;

	private PrintStream outputStream;
	private PrintStream errorStream;

	private void functionUnsupported(String method) {
		errorStream.printf("Function %s() not supported by ProcessingBot\n", method);
	}

	public void setPrintStreams(PrintStream outputStream, PrintStream errorStream) {
		this.outputStream = outputStream;
		this.errorStream = errorStream;
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

}