package com.bookoo.jukeboxserver.songdetails;

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
public class SongGraphQLDataMutators {

    @Autowired
    private Config config;

    @SuppressWarnings("unchecked")
    public DataFetcher<Song> createSongMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");

            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");
            String albumId = (String) map.get("albumId");
            Integer track = (Integer) map.get("track");

            try {
                PreparedStatement pStatSongs = config.dbConnection()
                                                        .prepareStatement("INSERT INTO songs(name, artistid, albumid, track) VALUES (?, ?::uuid, ?::uuid, ?) ON CONFLICT (name, artistid, albumid) DO UPDATE SET name=?, artistid=?::uuid, albumid=?::uuid, track=? RETURNING *");

                pStatSongs.setString(1, name);
                pStatSongs.setString(2, artistId);
                pStatSongs.setString(3, albumId);

                if (track == null) {
                    pStatSongs.setNull(4, Types.INTEGER);
                } else {
                    pStatSongs.setInt(4, track);
                }

                pStatSongs.setString(5, name);
                pStatSongs.setString(6, artistId);
                pStatSongs.setString(7, albumId);

                if (track == null) {
                    pStatSongs.setNull(8, Types.INTEGER);
                } else {
                    pStatSongs.setInt(8, track);
                }

                if (pStatSongs.execute()) {
                    ResultSet rSet = pStatSongs.getResultSet();
                    if (rSet.next()) {
                        PreparedStatement pStatAlbumSongs = config.dbConnection()
                                                                    .prepareStatement("INSERT INTO albumsongs(albumid, songid) VALUES (?::uuid, ?::uuid) ON CONFLICT DO NOTHING RETURNING *");
                        pStatAlbumSongs.setString(1, albumId);
                        pStatAlbumSongs.setString(2, rSet.getString("id"));

                        if (pStatAlbumSongs.execute()) {
                            return new Song(
                                UUID.fromString(rSet.getString("id")),
                                rSet.getString("name"),
                                new Album(UUID.fromString(albumId), null, null, null),
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
}