package BotAudio;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class QueueEvent {
    private volatile ServerVoiceChannel channel;
    private volatile AudioConnection connection;
    private volatile AtomicReference<AudioSource> currentSource = new AtomicReference<>();
    private final BlockingQueue<AudioSource> queue = new LinkedBlockingQueue<>();

    public QueueEvent(ServerVoiceChannel channel, AudioConnection connection) {
        this.channel = channel;
        this.connection = connection;
    }

    public void setChannel(ServerVoiceChannel channel) {
        this.channel = channel;
    }

    public void removeCurrentSource() {
        currentSource.set(null);
    }

    public void queue(AudioSource source) {
        queue.add(source);
    }

    public boolean dequeue(AudioSource source) {
        if (currentSource.get() == source) {
            removeCurrentSource();
            return true;
        } else {
            return queue.remove(source);
        }
    }

    public Optional<AudioSource> getCurrentAudioSource() {
        return Optional.ofNullable(
                currentSource.updateAndGet(source -> {
                    if (source != null) {
                        return source;
                    }
                    return queue.peek();
                }));
    }

    public Iterator<AudioSource> getQueue() {
        return queue.iterator();
    }
}
