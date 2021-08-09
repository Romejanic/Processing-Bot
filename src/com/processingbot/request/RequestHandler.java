package com.processingbot.request;

import java.awt.Color;

import com.processingbot.main.ProcessingBot;
import com.processingbot.processing.SketchQueue;
import com.processingbot.processing.SketchRunner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class RequestHandler {
	
	public static final String VERSION = "0.1.0 Beta";

	public static final String LOGO = "https://cdn.discordapp.com/avatars/681415272187559959/b102e6a2c285d43bb4732d3f90a62673.png?size=64";
	private static final String GITHUB = "[GitHub](https://github.com/Romejanic/Processing-Bot)";
	private static final String SERVER = "[Support Server](https://discord.gg/WNCKCaF)";
	private static final String FOOTER = "Made with ❤️ by @memedealer#6607";
	private final SketchQueue queue = new SketchQueue();

	public RequestHandler() {
		this.queue.start();
	}

	public void process(String[] args, Message msg, MessageChannel channel) {
		if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
			printHelp(channel);
		} else if(args[0].startsWith("```") && args[args.length-1].endsWith("```")) {
			String code = join(args);
			int startIdx = code.indexOf('\n');
			int endIdx = code.length() - 4;
			code = code.substring(startIdx, endIdx);

			msg.addReaction("🤔").queue();
			this.queue.enqueueSketch(code, msg.getAuthor(), channel, (success) -> {
				msg.removeReaction("🤔").queue();
			});
		} else {
			switch(args[0]) {
			case "codehelp":
				printCodeHelp(channel);
				break;
			case "botinfo":
				printBotInfo(channel);
				break;
			case "debug.getConvertedCode":
				if(!ProcessingBot.isProduction()) {
					if(SketchRunner.getLastConvertedCode().isEmpty()) return;
					channel.sendMessage("```java\n" + SketchRunner.getLastConvertedCode() + "\n```").queue();
					break;
				}
			default:
				String prefix = channel.getType() == ChannelType.PRIVATE ? "" : "!processing ";
				channel.sendMessage("Oops, I don't recognize the command `" + args[0] + "`. Type `" + prefix + "help` for a list of commands.").queue();
				break;
			}
		}
	}

	private void printHelp(MessageChannel channel) {
		boolean isPM = channel.getType() == ChannelType.PRIVATE;
		String commandPrefix = !isPM ? "!processing " : "";

		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Processing Bot Help", null, LOGO);
		// START DESCRIPTION BUILDING
		StringBuilder desc = embed.getDescriptionBuilder();
		desc.append("You can execute these commands by typing `");
		desc.append(commandPrefix);
		desc.append("<command>`.\nTo run code, ");
		if(!isPM) {
			desc.append("type `").append(commandPrefix.trim()).append("` and ");
		}
		desc.append("write your code into a code block");
		if(!isPM) {
			desc.append(" on the line underneath");
		}
		desc.append(". Type `");
		desc.append(commandPrefix);
		desc.append("codehelp` for an example.");
		// START COMMAND LIST
		embed.addField("botinfo", "Some information and helpful links about this bot", false);
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
		example += "fill(255,0,0);\n";
		example += "rect(10,10,25,25);\n";
		example += "```\n\n";

		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Code Example", null, LOGO);
		embed.setDescription("The command you would send to produce the image shown is:\n\n" + example);
		embed.setThumbnail("https://cdn.discordapp.com/attachments/681405131199479808/681455709258514462/test.png");
		sendEmbed(embed, channel);
	}

	private void printBotInfo(MessageChannel channel) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Bot Info", null, LOGO);
		// ADD LINKS
		StringBuilder builder = embed.getDescriptionBuilder();
		builder.append(GITHUB).append('\n');
		builder.append(SERVER).append('\n');
		// ADD FIELDS
		Runtime r = Runtime.getRuntime();
		embed.addField("Bot Version", VERSION, true);
		embed.addField("Processing Version", "3.3.6", true);
		embed.addField("Memory Usage", (100-(r.freeMemory()*100/r.totalMemory())) + "%", true);
		embed.addField("Java Version", System.getProperty("java.version"), true);
		embed.addField("Operating System", System.getProperty("os.name"), true);
		embed.addField("Build Environment", ProcessingBot.getDevEnvironment(), true);
		// END LINKS
		sendEmbed(embed, channel);
	}

	private void sendEmbed(EmbedBuilder builder, MessageChannel channel) {
		builder.setFooter(FOOTER);
		builder.setColor(Color.cyan);
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

	private String join(String[] arr) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < arr.length; i++) {
			builder.append(arr[i]);
			if(i < arr.length-1) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}

}