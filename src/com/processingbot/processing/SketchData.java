package com.processingbot.processing;

import com.processingbot.processing.SketchQueue.FutureSketch;

import net.dv8tion.jda.api.entities.MessageChannel;

public class SketchData {

	public final String code;
	public final String author;
	public final MessageChannel channel;
	public final FutureSketch future;
	
	public SketchData(String code, String author, MessageChannel channel, FutureSketch future) {
		this.code = code;
		this.author = author;
		this.channel = channel;
		this.future = future;
	}
	
}