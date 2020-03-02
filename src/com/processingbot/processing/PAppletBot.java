package com.processingbot.processing;

import processing.core.PApplet;

public class PAppletBot extends PApplet {

	private void functionUnsupported(String method) {
		System.err.printf("Function %s() not supported by ProcessingBot\n", method);
	}

	//---------------------------------------------------------------------------

	@Override
	public void setup() {}

	@Override
	public final void size(int width, int height) {
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