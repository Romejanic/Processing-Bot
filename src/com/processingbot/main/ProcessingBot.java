package com.processingbot.main;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ProcessingBot extends ListenerAdapter {

	public static void main(String[] args) {
		AuthSettings auth = new AuthSettings();
		if(!auth.loadSettings()) {
			return; // if the token isn't loaded, kill the program
		}
		
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(auth.botToken);
		builder.addEventListeners(new ProcessingBot());
		
		try {
			builder.build();
		} catch (LoginException e) {
			System.err.println("ERROR: Failed to connect to Discord!");
			System.err.print("Caused by ");
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) return;
		event.getChannel().sendMessage(
			"Hi " + event.getAuthor().getAsMention() + "! You said: `"
			+ event.getMessage().getContentStripped() + "`")
		.queue();
	}
	
}
