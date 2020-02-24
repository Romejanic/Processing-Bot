package com.processingbot.request;

import net.dv8tion.jda.api.entities.MessageChannel;

public class RequestHandler {

	public void process(String[] args, MessageChannel channel) {
		if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
			printHelp(channel);
		}
	}
	
	private void printHelp(MessageChannel channel) {
		channel.sendMessage("help is on it's way!").queue();
	}	
	
}