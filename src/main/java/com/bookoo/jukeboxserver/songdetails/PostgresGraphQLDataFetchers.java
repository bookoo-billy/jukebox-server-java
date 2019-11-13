package com.bookoo.jukeboxserver.songdetails;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PostgresGraphQLDataFetchers {

    private static final String url = "jdbc:postgresql://192.168.99.100:5432/jukebox";
    private static final String user = "jukebox";
    private static final String password = "example";

    private static List<Map<String, String>> songs = Arrays.asList(
        ImmutableMap.of("id", "song-1",
            "name", "Electric Gypsie",
            "artistId", "artist-1",
            "albumId", "album-1",
            "track", "1"),
        ImmutableMap.of("id", "song-2",
            "name", "Cry for You",
            "artistId", "artist-1",
            "albumId", "album-1",
            "track", "2"),
        ImmutableMap.of("id", "song-3",
            "name", "Mother's Dream",
            "artistId", "artist-2",
            "albumId", "album-2",
            "track", "1")
    );

    private static List<Map<String, String>> artists = Arrays.asList(
        ImmutableMap.of("id", "artist-1",
            "name", "Andy Timmons",
            "albumIds", "album-1"
        ),
        ImmutableMap.of("id", "artist-2",
            "name", "Candlebox",
            "albumIds", "album-2,album-3")
    );

    private static List<Map<String, String>> albums = Arrays.asList(
        ImmutableMap.of("id", "album-1",
            "name", "Best of Andy Timmons",
            "artistId", "artist-1"),
        ImmutableMap.of("id", "album-2",
            "name", "Candlebox",
            "artistId", "artist-2"),
        ImmutableMap.of("id", "album-3",
            "name", "Happy Pills",
            "artistId", "artist-2")
    );
    public DataFetcher getSongByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String songId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = DriverManager.getConnection(url, user, password)
                                                        .prepareStatement("SELECT * FROM songs WHERE id::text=?");
                pStat.setString(1, songId);

                ResultSet rSet = pStat.executeQuery();
                if (rSet.next()) {
                    return ImmutableMap.of(
                        "id", rSet.getString("id"),
                        "name", rSet.getString("name"),
                        "artistId", rSet.getString("artistid"),
                        "albumId", rSet.getString("albumid"),
                        "track", String.valueOf(rSet.getInt("track"))
                    );
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher getArtistByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String artistId = dataFetchingEnvironment.getArgument("id");

            return artists
                    .stream()
                    .filter(artist -> artist.get("id").equals(artistId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher getAlbumByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String albumId = dataFetchingEnvironment.getArgument("id");

            return albums
                    .stream()
                    .filter(album -> album.get("id").equals(albumId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher getArtistOfSongDataFetcher() {
        return dataFetchingEnvironment -> {
            Map<String, String> song = dataFetchingEnvironment.getSource();
            String artistId = song.get("artistId");
            return artists
                    .stream()
                    .filter(artist -> artist.get("id").equals(artistId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher getAlbumOfSongDataFetcher() {
        return dataFetchingEnvironment -> {
            Map<String, String> song = dataFetchingEnvironment.getSource();
            String albumId = song.get("albumId");
            return albums
                    .stream()
                    .filter(artist -> artist.get("id").equals(albumId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher getAlbumsOfArtistDataFetcher() {
        return dataFetchingEnvironment -> {
            Map<String, String> artist = dataFetchingEnvironment.getSource();
            List<String> albumIds = Arrays.asList(artist.get("albumIds").split(","));
            return albums
                    .stream()
                    .filter(album -> albumIds.contains(album.get("id")))
                    .collect(Collectors.toList());
        };
    }

    public DataFetcher getSongsOfArtistDataFetcher() {
        return dataFetchingEnvironment -> {
            Map<String, String> artist = dataFetchingEnvironment.getSource();
            List<String> songIds = Arrays.asList(artist.get("songIds").split(","));
            return songs
                    .stream()
                    .filter(song -> songIds.contains(song.get("id")))
                    .collect(Collectors.toList());
        };
    }
}