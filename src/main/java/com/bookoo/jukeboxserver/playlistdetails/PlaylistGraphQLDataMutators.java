package com.bookoo.jukeboxserver.playlistdetails;

import java.sql.SQLException;
import java.util.Map;

import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class PlaylistGraphQLDataMutators {
    
    @Autowired
    private DAO dao;

    @SuppressWarnings("unchecked")
    public DataFetcher<Playlist> createPlaylistMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");
            String name = (String) map.get("name");

            try {
                Playlist playlist = dao.createPlaylist(name);

                if (playlist != null) return playlist;

                throw new RuntimeException("Unknown error while creating playlist");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

	public DataFetcher<Playlist> addSongToPlaylistMutator() {
		return dataFetchingEnvironment -> {
            Map<String, Object> args = dataFetchingEnvironment.getArguments();

            String playlistId = (String) args.get("playlistId");
            String songId = (String) args.get("songId");

            try {
                Playlist playlist = dao.addSongToPlaylist(playlistId, songId);

                if (playlist != null) return playlist;

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
	}

	public DataFetcher<Playlist> removeSongFromPlaylistMutator() {
		return dataFetchingEnvironment -> {
            Map<String, Object> args = dataFetchingEnvironment.getArguments();

            String playlistId = (String) args.get("playlistId");
            String songId = (String) args.get("songId");
            String timestamp = (String) args.get("timestamp");

            try {
                dao.removeSongFromPlaylist(playlistId, songId, timestamp);

                return dao.getPlaylist(playlistId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
	}
}