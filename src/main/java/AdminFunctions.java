import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.concurrent.ExecutionException;

public class AdminFunctions extends LupoOneBot implements MessageCreateListener, MessageDeleteListener {
    //FIXME Test this section Completely
    @Override
    public void onMessageCreate(MessageCreateEvent event) { //Contains the Ban/Kick/Unban functions
        //FIXME Ultimately Optimize this section in total
        apiGetter().addMessageCreateListener(banEvent -> {
            if (banEvent.getMessage().getContent().contains("!ban") &&
                    banEvent.getMessageAuthor().isServerAdmin()) {
                loggerGetter().info("!ban received from an Admin "
                        + banEvent.getMessageAuthor().asUser().orElseThrow().getDiscriminatedName());
                User bUser = event.getServer().get().getMemberById(event.getMessageContent().split(" ")[1]).get();
                loggerGetter().info("User to be Banned received: " + bUser.getDiscriminatedName());
                event.getServer().get().kickUser(bUser);
                loggerGetter().info("User has been Banned: " + bUser.getDiscriminatedName());
            }
            else if(banEvent.getMessage().getContent().contains("!ban") &&
                    !banEvent.getMessageAuthor().isServerAdmin()) {
                loggerGetter().info("!ban received from a NonAdmin " + banEvent.getMessageAuthor().asUser().toString());
                User kUser = event.getServer().get().getMemberById(event.getMessage().getAuthor().getId()).get();
                loggerGetter().info("User to be Banned received: " + kUser.getDiscriminatedName());
                event.getServer().get().kickUser(kUser);
                loggerGetter().info("User has been Banned: " + kUser.getDiscriminatedName());
            }
        });
        //Listens to a users ban
        apiGetter().addMessageCreateListener(kickEvent -> {
            if (kickEvent.getMessage().getContent().contains("!kick") &&
                    kickEvent.getMessageAuthor().isServerAdmin()) {
                loggerGetter().info("!kick received from an Admin " + kickEvent.getMessageAuthor().asUser().toString());
                User kUser = event.getServer().get().getMemberById(event.getMessageContent().split(" ")[1]).get();
                loggerGetter().info("User to be Kicked received: " + kUser.getDiscriminatedName());
                event.getServer().get().kickUser(kUser);
                loggerGetter().info("User has been Kicked: " + kUser.getDiscriminatedName());
            }
            else if(kickEvent.getMessage().getContent().contains("!kick") &&
                    !kickEvent.getMessageAuthor().isServerAdmin()) {
                loggerGetter().info("!kick received from a NonAdmin " + kickEvent.getMessageAuthor().asUser().toString());
                User kUser = event.getServer().get().getMemberById(event.getMessage().getAuthor().getId()).get();
                loggerGetter().info("User to be Kicked received: " + kUser.getDiscriminatedName());
                event.getServer().get().kickUser(kUser);
                loggerGetter().info("User has been Kicked: " + kUser.getDiscriminatedName());
            }
        });
        //Listens to a users kick
        apiGetter().addMessageCreateListener(unbanEvent -> {
            if (unbanEvent.getMessage().getContent().contains("!unban") &&
                    unbanEvent.getMessageAuthor().isServerAdmin()) {
                loggerGetter().info("!unban received from an Admin " + unbanEvent.getMessageAuthor().asUser().toString());
                String id = unbanEvent.getMessageContent().substring(7);
                try {
                    User ubUser = apiGetter().getUserById(id).get();
                    serverGetter().unbanUser(ubUser);
                    //FIXME These Commands are not working properly
                    loggerGetter().info("User: " + ubUser.getDiscriminatedName() + " has been unbanned.");
                }
                catch ( InterruptedException | ExecutionException ie ) {
                    loggerGetter().error("Caught Unban User Exception: ", ie.getCause());
                    loggerGetter().error("ID received was " + id);
                }
            }
        });
        //Listens to a users unban
    }

    //Sends the Owner the User and Deleted Message.
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        loggerGetter().info("The Message from " + event.getMessage().orElseThrow().getAuthor().getDiscriminatedName() +
                " \"" + event.getMessageContent().orElseThrow() + "\" Has been Deleted!");
        loggerGetter().info("Opening a Private Channel with User Admin: " + event.getServer().orElseThrow().getOwner().getDiscriminatedName());
        event.getServer().get().getOwner().openPrivateChannel()
                .thenAcceptAsync(privateChannel -> privateChannel.sendMessage("User " + event.getMessage().get().getAuthor().getDiscriminatedName()
                + " wrote \"" + event.getMessageContent().get() + "\" and the message got deleted."))
                //Tries to send a message to the Owner
                .exceptionally(throwable -> {
                    loggerGetter().error("Sending Deleted Message to Owner Error " + throwable.getCause());
                    return null;
                })
                .exceptionally(ExceptionLogger.get());
                //Both Catch the exception being thrown.
                //TODO Find how and where this goes to.
    }
}
