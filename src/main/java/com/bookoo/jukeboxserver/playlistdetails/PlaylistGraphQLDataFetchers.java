package com.bookoo.jukeboxserver.playlistdetails;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bookoo.jukeboxserver.config.Config;
import com.bookoo.jukeboxserver.domain.Playlist;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PlaylistGraphQLDataFetchers {

    @Autowired
    private Config config;

    public DataFetcher<Playlist> getPlaylistByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String albumId = dataFetchingEnvironment.getArgument("id");

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("SELECT * FROM playlists WHERE id::text=?");
                pStat.setString(1, albumId);

                ResultSet rSet = pStat.executeQuery();
                if (rSet.next()) {
                    return new Playlist(
                        UUID.fromString(rSet.getString("id")),
                        rSet.getString("name"),
                        null
                    );
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<List<Song>> getSongsOfPlaylistDataFetcher() {
        return dataFetchingEnvironment -> {
            Playlist playlist = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement(
                                                    "SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack, songs.uri AS songuri " +
                                                    "FROM playlists, playlistsongs, songs " +
                                                    "WHERE playlists.id=?::uuid AND playlistsongs.playlistid=playlists.id AND songs.id = playlistsongs.songid " +
                                                    "ORDER BY inserttime ASC");
                pStat.setString(1, playlist.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<Song> songs = new ArrayList<> ();

                while (rSet.next()) {
                    songs.add(new Song(
                        UUID.fromString(rSet.getString("songid")),
                        rSet.getString("songname"),
                        null,
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