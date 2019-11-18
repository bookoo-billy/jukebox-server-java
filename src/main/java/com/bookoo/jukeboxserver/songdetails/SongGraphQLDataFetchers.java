package com.bookoo.jukeboxserver.songdetails;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.bookoo.jukeboxserver.config.Config;
import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class SongGraphQLDataFetchers {

    @Autowired
    private Config config;

    public DataFetcher<Song> getSongByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String songId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT id, name, track, artistid, albumid, uri FROM songs WHERE songs.id=?::uuid");
                pStat.setString(1, songId);

                ResultSet rSet = pStat.executeQuery();

                if (rSet.next()) {
                    return new Song(
                        UUID.fromString(rSet.getString("id")),
                        rSet.getString("name"),
                        new Album(UUID.fromString(rSet.getString("albumid")), null, null, null),
                        new Artist(UUID.fromString(rSet.getString("artistid")), null, null, null),
                        rSet.getInt("track"),
                        URI.create(rSet.getString("uri"))
                    );
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Artist> getArtistOfSongDataFetcher() {
        return dataFetchingEnvironment -> {
            Song song = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT * FROM artists WHERE id::text=?");
                pStat.setString(1, song.getArtist().getId().toString());

                ResultSet rSet = pStat.executeQuery();

                if (rSet.next()) {
                    return new Artist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Album> getAlbumOfSongDataFetcher() {
        return dataFetchingEnvironment -> {
            Song song = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT * FROM albums WHERE id::text=?");
                pStat.setString(1, song.getAlbum().getId().toString());

                ResultSet rSet = pStat.executeQuery();

                if (rSet.next()) {
                    return new Album(
                        UUID.fromString(rSet.getString("id")),
                        rSet.getString("name"),
                        null,
                        new Artist(UUID.fromString(rSet.getString("artistId")), null, null, null)
                    );
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}