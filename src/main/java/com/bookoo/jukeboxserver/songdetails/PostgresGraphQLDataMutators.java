package com.bookoo.jukeboxserver.songdetails;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PostgresGraphQLDataMutators {

    private static final String url = "jdbc:postgresql://192.168.99.100:5432/jukebox";
    private static final String user = "jukebox";
    private static final String password = "example";

    public DataFetcher createSongMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");

            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");
            String albumId = (String) map.get("albumId");
            Integer track = (Integer) map.get("track");

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("INSERT INTO songs(name, artistid, albumid, track) VALUES (?, ?::uuid, ?::uuid, ?) RETURNING *");
                pStat.setString(1, name);
                pStat.setString(2, artistId);
                pStat.setString(3, albumId);
                pStat.setInt(4, track);

                if (pStat.execute()) {
                    ResultSet rSet = pStat.getResultSet();
                    if (rSet.next()) {
                        return ImmutableMap.of(
                            "id", rSet.getString("id"),
                            "name", rSet.getString("name"),
                            "artistId", rSet.getString("artistid"),
                            "albumId", rSet.getString("albumid"),
                            "track", String.valueOf(rSet.getInt("track"))
                        );
                    }
                }

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher createArtistMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");
            String name = (String) map.get("name");

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("INSERT INTO artists(name) VALUES (?) RETURNING *");
                pStat.setString(1, name);

                if (pStat.execute()) {
                    ResultSet rSet = pStat.getResultSet();
                    if (rSet.next()) {
                        return ImmutableMap.of(
                            "id", rSet.getString("id"),
                            "name", rSet.getString("name")
                        );
                    }
                }

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher createAlbumMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");
            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("INSERT INTO albums(name, artistid) VALUES (?, ?::uuid) RETURNING *");
                pStat.setString(1, name);
                pStat.setString(2, artistId);

                if (pStat.execute()) {
                    ResultSet rSet = pStat.getResultSet();
                    if (rSet.next()) {
                        return ImmutableMap.of(
                            "id", rSet.getString("id"),
                            "name", rSet.getString("name"),
                            "artist", ImmutableMap.of()
                        );
                    }
                }

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}