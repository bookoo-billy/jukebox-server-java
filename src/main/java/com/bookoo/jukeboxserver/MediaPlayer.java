package com.bookoo.jukeboxserver;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;

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

        if (song.getUri().getScheme().startsWith("http") && song.getUri().getHost().equals("www.youtube.com")) {
            currentRunner = new Runner(song, new YoutubePlayer(song));
        } else {
            currentRunner = new Runner(song, new DefaultPlayer(song));
        }

        es.submit(currentRunner);

        return song;
    }

    public static class YoutubePlayer implements Player {

        private final Song song;
        private WebView webview;

        public YoutubePlayer(Song song) {
            this.song = song;
        }

        @Override
        public Song pause() {
            Platform.runLater(() -> {
                webview.getEngine().executeScript("document.getElementById(\"ytd-player\").pause();");
            });

            return song;
        }

        @Override
        public Song resume() {
            Platform.runLater(() -> {
                webview.getEngine().executeScript("document.getElementById(\"ytd-player\").play();");
            });

            return song;
        }

        @Override
        public Song stop() {
            Platform.runLater(() -> {
                webview.getEngine().executeScript("document.getElementById(\"ytd-player\").stop();");
            });

            return song;
        }

        @Override
        public Song play() {
            Platform.runLater(() -> {
                if (webview == null) {
                    webview = new WebView();
                }

                try {
                    webview.getEngine().getLoadWorker().stateProperty()
                            .addListener(new ChangeListener<Worker.State>() {
                                public void changed(ObservableValue ov, Worker.State oldState,
                                        Worker.State newState) {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        System.out.println(oldState);
                                        System.out.println(newState);
                                        webview.getEngine()
                                                .executeScript("document.getElementById(\"ytd-player\").play();");
                                    } else if (newState == Worker.State.CANCELLED) {
                                        throw new RuntimeException("Failed to load youtube video");
                                    }
                                }
                            });
                    webview.getEngine().load(song.getUri().toURL().toString());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            });

            return song;
        }
    }

    public static class Runner implements Runnable {
        private static enum State {
            STOPPED, PAUSED, PLAYING, STARTING,
        }

        private volatile Runner.State state;
        private final Player player;
        private final Song song;

        public Runner(Song song, Player player) {
            this.song = song;
            
            this.player = player;
            this.state = Runner.State.STARTING;
        }

        @Override
        public void run() {
            while (this.state != Runner.State.STOPPED) {
                if (this.state == Runner.State.STARTING) {
                    play();
                }
            }

            stop();
        }

        public Song pause() {
            player.pause();
            state = Runner.State.PAUSED;
            return song;
        }

        public Song resume() {
            player.resume();
            state = Runner.State.PLAYING;
            return song;
        }

        public Song stop() {
            player.stop();
            state = Runner.State.STOPPED;
            return song;
        }

        public Song play() {
            player.play();
            state = Runner.State.PLAYING;
            return song;
        }
    }

    public interface Player {
        Song pause();
        Song resume();
        Song stop();
        Song play();
    }

    public static class DefaultPlayer implements Player {

        private final javafx.scene.media.MediaPlayer player;
        private final Song song;

        public DefaultPlayer(Song song) {
            final javafx.scene.media.Media media = new javafx.scene.media.Media(song.getUri().toString());
            this.player = new javafx.scene.media.MediaPlayer(media);
            this.song = song;
        }

        @Override
        public Song pause() {
            this.player.pause();
            return song;
        }

        @Override
        public Song resume() {
            this.player.play();
            return song;
        }

        @Override
        public Song stop() {
            this.player.stop();
            return song;
        }

        @Override
        public Song play() {
            this.player.play();
            return song;
        }
    }
}
