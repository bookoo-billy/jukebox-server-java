package com.bookoo.jukeboxserver.songdetails;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bookoo.jukeboxserver.domain.Album;
import com.bookoo.jukeboxserver.domain.Artist;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PostgresGraphQLDataFetchers {

    private static final String url = "jdbc:postgresql://192.168.99.100:5432/jukebox";
    private static final String user = "jukebox";
    private static final String password = "example";

    public DataFetcher<Song> getSongByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String songId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("SELECT id, name, track, artistid, albumid FROM songs WHERE songs.id=?::uuid");
                pStat.setString(1, songId);

                ResultSet rSet = pStat.executeQuery();
                if (rSet.next()) {
                    return new Song(
                        UUID.fromString(rSet.getString("id")),
                        rSet.getString("name"),
                        new Album(UUID.fromString(rSet.getString("albumid")), null, null),
                        new Artist(UUID.fromString(rSet.getString("artistid")), null, null, null),
                        rSet.getInt("track")
                    );
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<Artist> getArtistByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String artistId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
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

    public DataFetcher<Album> getAlbumByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String albumId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("SELECT * FROM albums WHERE id::text=?");
                pStat.setString(1, albumId);

                ResultSet rSet = pStat.executeQuery();
                if (rSet.next()) {
                    return new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null);
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
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
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
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("SELECT * FROM albums WHERE id::text=?");
                pStat.setString(1, song.getAlbum().getId().toString());

                ResultSet rSet = pStat.executeQuery();

                if (rSet.next()) {
                    return new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null);
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
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("SELECT * FROM albums WHERE artistid::text=?");
                pStat.setString(1, artist.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<Album> albums = new ArrayList<> ();

                while (rSet.next()) {
                    albums.add(new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null));
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
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                    .prepareStatement("SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack, artists.id AS artistid, artists.name AS artistname, albums.id AS albumid, albums.name as albumname FROM songs, artists, albums WHERE songs.artistid=?::uuid AND songs.artistId = artists.id AND songs.albumId = albums.id");
                pStat.setString(1, artist.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<Song> songs = new ArrayList<> ();

                while (rSet.next()) {
                    songs.add(new Song(
                        UUID.fromString(rSet.getString("songid")),
                        rSet.getString("songname"),
                        new Album(UUID.fromString(rSet.getString("albumid")), null, null),
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

    public DataFetcher<Artist> getArtistOfAlbumDataFetcher() {
        return dataFetchingEnvironment -> {
            Album album = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                    .prepareStatement("SELECT id, name FROM artists WHERE id=?::uuid");
                pStat.setString(1, album.getId().toString());

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
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                    .prepareStatement("SELECT songs.id AS songid, songs.name AS songname, track AS songtrack FROM songs, albumsongs WHERE songs.albumid=?::uuid AND songs.albumid = albumsongs.albumid");
                pStat.setString(1, album.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<Song> songs = new ArrayList<> ();

                while (rSet.next()) {
                    songs.add(new Song(
                        UUID.fromString(rSet.getString("songid")),
                        rSet.getString("songname"),
                        album,
                        null,
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