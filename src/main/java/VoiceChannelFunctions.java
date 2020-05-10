import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.lavaplayerwrapper.youtube.YouTubeAudioSource;

public class VoiceChannelFunctions extends LupoOneBot implements ServerVoiceChannelMemberJoinListener{
    @Override
    public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
        loggerGetter().info("A User has joined a Voice Channel: " + event.getChannel().asVoiceChannel().toString());
        apiGetter().addServerVoiceChannelMemberLeaveListener(leaveEvent -> {
            leaveEvent.getServer().getAudioConnection().ifPresent(connection -> {
                if (connection.getChannel() == leaveEvent.getChannel()) {
                    if (leaveEvent.getChannel().getConnectedUsers().size() <= 1) {
                        connection.close();
                    }
                }
            });
        });
        //quick leave condition if no one else is in the given voice channel.

        /*  This method has a couple of IDE required security asserts.
            ".orElseThrow(AssertionError::new)" still learning what these are
            and how to use them correctly.
        */
        //TODO Possibly find a way to optimize this section.
        apiGetter().addMessageCreateListener(eventMusic -> {
            loggerGetter().info("Opened a MessageCreateListener in the VoiceChannelFunctions class");
            User author = null;
            String video = "";
            if (eventMusic.getMessage().getContent().contains("!music-request")) {
                author = eventMusic.getMessageAuthor().asUser().orElseThrow(AssertionError::new);
                video = eventMusic.getMessage().getContent().substring(15);
                //Collects the Video url from the command
                loggerGetter().info("Video address has been received as: " + video);
            }
            //listens to voice channel commands from the user. Use for only the !music-request
            //Got these things from the Mosaku example bot
            ServerTextChannel channel = eventMusic.getServerTextChannel().orElseThrow(AssertionError::new);
            assert author != null;
            ServerVoiceChannel voiceChannel = author.getConnectedVoiceChannel(channel.getServer()).orElse(null);

            String finalVideo = video;
            assert voiceChannel != null;
            voiceChannel.connect()
                    //bot connects to the voice channel that the user is in
                    .thenAcceptAsync(connection -> {
                        connection.queue(YouTubeAudioSource.of(apiGetter(), finalVideo).join());
                        messageListeners(connection);
                    })
                    //Queue up the video and play it in to the voice channel.
                    .exceptionally(throwable -> {
                        eventMusic.getChannel().asTextChannel().orElseThrow().sendMessage("Failed to Start Song.");
                        loggerGetter().warn("Failed to Start Song" + throwable);
                        return null;
                    })
                    //Throws a message of 'Failed to Start Song' and logs it as well.
                    .exceptionally(ExceptionLogger.get());
                    //throws the exception somewhere else too.
        });
    }

    private void messageListeners(AudioConnection connected) {
        //These methods listens to the rest of the commands
        //TODO Test the command and add MessageBuilders to Queue.
        //TODO Either wait for them to make something or make one on your own.
        apiGetter().addMessageCreateListener(queueEvent -> {
            if(queueEvent.getMessage().getContent().equalsIgnoreCase("!queue")) {
                loggerGetter().error("Currently Unuseable Function called by "
                        + queueEvent.getMessageAuthor().getDiscriminatedName());
            }
        });
        //get a list of the current queue

        //TODO Test the command and add MessageBuilders to Skip.
        apiGetter().addMessageCreateListener(skipEvent -> {
            if(skipEvent.getMessage().getContent().equalsIgnoreCase("!skip")) {
                //Dont know if this can get an error or not
                connected.dequeueCurrentSource();
                loggerGetter().info("Current source was dequeued by User "
                        + skipEvent.getMessageAuthor().getDiscriminatedName());
            }
        });
        //skips the next source/song

        //TODO Test the command and add MessageBuilders to Stop.
        apiGetter().addMessageCreateListener(stopEvent -> {
            if(stopEvent.getMessage().getContent().equalsIgnoreCase("!stop")) {
                loggerGetter().info("Connection interrupted by !stop command from User"
                        + stopEvent.getMessageAuthor().getDiscriminatedName());
                connected.close().exceptionally(throwable -> {
                    //Connection is closed and the logger will catch any mistakes.
                    loggerGetter().error("Connection Not closed, ran into issue" + throwable);
                    return null;
                });
            }
        });
        //stops and disconnects the bot from the voice channel
    }
}