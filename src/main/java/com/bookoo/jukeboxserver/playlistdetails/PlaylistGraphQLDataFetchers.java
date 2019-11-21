package com.bookoo.jukeboxserver.playlistdetails;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bookoo.jukeboxserver.config.Config;
import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Playlist;
import com.bookoo.jukeboxserver.domain.PlaylistItem;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PlaylistGraphQLDataFetchers {

    @Autowired
    private DAO dao;

    @Autowired
    private Config config;

    public DataFetcher<Playlist> getPlaylistByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String playlistId = dataFetchingEnvironment.getArgument("id");

            try {
                return dao.getPlaylist(playlistId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public DataFetcher<List<PlaylistItem>> getItemsOfPlaylistDataFetcher() {
        return dataFetchingEnvironment -> {
            Playlist playlist = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement(
                                                    "SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack, songs.uri AS songuri, playlistsongs.inserttime AS psinserttime " +
                                                    "FROM playlists, playlistsongs, songs " +
                                                    "WHERE playlists.id=?::uuid AND playlistsongs.playlistid=playlists.id AND songs.id = playlistsongs.songid " +
                                                    "ORDER BY inserttime ASC");
                pStat.setString(1, playlist.getId().toString());

                ResultSet rSet = pStat.executeQuery();
                List<PlaylistItem> playlistItems = new ArrayList<> ();

                while (rSet.next()) {
                    playlistItems.add(
                        new PlaylistItem(
                            new Song(
                                UUID.fromString(rSet.getString("songid")),
                                rSet.getString("songname"),
                                null,
                                null,
                                rSet.getInt("songtrack"),
                                URI.create(rSet.getString("songuri"))
                            ),
                            rSet.getObject("psinserttime", LocalDateTime.class)
                        )
                    );
                }

                return playlistItems;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

	public DataFetcher<String> getNameOfPlaylistDataFetcher() {
        return dataFetchingEnvironment -> {
            Playlist playlist = dataFetchingEnvironment.getSource();

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement(
                                                    "SELECT name FROM playlists WHERE id=?::uuid");
                pStat.setString(1, playlist.getId().toString());

                ResultSet rSet = pStat.executeQuery();

                if (rSet.next()) {
                   return rSet.getString("name");
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
	}
}