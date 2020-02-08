package com.bookoo.jukeboxserver.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.List;

import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Song;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ID3TagReader extends SimpleFileVisitor<Path> implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ID3TagReader.class);

    private DAO dao;

    public ID3TagReader(@Autowired DAO dao) {
        this.dao = dao;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        if (attr.isRegularFile()) {
            Mp3File mp3File;
            try {
                mp3File = new Mp3File(file);
            } catch (UnsupportedTagException e) {
                // Ignore for now
                return FileVisitResult.CONTINUE;
            } catch (InvalidDataException e) {
                // Ignore for now
                return FileVisitResult.CONTINUE;
            } catch (IOException e) {
                // Ignore for now
                return FileVisitResult.CONTINUE;
            }

            if (mp3File.hasId3v1Tag()) {
                ID3v1 tag = mp3File.getId3v1Tag();

                try {
                    Artist artist = dao.createArtist(tag.getArtist());
                    Album album = dao.createAlbum(tag.getAlbum(), artist);
                    Integer track;
                    try {
                        track = Integer.parseInt(tag.getTrack());
                    } catch (NumberFormatException e) {
                        track = 0;
                    }
                    Song created = dao.createSong(tag.getTitle(), artist, album, track, file.toUri());
                    LOGGER.info("Created song: " + created);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (mp3File.hasId3v2Tag()) {
                ID3v2 tag = mp3File.getId3v2Tag();

                try {
                    Artist artist = dao.createArtist(tag.getArtist());
                    Album album = dao.createAlbum(tag.getAlbum(), artist);
                    Integer track;
                    try {
                        track = Integer.parseInt(tag.getTrack());
                    } catch (NumberFormatException e) {
                        track = 0;
                    }
                    Song created = dao.createSong(tag.getTitle(), artist, album, track, file.toUri());
                    LOGGER.info("Created song: " + created);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public void run(String... args) throws Exception {
        addSongs(args);
        removeSongs();
        dao.refreshSearchIndex();
    }

    public void addSongs(String... args) throws Exception {
        Path startingDir = Path.of(args[0]);
        ID3TagReader id3TagReader = new ID3TagReader(dao);

        LOGGER.info("Started indexing songs from local directory " + startingDir);

        Files.walkFileTree(startingDir, id3TagReader);

        LOGGER.info("Finished indexing songs from local directory " + startingDir);
    }

    public void removeSongs() throws Exception {
        LOGGER.info("Starting removing songs from index that no longer exist");
        List<Song> songs = dao.listSongs();

        for (Song song : songs) {
            if (song.getUri().getScheme().equals("file")) {
                File file = new File(song.getUri());
                if (!file.isFile()) {
                    Song removed = dao.removeSong(song.getUuid().toString());
                    LOGGER.info("Removed song " + removed);
                }
            }
        }

        LOGGER.info("Finished removing songs from index that no longer exist");
    }
}