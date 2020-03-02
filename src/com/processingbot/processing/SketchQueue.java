package com.processingbot.processing;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.processingbot.security.SketchSecurityManager;

import net.dv8tion.jda.api.entities.MessageChannel;

public class SketchQueue extends Thread {

	private volatile boolean running = true;
	private final Queue<SketchData> sketchQueue = new LinkedBlockingQueue<SketchData>();
	
	private final SketchSecurityManager security;
	
	public SketchQueue() {
		this.security = new SketchSecurityManager();
	}
	
	public FutureSketch enqueueSketch(String code, String author, MessageChannel channel, Consumer<Boolean> callback) {
		FutureSketch future = new FutureSketch(callback);
		this.sketchQueue.offer(new SketchData(code, author, channel, future));
		return future;
	}
	
	@Override
	public void run() {
		while(this.running) {
			SketchData data = this.sketchQueue.poll();
			if(data != null) {
//				this.security.enable();
				SketchRunner.runCode(data.code, data.author, data.channel, data.future);
//				this.security.disable();
			}
		}
	}
	
	public static class FutureSketch implements Future<Boolean> {
		
		private Consumer<Boolean> callback;
		private boolean done = false;
		private boolean result = false;
		
		public FutureSketch(Consumer<Boolean> callback) {
			this.callback = callback;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return this.done;
		}

		@Override
		public Boolean get() throws InterruptedException, ExecutionException {
			return this.result;
		}

		@Override
		public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return this.result;
		}
		
		protected void complete(Boolean result) {
			this.done = true;
			this.result = (boolean)result;
			if(this.callback != null) {
				this.callback.accept(result);
			}
		}
		
	}
	
}