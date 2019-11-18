package com.bookoo.jukeboxserver.songdetails;

import java.sql.SQLException;
import java.util.Map;

import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class SongGraphQLDataMutators {

    @Autowired
    private DAO dao;

    @SuppressWarnings("unchecked")
    public DataFetcher<Song> createSongMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");

            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");
            String albumId = (String) map.get("albumId");
            Integer track = (Integer) map.get("track");

            try {
                Song song = dao.createSong(name, artistId, albumId, track);

                if (song != null) return song;

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}