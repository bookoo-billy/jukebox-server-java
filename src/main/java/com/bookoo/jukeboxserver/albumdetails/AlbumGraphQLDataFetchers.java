package com.bookoo.jukeboxserver.albumdetails;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bookoo.jukeboxserver.config.Config;
import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class AlbumGraphQLDataFetchers {

    @Autowired
    private Config config;

    public DataFetcher<Album> getAlbumByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String albumId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT * FROM albums WHERE id::text=?");
                pStat.setString(1, albumId);

                ResultSet rSet = pStat.executeQuery();
                if (rSet.next()) {
                    return new Album(
                        UUID.fromString(rSet.getString("id")),
                        rSet.getString("name"), null,
                        new Artist(UUID.fromString(rSet.getString("artistId")), null, null, null)
                    );
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Artist> getArtistOfAlbumDataFetcher() {
        return dataFetchingEnvironment -> {
            Album album = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT id, name FROM artists WHERE id=?::uuid");
                pStat.setString(1, album.getArtist().getId().toString());

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

    public DataFetcher<List<Song>> getSongsOfAlbumDataFetcher() {
        return dataFetchingEnvironment -> {
            Album album = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack songs.uri AS songuri FROM songs, albumsongs WHERE songs.albumid=?::uuid AND songs.albumid = albumsongs.albumid");
                pStat.setString(1, album.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<Song> songs = new ArrayList<> ();

                while (rSet.next()) {
                    songs.add(new Song(
                        UUID.fromString(rSet.getString("songid")),
                        rSet.getString("songname"),
                        album,
                        null,
                        rSet.getInt("songtrack"),
                        URI.create(rSet.getString("songuri"))
                    ));
                }

                return songs;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}