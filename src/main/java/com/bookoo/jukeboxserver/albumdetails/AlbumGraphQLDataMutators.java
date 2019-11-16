package com.bookoo.jukeboxserver.albumdetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.UUID;

import com.bookoo.jukeboxserver.config.Config;
import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class AlbumGraphQLDataMutators {

    @Autowired
    private Config config;

    @SuppressWarnings("unchecked")
    public DataFetcher<Album> createAlbumMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");
            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("INSERT INTO albums(name, artistid) VALUES (?, ?::uuid) ON CONFLICT (name, artistid) DO UPDATE SET name=?, artistid=?::uuid RETURNING *");
                pStat.setString(1, name);
                pStat.setString(2, artistId);
                pStat.setString(3, name);
                pStat.setString(4, artistId);

                if (pStat.execute()) {
                    ResultSet rSet = pStat.getResultSet();
                    if (rSet.next()) {
                        return new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
                    }
                }

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}