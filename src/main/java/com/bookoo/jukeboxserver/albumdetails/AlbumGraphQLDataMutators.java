package com.bookoo.jukeboxserver.albumdetails;

import java.sql.SQLException;
import java.util.Map;

import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class AlbumGraphQLDataMutators {

    @Autowired
    private DAO dao;

    @SuppressWarnings("unchecked")
    public DataFetcher<Album> createAlbumMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");
            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");

            try {
                Album album = dao.createAlbum(name, artistId);

                if (album != null) return album;

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}