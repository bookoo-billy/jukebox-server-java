package com.bookoo.jukeboxserver.albumdetails;

import java.sql.SQLException;
import java.util.List;

import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class AlbumGraphQLDataFetchers {

    @Autowired
    private DAO dao;

    public DataFetcher<Album> getAlbumByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String albumId = dataFetchingEnvironment.getArgument("id");

            try {
                return dao.getAlbumById(albumId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Artist> getArtistOfAlbumDataFetcher() {
        return dataFetchingEnvironment -> {
            Album album = dataFetchingEnvironment.getSource();

            try {
                return dao.getArtistOfAlbum(album);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<List<Song>> getSongsOfAlbumDataFetcher() {
        return dataFetchingEnvironment -> {
            Album album = dataFetchingEnvironment.getSource();

            try {
                return dao.getSongsOfAlbum(album);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}