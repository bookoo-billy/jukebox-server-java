package com.bookoo.jukeboxserver.playlistdetails;

import java.sql.SQLException;
import java.util.List;

import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Playlist;
import com.bookoo.jukeboxserver.domain.PlaylistItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PlaylistGraphQLDataFetchers {

    @Autowired
    private DAO dao;

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
                return dao.getItemsOfPlaylist(playlist);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

	public DataFetcher<String> getNameOfPlaylistDataFetcher() {
        return dataFetchingEnvironment -> {
            Playlist playlist = dataFetchingEnvironment.getSource();

            try {
                return dao.getNameOfPlaylist(playlist);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

	public DataFetcher<List<Playlist>> searchPlaylistsDataFetcher() {
		return dataFetchingEnvironment -> {
            String search = dataFetchingEnvironment.getArgument("search");

            try {
                return dao.searchPlaylists(search);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
	}
}