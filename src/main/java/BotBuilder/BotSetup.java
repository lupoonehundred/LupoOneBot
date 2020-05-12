package BotBuilder;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.server.ServerUpdater;

import java.util.concurrent.ExecutionException;

public class BotSetup extends LupoOneBot{
    private DiscordApi api;
    private Server updateServer;
    private User owner;

    public BotSetup(DiscordApi api, User owner) {
        this.api = api;
        this.owner = owner;
        //FIXME Maybe put this somewhere not in the constructor
        api.addMessageCreateListener(event -> {
            owner.openPrivateChannel().thenAcceptAsync(privateChannel -> {
                privateChannel.sendMessage("Do you me to setup your server or create a new one? (setup or create)");
                if (event.getMessage().getContent().equalsIgnoreCase("setup"))
                    setup();
                else if(event.getMessage().getContent().equalsIgnoreCase("create")) {
                    privateChannel.sendMessage("Respond with the name of the server");
                    String name = event.getMessage().getContent();
                    String response = "";
                    while(response.equalsIgnoreCase("no")) {
                        privateChannel.sendMessage("I received the name " + name + " as the Server Name, Correct?");
                        if (event.getMessage().getContent().equalsIgnoreCase("yes")) { break; }
                        else {
                            response = "no";
                            privateChannel.sendMessage("Type in a Server Name");
                            name = event.getMessage().getContent();
                        }
                    }
                    try {
                        createServer(name);
                    } catch (ExecutionException | InterruptedException ie) {
                        loggerGetter().error("Error Creating a server ", ie.getCause());
                    }
                }
                else {
                    privateChannel.sendMessage("Please respond with an answer or say exit.");
                    if (event.getMessage().getContent().equalsIgnoreCase("exit")) {}
                }
            });
        });
    }

    public BotSetup(DiscordApi api, Server server, User owner) {
        this(api, owner);
        updateServer = server;
    }

    public void setup() {
        updateServer.createTextChannelBuilder().setName("general")
                    .create();
        updateServer.createTextChannelBuilder().setName("music-channel")
                    .create();
        updateServer.createVoiceChannelBuilder().setName("General Voice")
                    .create();
        updateServer.createVoiceChannelBuilder().setName("Music Channel")
                    .create();
    }

    public void createServer(String serverName) throws ExecutionException, InterruptedException {
        long serverId = api.createServerBuilder().setName(serverName).create().get();
        updateServer = api.getServerById(serverId).get();
        setup();
        new ServerUpdater(updateServer)
                .setOwner(owner);
        owner.openPrivateChannel().thenAcceptAsync(channel -> {
            channel.sendMessage("Server has been created under the name " + serverName);
        });
    }
}
