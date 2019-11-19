package com.bookoo.jukeboxserver.artistdetails;

import java.sql.SQLException;
import java.util.Map;

import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class ArtistGraphQLDataMutators {

    @Autowired
    private DAO dao;

    @SuppressWarnings("unchecked")
    public DataFetcher<Artist> createArtistMutator() {
        return dataFetchingEnvironment -> {
            Map<String, String> map = (Map<String, String>) dataFetchingEnvironment.getArguments().get("input");
            String name = map.get("name");
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