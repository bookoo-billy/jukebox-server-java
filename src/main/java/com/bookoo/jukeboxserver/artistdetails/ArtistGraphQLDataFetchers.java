package com.bookoo.jukeboxserver.artistdetails;

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
public class ArtistGraphQLDataFetchers {

    @Autowired
    private DAO dao;

    public DataFetcher<Artist> getArtistByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String artistId = dataFetchingEnvironment.getArgument("id");

            try {
                return dao.getArtistById(artistId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<List<Album>> getAlbumsOfArtistDataFetcher() {
        return dataFetchingEnvironment -> {
            Artist artist = dataFetchingEnvironment.getSource();

            try {
                return dao.getAlbumsOfArtist(artist);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<List<Song>> getSongsOfArtistDataFetcher() {
        return dataFetchingEnvironment -> {
            Artist artist = dataFetchingEnvironment.getSource();

            try {
                return dao.getSongsOfArtist(artist);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}