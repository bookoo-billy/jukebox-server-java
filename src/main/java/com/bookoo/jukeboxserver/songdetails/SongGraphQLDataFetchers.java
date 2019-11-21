package com.bookoo.jukeboxserver.songdetails;

import java.sql.SQLException;

import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class SongGraphQLDataFetchers {

    @Autowired
    private DAO dao;

    public DataFetcher<Song> getSongByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String songId = dataFetchingEnvironment.getArgument("id");

            try {
                return dao.getSongById(songId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Artist> getArtistOfSongDataFetcher() {
        return dataFetchingEnvironment -> {
            Song song = dataFetchingEnvironment.getSource();

            try {
                return dao.getArtistOfSong(song);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Album> getAlbumOfSongDataFetcher() {
        return dataFetchingEnvironment -> {
            Song song = dataFetchingEnvironment.getSource();

            try {
                return dao.getAlbumOfSong(song);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}