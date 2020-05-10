import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public class LupoOneBot {
    private static DiscordApi api;
    private static Server currentServer;
    private static final Logger logger = LogManager.getLogger(LupoOneBot.class.getName());
    //TODO Possibly Fix all the Loggers to get them to have important information.
    public static void main(String[] args) {
        api = new DiscordApiBuilder().setToken(args[0]).login().join();
        //Builds the bot and logs in to the account.

        FallbackLoggerConfiguration.setDebug(true);
        //Sets the logger to look for bugs with full details on messages and users.
        logger.info("Bot Started Up!");
        //Listens for a command from any User.
        //This command is only for the !help Command.
        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase("!help")) {
                //long helpID = event.getMessageId();
                try {
                    logger.info("Setting current Server");
                    currentServer = event.getMessage().getServer().orElseThrow();
                    logger.info("Finished Setting current Server");
                } catch(NoSuchElementException nsee) { logger.error("Server Not Saved: ", nsee); }
                logger.info("Received Command from: " + event.getMessage().getAuthor().getDiscriminatedName());
                User helpUser = null;
                if (event.getMessage().getAuthor().asUser().isPresent()) {
                    helpUser = event.getMessage().getAuthor().asUser().get();
                }
                try {
                    logger.info("Opening Channel with : " + event.getMessage().getAuthor().getDiscriminatedName());
                    //I don't know what these next two lines are, the IDE made me apply them for exceptions and possible problems.
                    User finalHelpUser = helpUser;
                    assert helpUser != null;
                    logger.info("The two weird Lines finished running.");
                    finalHelpUser.openPrivateChannel()
                            .thenAcceptAsync(channel -> {
                                MessageBuilder messageBuilder = new MessageBuilder();
                                        try {
                                            //First try with the Message builder and it looks promising.
                                            messageBuilder.getStringBuilder()
                                                    .append(Files.readString(Paths.get("src/main/resources/!help.txt"), StandardCharsets.UTF_8));
                                        } catch(IOException ioe) { logger.error("Making the Help Message " + ioe); }
                                        messageBuilder.send(finalHelpUser);
                                logger.info("Commands Message Sent to " + finalHelpUser.getDiscriminatedName());
                            });
                } catch(NullPointerException npe) { logger.error("Channel may not have been opened " , npe.getCause()); }
            }
        });
        //TODO Possibly listen to a !setup Command
        //adds a list of listeners that do different things

        api.addServerMemberJoinListener(new BotFunctions());
        //Listens to when a new member joins the server

        api.addMessageCreateListener(new AdminFunctions());
        //Listens to messages for commands (Specifically for Admins)

        api.addMessageDeleteListener(new AdminFunctions());
        //Listens to when a message is deleted (Specifically for Admins)

        api.addServerVoiceChannelMemberJoinListener(new VoiceChannelFunctions());
        //listens when a member joins any voice channel.
    }
    //Getters for Private Variables
    public DiscordApi apiGetter() { return api; }
    public Server serverGetter() { return currentServer; }
    public Logger loggerGetter() { return logger; }
}
