package com.processingbot.request;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class RequestHandler {
	
	private static final String LOGO = "https://cdn.discordapp.com/avatars/681415272187559959/b102e6a2c285d43bb4732d3f90a62673.png?size=64";
	private static final String FOOTER = "[GitHub](https://github.com/Romejanic/Processing-Bot) [Support Server](https://discord.gg/WNCKCaF)";
	
	public void process(String[] args, MessageChannel channel) {
		if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
			printHelp(channel);
		} // TODO: add condition for code blocks
		else {
			switch(args[0]) {
			case "codehelp":
				printCodeHelp(channel);
				break;
			default:
				channel.sendMessage("Oops, I don't recognize the command `" + args[0] + "`. Type `!processing help` for a list of commands.").queue();
				break;
			}
		}
	}
	
	private void printHelp(MessageChannel channel) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Processing Bot Help", null, LOGO);
		embed.setDescription("You can execute these commands by typing `!processing <command>`.\nTo run code, type `!processing` and write your code into a code block on the line underneath.");
		embed.setColor(Color.cyan);
		// START COMMAND LIST
		embed.addField("help", "Show this message", false);
		embed.addField("codehelp", "See more detailed instructions on how to run code", false);
		// END COMMAND LIST
		sendEmbed(embed, channel);
	}
	
	private void printCodeHelp(MessageChannel channel) {
		String example = "";
		if(channel.getType() != ChannelType.PRIVATE) example += "!processing\n";
		example += "```processing\n";
		example += "size(50,50);\n";
		example += "fill(#ff0000);\n";
		example += "rect(10,10,25,25);\n";
		example += "```\n\n";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Code Example", null, LOGO);
		embed.setColor(Color.cyan);
		embed.setDescription("The command you would send to produce the image shown is:\n\n" + example);
		embed.setThumbnail("https://cdn.discordapp.com/attachments/681405131199479808/681455709258514462/test.png");
		sendEmbed(embed, channel);
	}
	
	private void sendEmbed(EmbedBuilder builder, MessageChannel channel) {
		builder.setFooter(FOOTER);
		channel.sendMessage(builder.build()).queue((msg) -> {}, (err) -> {
			if(err instanceof InsufficientPermissionException) {
				Permission perm = ((InsufficientPermissionException)err).getPermission();
				channel.sendMessage("Oops, I don't have permission to do that! Please give me the `" + perm.getName() + "` permission.");
			} else {
				System.err.print("Unexpected error sending message with embed: ");
				err.printStackTrace(System.err);
			}
		});
	}
	
}