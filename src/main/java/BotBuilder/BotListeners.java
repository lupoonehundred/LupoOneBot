package BotBuilder;

import BotAudio.VoiceChannelFunctions;
import BotCommands.AdminFunctions;
import BotCommands.BotFunctions;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public class BotListeners extends LupoOneBot{
    private static Server currentServer;

    public void mainListener() {
        //Listens for a command from any User.
        apiGetter().addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase("!help")) {
                //long helpID = event.getMessageId();
                try {
                    loggerGetter().info("Setting current Server");
                    currentServer = event.getMessage().getServer().orElseThrow();
                    loggerGetter().info("Finished Setting current Server");
                } catch(NoSuchElementException nsee) { loggerGetter().error("Server Not Saved: ", nsee); }
                loggerGetter().info("Received Command from: " + event.getMessage().getAuthor().getDiscriminatedName());
                User helpUser = null;
                if (event.getMessage().getAuthor().asUser().isPresent()) {
                    helpUser = event.getMessage().getAuthor().asUser().get();
                }
                try {
                    loggerGetter().info("Opening Channel with : " + event.getMessage().getAuthor().getDiscriminatedName());
                    //I don't know what these next two lines are, the IDE made me apply them for exceptions and possible problems.
                    User finalHelpUser = helpUser;
                    assert helpUser != null;
                    loggerGetter().info("The two weird Lines finished running.");
                    finalHelpUser.openPrivateChannel()
                            .thenAcceptAsync(channel -> {
                                MessageBuilder messageBuilder = new MessageBuilder();
                                try {
                                    //First try with the Message builder and it looks promising.
                                    messageBuilder.getStringBuilder()
                                            .append(Files.readString(Paths.get("src/main/resources/!help.txt"), StandardCharsets.UTF_8));
                                } catch(IOException ioe) { loggerGetter().error("Making the Help Message " + ioe); }
                                messageBuilder.send(finalHelpUser);
                                loggerGetter().info("Commands Message Sent to " + finalHelpUser.getDiscriminatedName());
                            });
                } catch(NullPointerException npe) { loggerGetter().error("Channel may not have been opened " , npe.getCause()); }
            }
        });
        //This command is only for the !help Command.

        apiGetter().addMessageCreateListener(setupEvent -> {
            if(setupEvent.getMessage().getContent().equalsIgnoreCase("!setup"))
                new BotSetup(apiGetter(), setupEvent.getMessage().getUserAuthor().get());
        });
        //adds a list of listeners that do different things

        apiGetter().addMessageCreateListener(event -> {
            if (event.getMessage().getContent().contains("!request")
                    && event.getChannel().asServerVoiceChannel().get().getConnectedUsers().size() <= 1)
                event.getChannel().sendMessage("Please enter a voice channel to use the commands.");
        });
        //if the !request is used without anyone in it.

        apiGetter().addServerMemberJoinListener(new BotFunctions());
        //Listens to when a new member joins the server

        //apiGetter().addMessageCreateListener(new BotCommands.AdminFunctions());
        //Listens to messages for commands (Specifically for Admins)

        apiGetter().addMessageDeleteListener(new AdminFunctions());
        //Listens to when a message is deleted (Specifically for Admins)

        apiGetter().addServerVoiceChannelMemberJoinListener(new VoiceChannelFunctions());
        //listens when a member joins any voice channel.
    }
    public Server serverGetter() { return currentServer; } //sometimes doesn't pull save a server.
}
