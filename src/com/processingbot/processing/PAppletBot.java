package com.processingbot.processing;

import java.io.PrintStream;

import processing.core.PApplet;

public class PAppletBot extends PApplet {

	private PrintStream errorStream;
	
	private void functionUnsupported(String method) {
		errorStream.printf("Function %s() not supported by ProcessingBot\n", method);
	}
	
	public void setErrorStream(PrintStream errorStream) {
		this.errorStream = errorStream;
	}

	//---------------------------------------------------------------------------

	@Override
	public void setup() {}

	@Override
	public final void size(int width, int height) {
		if(width > 2000) {
			
		}
		this.width = width;
		this.height = height;
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