package com.bookoo.jukeboxserver.artistdetails;

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
public class ArtistGraphQLDataFetchers {

    @Autowired
    private Config config;

    public DataFetcher<Artist> getArtistByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String artistId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT * FROM artists WHERE id::text=?");
                pStat.setString(1, artistId);

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

    public DataFetcher<List<Album>> getAlbumsOfArtistDataFetcher() {
        return dataFetchingEnvironment -> {
            Artist artist = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT * FROM albums WHERE artistid::text=?");
                pStat.setString(1, artist.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<Album> albums = new ArrayList<> ();

                while (rSet.next()) {
                    albums.add(new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null));
                }

                return albums;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<List<Song>> getSongsOfArtistDataFetcher() {
        return dataFetchingEnvironment -> {
            Artist artist = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack, artists.id AS artistid, artists.name AS artistname, albums.id AS albumid, albums.name as albumname FROM songs, artists, albums WHERE songs.artistid=?::uuid AND songs.artistId = artists.id AND songs.albumId = albums.id");
                pStat.setString(1, artist.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<Song> songs = new ArrayList<> ();

                while (rSet.next()) {
                    songs.add(new Song(
                        UUID.fromString(rSet.getString("songid")),
                        rSet.getString("songname"),
                        new Album(UUID.fromString(rSet.getString("albumid")), null, null, null),
                        artist,
                        rSet.getInt("songtrack")
                    ));
                }

                return songs;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}