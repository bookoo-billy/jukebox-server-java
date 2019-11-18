package com.bookoo.jukeboxserver.artistdetails;

import java.sql.SQLException;

import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class ArtistGraphQLDataMutators {

    @Autowired
    private DAO dao;

    public DataFetcher<Artist> createArtistMutator() {
        return dataFetchingEnvironment -> {
            String name = (String) dataFetchingEnvironment.getArguments().get("name");

            try {
                Artist artist = dao.createArtist(name);

                if (artist != null) return artist;

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}