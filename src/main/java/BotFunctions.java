import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.channel.user.PrivateChannelCreateListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

public class BotFunctions extends LupoOneBot implements ServerMemberJoinListener, PrivateChannelCreateListener {
    //FIXME Ultimately Optimize this section in total
    private Server serverJoined;
    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) { //When a new member joins the
        serverJoined = event.getServer();
        loggerGetter().info("New member has Joined the Server: " +
                event.getServer().getName() + " " +
                event.getUser().getDiscriminatedName());
        //Logs the name and server the user has joined.
        //FIXME Fix how the verification process works

        event.getUser().openPrivateChannel()
                .thenAcceptAsync(channel -> {
                    channel.sendMessage("Please Verify Who You Are. \nReply With your Current Display Name. (Case Sensitive)\nYou have 30 secs.");
                    loggerGetter().info("sent Verification to User: " + event.getUser().getDiscriminatedName());
                });
        //Gets the user and asks for verification
        //TODO add an Exceptionally catcher
        loggerGetter().info("Opening a private channel with User: " + event.getUser().getDiscriminatedName());
        apiGetter().addPrivateChannelCreateListener(new BotFunctions());
        //Logs and opens a new Private Channel with the User.
    }

    @Override
    public void onPrivateChannelCreate(PrivateChannelCreateEvent event) {
        if(serverJoined.getOwner().getDiscriminatedName().equals(event.getUser().getDiscriminatedName())) { return;}
        try {
            loggerGetter().info("Sleep Started.");
            Thread.sleep(30000);
            loggerGetter().info("Sleep Ended.");
            //Sleeps to receive the Users response to the prompt.
        }
        catch (Exception e) {
            loggerGetter().error("Caught Sleep execution: " , e.getCause());
            //Logs if the Sleep was disturbed
        }
        //Sleep Try and Catch
        event.getChannel()
            .getMessages(0)
            .thenAcceptAsync(channel -> {
                String verify = channel.getNewestMessage().orElseThrow().getContent();
                loggerGetter().info("Starting the Verification for the User " + event.getUser().getName());
                //The Change from .getDiscriminatedName to .getName might work
                int tries = 0;
                //User will only have three tries to write their response.
                while(tries < 3) {
                    if(event.getUser().getName().equals(verify)) {
                        //event.getUser().addRole(vUser, "Verified User");
                        event.getChannel().sendMessage("You have been Verified and can use the server." + serverJoined.getName());
                        loggerGetter().info("User " + event.getUser().getDiscriminatedName() + " has been Verified.");
                        //Tells the User that they have been verified and also logged it as well.
                        break;
                    }
                    else {
                        event.getChannel().sendMessage("Please Redo Verification.");
                        loggerGetter().info("User " + event.getUser().getDiscriminatedName() + " has not been Verified.");
                        //Tells the User that they have not been verified and logs it.
                        tries++;
                        //Allow them to try again.
                    }
                    //FIXME Not correctly identifying their names.
                    loggerGetter().info("Retrying Verification for User: " + event.getUser().getDiscriminatedName());
                    try {
                        loggerGetter().info("Sleep Started.");
                        Thread.sleep(30000);
                        loggerGetter().info("Sleep Ended.");
                        //Sleeps to receive the Users response to the prompt.
                    } catch (Exception e) {
                        loggerGetter().error("Caught Sleep execution: " , e.getCause());
                        //Logs if the Sleep was disturbed
                    }
                    //place a try catch for them to retry the prompt.
                }
                User kUser = serverJoined.getMemberById(channel.getNewestMessage().get().getAuthor().getId()).get();
                loggerGetter().info("User to be Kicked received: " + kUser.getDiscriminatedName());
                serverJoined.kickUser(kUser);
                loggerGetter().info("User has been Kicked: " + kUser.getDiscriminatedName());
                event.getChannel().sendMessage("Verification failed and Removed from Server.");
            });
        //Send message to a User personally
        //TODO add an Exceptionally catcher
    }
}