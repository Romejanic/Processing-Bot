package com.processingbot.main;

import javax.security.auth.login.LoginException;

import com.processingbot.request.RequestHandler;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ProcessingBot extends ListenerAdapter {

	private final RequestHandler requestHandler = new RequestHandler();
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String messageText = event.getMessage().getContentRaw().trim();
		boolean isPM = event.getChannelType() == ChannelType.PRIVATE;
		boolean prefixed = messageText.toLowerCase().startsWith("!processing");
		
		if(event.getAuthor().isBot()) return;
		if(!prefixed && !isPM) return;
		if(isPM && prefixed) {
			event.getChannel().sendMessage("Psst, you don't need the `!processing` prefix in private messages! Try sending your command without it.").queue();
			return;
		}
		
		String[] args;
		if(!isPM) {
			if(messageText.length() == "!processing".length()) {
				args = new String[0];
			} else {
				args = messageText
						.substring("!processing ".length())
						.split(" ");
			}
		} else {
			args = messageText.split(" ");
		}
		
		this.requestHandler.process(args, event.getMessage(), event.getChannel());
	}
	
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
	
}