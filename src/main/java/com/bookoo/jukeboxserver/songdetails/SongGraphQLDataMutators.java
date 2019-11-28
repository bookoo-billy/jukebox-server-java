package com.bookoo.jukeboxserver.songdetails;

import java.net.URI;
import java.sql.SQLException;
import java.util.Map;

import com.bookoo.jukeboxserver.MediaPlayer;
import com.bookoo.jukeboxserver.domain.DAO;
import com.bookoo.jukeboxserver.domain.Song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class SongGraphQLDataMutators {

    @Autowired
    private DAO dao;

    @Autowired
    private MediaPlayer mediaPlayer;

    @SuppressWarnings("unchecked")
    public DataFetcher<Song> createSongMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");

            String name = (String) map.get("name");
            String artistId = (String) map.get("artistId");
            String albumId = (String) map.get("albumId");
            Integer track = (Integer) map.get("track");
            URI uri = URI.create((String) map.get("uri"));

            try {
                Song song = dao.createSong(name, artistId, albumId, track, uri);

                if (song != null) return song;

                throw new RuntimeException("Unknown error while adding song");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

	public DataFetcher<Song> playSongMutator() {
		return dataFetchingEnvironment -> {
            String songId = (String) dataFetchingEnvironment.getArguments().get("songId");

            try {
                Song song = dao.getSongById(songId);

                if (song != null) {
                    return mediaPlayer.play(song);
                }

                throw new RuntimeException("Could not find song with ID " + songId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
	}

	public DataFetcher<Song> pauseSongMutator() {
		return dataFetchingEnvironment -> {
            return mediaPlayer.pause();
        };
    }

	public DataFetcher<Song> resumeSongMutator() {
		return dataFetchingEnvironment -> {
            return mediaPlayer.resume();
        };
    }
}