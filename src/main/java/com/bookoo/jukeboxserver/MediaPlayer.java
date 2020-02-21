package com.bookoo.jukeboxserver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.bookoo.AudioSink;
import com.bookoo.jukebox.JukeboxProtos.Message;
import com.bookoo.jukebox.JukeboxProtos.Message.MessageType;
import com.bookoo.jukebox.JukeboxProtos.SongChunk;
import com.bookoo.jukebox.JukeboxProtos.SongPreamble;
import com.bookoo.jukeboxserver.domain.Song;
import com.google.protobuf.ByteString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import javazoom.spi.mpeg.sampled.file.MpegEncoding;

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
            currentRunner = new Runner(song, new RemotePlayer(Collections.singletonList(new AudioSink("localhost", 2187)), song));
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
                                public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState,
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

    public static class LocalPlayer implements Player {

        private final javafx.scene.media.MediaPlayer player;
        private final Song song;

        public LocalPlayer(Song song) {
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

    public static class RemotePlayer implements Player {
        private static final Logger LOGGER = LoggerFactory.getLogger(RemotePlayer.class);

        private final List<AudioSink> sinks;
        private final Song song;

        public RemotePlayer(List<AudioSink> sinks, Song song) {
            this.sinks = sinks;
            this.song = song;
        }

        @Override
        public Song pause() {
            return song;
        }

        @Override
        public Song resume() {
            return song;
        }

        @Override
        public Song stop() {
            return song;
        }

        @Override
        public Song play() {
            for (AudioSink sink : sinks) {
                try (Socket socket = new Socket(sink.getHost(), sink.getPort())) {
                    File file = new File(song.getUri());
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(file);
                    AudioFormat baseFormat = audioInput.getFormat();
                    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                                                    16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                    SongPreamble songPreamble = SongPreamble.newBuilder()
                                                    .setChannels(decodedFormat.getChannels())
                                                    .setEncoding(translate(decodedFormat.getEncoding()))
                                                    .setFrameRate(decodedFormat.getFrameRate())
                                                    .setFrameSize(decodedFormat.getFrameSize())
                                                    .setSampleRate(decodedFormat.getSampleRate())
                                                    .setSampleSizeInBits(decodedFormat.getSampleSizeInBits())
                                                    .build();

                    Message preambleMessage = Message.newBuilder()
                                                        .setMessageType(MessageType.SONG_PREAMBLE)
                                                        .setSongPreamble(songPreamble)
                                                        .build();

                    preambleMessage.writeDelimitedTo(socket.getOutputStream());

                    AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, audioInput);
                    byte[] buffer = new byte[1024 * 4];

                    int nBytesRead = 0;

                    while (nBytesRead != -1) {
                        nBytesRead = din.read(buffer, 0, buffer.length);
                        if (nBytesRead != -1) {
                            SongChunk songChunk = SongChunk.newBuilder().setChunk(ByteString.copyFrom(buffer, 0, nBytesRead)).build();
                            Message chunkMessage = Message.newBuilder().setMessageType(MessageType.SONG_CHUNK).setSongChunk(songChunk).build();
                            chunkMessage.writeDelimitedTo(socket.getOutputStream());
                        }
                    }

                    din.close();
                    audioInput.close();
                    Message clientResponse = Message.parseDelimitedFrom(socket.getInputStream());

                    if (clientResponse.getMessageType() == MessageType.AUDIO_STREAM_CONSUMED) {
                        LOGGER.info("Finished writing "  + song + " to audio sink");
                    } else if (clientResponse.getMessageType() == MessageType.AUDIO_STREAM_RESET) {
                        LOGGER.error("Error while writing audio stream, resetting");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return song;
        }

        private SongPreamble.Encoding translate(AudioFormat.Encoding encoding) {
            if (encoding == AudioFormat.Encoding.ALAW) {
                return SongPreamble.Encoding.ALAW;
            } else if (encoding == AudioFormat.Encoding.PCM_FLOAT) {
                return SongPreamble.Encoding.PCM_FLOAT;
            } else if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
                return SongPreamble.Encoding.PCM_SIGNED;
            } else if (encoding == AudioFormat.Encoding.PCM_UNSIGNED) {
                return SongPreamble.Encoding.PCM_UNSIGNED;
            } else if (encoding == AudioFormat.Encoding.ULAW) {
                return SongPreamble.Encoding.ULAW;
            } else if (encoding == MpegEncoding.MPEG1L1) {
                return SongPreamble.Encoding.MPEG1L1;
            } else if (encoding == MpegEncoding.MPEG1L2) {
                return SongPreamble.Encoding.MPEG1L2;
            } else if (encoding == MpegEncoding.MPEG1L3) {
                return SongPreamble.Encoding.MPEG1L3;
            } else if (encoding == MpegEncoding.MPEG2DOT5L1) {
                return SongPreamble.Encoding.MPEG2DOT5L1;
            } else if (encoding == MpegEncoding.MPEG2DOT5L2) {
                return SongPreamble.Encoding.MPEG2DOT5L2;
            } else if (encoding == MpegEncoding.MPEG2DOT5L3) {
                return SongPreamble.Encoding.MPEG2DOT5L3;
            } else if (encoding == MpegEncoding.MPEG2L1) {
                return SongPreamble.Encoding.MPEG2L1;
            } else if (encoding == MpegEncoding.MPEG2L2) {
                return SongPreamble.Encoding.MPEG2L2;
            } else if (encoding == MpegEncoding.MPEG2L3) {
                return SongPreamble.Encoding.MPEG2L3;
            } else {
                throw new RuntimeException("Unknown AudioFormat.Encoding " + encoding);
            }
        }
    }
}
