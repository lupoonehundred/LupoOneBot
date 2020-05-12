package BotAudio;

import BotBuilder.LupoOneBot;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.lavaplayerwrapper.youtube.YouTubeAudioSource;

import java.util.Iterator;

public class VoiceChannelFunctions extends LupoOneBot implements ServerVoiceChannelMemberJoinListener{
    @Override
    public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
        loggerGetter().info("A User has joined a Voice Channel: " + event.getChannel().asVoiceChannel().toString());
        //quick leave condition if no one else is in the given voice channel.
        apiGetter().addServerVoiceChannelMemberLeaveListener(leaveEvent -> {
            leaveEvent.getServer().getAudioConnection().ifPresent(connection -> {
                if (connection.getChannel() == leaveEvent.getChannel()) {
                    if (leaveEvent.getChannel().getConnectedUsers().size() <= 1) {
                        event.getServer().getTextChannelsByName("music-text-channel").get(0)
                                .sendMessage("Bot has left the VC, Music from queue has been removed");
                        connection.close();
                    }
                }
            });
        });
        //If Someone leaves the Voice Channel,FIXME Dont think I need this
        /*  This method has a couple of IDE required security asserts.
            ".orElseThrow(AssertionError::new)" still learning what these are
            and how to use them correctly.
        */
        //TODO Possibly find a way to optimize this section.
        event.getApi().addMessageCreateListener(eventMusic -> {
            loggerGetter().info("Opened a MessageCreateListener in the BotAudio.VoiceChannelFunctions class");
            User author = null;
            String video = "";
            if (eventMusic.getMessage().getContent().contains("!request")) {
                author = eventMusic.getMessageAuthor().asUser().orElseThrow(AssertionError::new);
                video = eventMusic.getMessage().getContent().substring(9);
                //Collects the Video url from the command
                loggerGetter().info("Video address has been received as: " + video);
            }
            //listens to voice channel commands from the user. Use for only the !music-request
            //Got these things from the Mosaku example bot
            //FIXME I think that part of these can be removed
            ServerTextChannel channel = eventMusic.getServerTextChannel().orElseThrow(AssertionError::new);
            assert author != null;
            ServerVoiceChannel voiceChannel = author.getConnectedVoiceChannel(channel.getServer()).orElse(null);

            String finalVideo = video;
            assert voiceChannel != null;
            voiceChannel.connect()
                    //bot connects to the voice channel that the user is in
                    .thenAcceptAsync(connection -> {
                        connection.queue(YouTubeAudioSource.of(event.getApi(), finalVideo).join());
                        messageListeners(connection, event.getChannel().asServerVoiceChannel().get());
                    })
                    //Queue up the video and play it in to the voice channel.
                    .exceptionally(throwable -> {
                        eventMusic.getChannel().asTextChannel().orElseThrow().sendMessage("Failed to Start Song.");
                        loggerGetter().warn("Failed to Start Song" + throwable);
                        return null;
                    });
                    //Throws a message of 'Failed to Start Song' and logs it as well.
        });
    }

    private void messageListeners(AudioConnection connected, ServerVoiceChannel serverVC) {
        //These methods listens to the rest of the commands
        //TODO Test the command and add MessageBuilders to Queue.
        //TODO make one on your own
        apiGetter().addMessageCreateListener(queueEvent -> {
            if(queueEvent.getMessage().getContent().equalsIgnoreCase("!queue")) {
                loggerGetter().error("Currently Unuseable Function called by "
                        + queueEvent.getMessageAuthor().getDiscriminatedName());
                Iterator<AudioSource> currentQueue = new QueueEvent(serverVC, connected).getQueue();
                MessageBuilder queue = new MessageBuilder();
                loggerGetter().info("Getting the sources");
                for (Iterator<AudioSource> it = currentQueue; it.hasNext(); ) {
                    AudioSource as = it.next();
                    queue.append(as.toString() + " \n");
                }
                queue.send(queueEvent.getChannel());
            }
        });
        //get a list of the current queue

        apiGetter().addMessageCreateListener(skipEvent -> {
            if(skipEvent.getMessage().getContent().equalsIgnoreCase("!skip")) {
                //Dont know if this can get an error or not
                connected.dequeueCurrentSource();
                loggerGetter().info("Current source was dequeued by User "
                        + skipEvent.getMessageAuthor().getDiscriminatedName());
                new MessageBuilder()
                        .append("Source removed from queue")
                        .append(connected.getCurrentAudioSource().get().toString())
                        .send(skipEvent.getChannel());
            }
        });
        //skips the next source/song
        apiGetter().addMessageCreateListener(stopEvent -> {
            if(stopEvent.getMessage().getContent().equalsIgnoreCase("!stop")) {
                loggerGetter().info("Connection interrupted by !stop command from User"
                        + stopEvent.getMessageAuthor().getDiscriminatedName());
                connected.close().exceptionally(throwable -> {
                    //Connection is closed and the logger will catch any mistakes.
                    loggerGetter().error("Connection Not closed, ran into issue" + throwable);
                    return null;
                });
                new MessageBuilder()
                        .append("Bot has left the VC, Music from queue has been removed")
                        .send(stopEvent.getChannel());
            }
        });
        //stops and disconnects the bot from the voice channel

        apiGetter().addMessageCreateListener(cSongEvent -> {
            if(cSongEvent.getMessage().getContent().equalsIgnoreCase("!song")) {
                loggerGetter().info("Receiving the Current Audio Sources for " + cSongEvent.getMessageAuthor().getName());

                new MessageBuilder()
                        .append("This is the current song playing ")
                        .append(new QueueEvent(serverVC, connected).getCurrentAudioSource().get().toString())
                        .send(cSongEvent.getChannel());
            }
        });
    }
}