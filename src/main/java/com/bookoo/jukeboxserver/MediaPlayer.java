package com.bookoo.jukeboxserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.stereotype.Component;

@Component
public class MediaPlayer {

    private final ExecutorService es;
    private Runner currentRunner;

    public MediaPlayer() {
        this.es = Executors.newSingleThreadExecutor();
    }

    public synchronized Song resume() {
        if (currentRunner != null) {
            return currentRunner.resume();
        }

        return null;
    }

    public synchronized Song pause() {
        if (currentRunner != null) {
            return currentRunner.pause();
        }

        return null;
    }

    public synchronized Song play(final Song song) {
        if (currentRunner != null) {
            currentRunner.stop();
        }

        currentRunner = new Runner(song);
        es.submit(currentRunner);

        return song;
    }

    private static class Runner implements Runnable {
        private static enum State {
            STOPPED,
            PAUSED,
            PLAYING,
            STARTING,
        }

        private volatile Runner.State state;
        private final javafx.scene.media.MediaPlayer player;
        private final Song song;

        public Runner(Song song) {
            this.song = song;

            final javafx.scene.media.Media media = new javafx.scene.media.Media(song.getUri().toString());
            this.player = new javafx.scene.media.MediaPlayer(media);
            this.state = Runner.State.STARTING;
        }

        @Override
        public void run() {
            while (this.state != Runner.State.STOPPED) {
                if (this.state == Runner.State.STARTING) {
                    resume();
                }
            }

            this.player.stop();
        }

        public Song pause() {
            this.player.pause();
            this.state = Runner.State.PAUSED;
            return song;
        }

        public Song resume() {
            this.player.play();
            this.state = Runner.State.PLAYING;
            return song;
        }

        public Song stop() {
            this.state = Runner.State.STOPPED;
            return song;
        }
    }
}
