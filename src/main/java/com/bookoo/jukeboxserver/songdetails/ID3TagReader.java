package com.bookoo.jukeboxserver.songdetails;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class ID3TagReader extends SimpleFileVisitor<Path> {
    public static void main(String[] args) throws IOException {
        Path startingDir = Path.of("C:\\Users\\Will\\Music");
        ID3TagReader id3TagReader = new ID3TagReader();
        Files.walkFileTree(startingDir, id3TagReader);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        if (attr.isRegularFile()) {
            Mp3File mp3File;
            try {
                mp3File = new Mp3File(file);
            } catch (UnsupportedTagException e) {
                //Ignore for now
                return FileVisitResult.CONTINUE;
            } catch (InvalidDataException e) {
                //Ignore for now
                return FileVisitResult.CONTINUE;
            } catch (IOException e) {
                //Ignore for now
                return FileVisitResult.CONTINUE;
            }

            if (mp3File.hasId3v1Tag()) {
                ID3v1 tag = mp3File.getId3v1Tag();
                System.out.println(tag.getArtist() + " - " + tag.getTitle());
            } else if (mp3File.hasId3v2Tag()) {
                ID3v2 tag = mp3File.getId3v2Tag();
                System.out.println(tag.getArtist() + " - " + tag.getTitle());
            } else if (mp3File.hasCustomTag()) {
                System.out.println(mp3File.getCustomTag());
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
        System.err.println(exc);
        return FileVisitResult.CONTINUE;
    }   
}