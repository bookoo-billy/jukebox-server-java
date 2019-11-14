package com.bookoo.jukeboxserver.songdetails;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PostgresGraphQLDataMutators {

    private static final String url = "jdbc:postgresql://192.168.99.100:5432/jukebox";
    private static final String user = "jukebox";
    private static final String password = "example";

    public DataFetcher<Song> createSongMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");

            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");
            String albumId = (String) map.get("albumId");
            Integer track = (Integer) map.get("track");

            try {
                PreparedStatement pStatSongs = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("INSERT INTO songs(name, artistid, albumid, track) VALUES (?, ?::uuid, ?::uuid, ?) RETURNING *");

                pStatSongs.setString(1, name);
                pStatSongs.setString(2, artistId);
                pStatSongs.setString(3, albumId);
                pStatSongs.setInt(4, track);

                if (pStatSongs.execute()) {
                    ResultSet rSet = pStatSongs.getResultSet();
                    if (rSet.next()) {
                        PreparedStatement pStatAlbumSongs = DriverManager.getConnection(url, user, password)
                                                                    .prepareStatement("INSERT INTO albumsongs(albumid, songid) VALUES (?::uuid, ?::uuid) RETURNING *");
                        pStatAlbumSongs.setString(1, albumId);
                        pStatAlbumSongs.setString(2, rSet.getString("id"));

                        if (pStatAlbumSongs.execute()) {
                            return new Song(
                                UUID.fromString(rSet.getString("id")),
                                rSet.getString("name"),
                                new Album(UUID.fromString(albumId), null, null),
                                new Artist(UUID.fromString(artistId), null, null, null),
                                rSet.getInt("track")
                            );
                        }
                    }
                }

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Artist> createArtistMutator() {
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
                        return new Artist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
                    }
                }

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Album> createAlbumMutator() {
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
                        return new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null);
                    }
                }

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}