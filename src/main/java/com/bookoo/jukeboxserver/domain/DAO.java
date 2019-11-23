package com.bookoo.jukeboxserver.domain;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bookoo.jukeboxserver.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DAO.class);

    @Autowired
    private Config config;

    public Artist createArtist(String name) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "INSERT INTO artists(name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name=? RETURNING *");
        pStat.setString(1, name);
        pStat.setString(2, name);

        if (pStat.execute()) {
            ResultSet rSet = pStat.getResultSet();
            if (rSet.next()) {
                return new Artist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
            }
        }

        return null;
    }

    public Album createAlbum(String name, String artistId) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "INSERT INTO albums(name, artistid) VALUES (?, ?::uuid) ON CONFLICT (name, artistid) DO UPDATE SET name=?, artistid=?::uuid RETURNING *");
        pStat.setString(1, name);
        pStat.setString(2, artistId);
        pStat.setString(3, name);
        pStat.setString(4, artistId);

        if (pStat.execute()) {
            ResultSet rSet = pStat.getResultSet();
            if (rSet.next()) {
                return new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
            }
        }

        return null;
    }

    public Album createAlbum(String name, Artist artist) throws SQLException {
        return createAlbum(name, artist.getId().toString());
    }

    public Song createSong(String name, String artistId, String albumId, Integer track, URI uri) throws SQLException {
        PreparedStatement pStatSongs = config.dbConnection().prepareStatement(
                "INSERT INTO songs(name, artistid, albumid, track, uri) VALUES (?, ?::uuid, ?::uuid, ?, ?) ON CONFLICT (name, artistid, albumid) DO UPDATE SET name=?, artistid=?::uuid, albumid=?::uuid, track=?, uri=? RETURNING *");

        pStatSongs.setString(1, name);
        pStatSongs.setString(2, artistId);
        pStatSongs.setString(3, albumId);

        if (track == null) {
            pStatSongs.setNull(4, Types.INTEGER);
        } else {
            pStatSongs.setInt(4, track);
        }

        pStatSongs.setString(5, uri.toString());

        pStatSongs.setString(6, name);
        pStatSongs.setString(7, artistId);
        pStatSongs.setString(8, albumId);

        if (track == null) {
            pStatSongs.setNull(9, Types.INTEGER);
        } else {
            pStatSongs.setInt(9, track);
        }

        pStatSongs.setString(10, uri.toString());

        if (pStatSongs.execute()) {
            ResultSet rSet = pStatSongs.getResultSet();
            if (rSet.next()) {
                return new Song(UUID.fromString(rSet.getString("id")), rSet.getString("name"),
                        new Album(UUID.fromString(albumId), null, null, null),
                        new Artist(UUID.fromString(artistId), null, null, null), rSet.getInt("track"),
                        URI.create(rSet.getString("uri")));
            }
        }

        return null;
    }

    public Song createSong(String name, Artist artist, Album album, Integer track, URI uri) throws SQLException {
        return createSong(name, artist.getId().toString(), album.getId().toString(), track, uri);
    }

    public Playlist createPlaylist(String name) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "INSERT INTO playlists(name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name=? RETURNING *");
        pStat.setString(1, name);
        pStat.setString(2, name);

        if (pStat.execute()) {
            ResultSet rSet = pStat.getResultSet();
            if (rSet.next()) {
                return new Playlist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null);
            }
        }

        return null;
    }

    public Playlist addSongToPlaylist(String playlistId, String songId) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "INSERT INTO playlistsongs(playlistid, songid) VALUES(?::uuid, ?::uuid) ON CONFLICT (playlistid, songid, inserttime) DO UPDATE SET playlistid=?::uuid, songid=?::uuid RETURNING *");

        pStat.setString(1, playlistId);
        pStat.setString(2, songId);
        pStat.setString(3, playlistId);
        pStat.setString(4, songId);

        if (pStat.execute()) {
            ResultSet rSet = pStat.getResultSet();
            if (rSet.next()) {
                return new Playlist(UUID.fromString(rSet.getString("playlistid")), null, null);
            }
        }

        return null;
    }

    public Playlist removeSongFromPlaylist(String playlistId, String songId, String timestamp) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "DELETE FROM playlistsongs WHERE playlistid=?::uuid AND songid=?::uuid AND inserttime=? RETURNING *");

        pStat.setString(1, playlistId);
        pStat.setString(2, songId);
        pStat.setObject(3, LocalDateTime.parse(timestamp));

        if (pStat.execute()) {
            ResultSet rSet = pStat.getResultSet();
            if (rSet.next()) {
                return new Playlist(UUID.fromString(playlistId), null, null);
            }
        }

        return null;
    }

    public Playlist getPlaylist(String playlistId) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT * FROM playlists WHERE id::text=?");
        pStat.setString(1, playlistId);

        ResultSet rSet = pStat.executeQuery();
        if (rSet.next()) {
            return new Playlist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null);
        }

        return null;
    }

    public List<PlaylistItem> getItemsOfPlaylist(Playlist playlist) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack, songs.uri AS songuri, playlistsongs.inserttime AS psinserttime "
                        + "FROM playlists, playlistsongs, songs "
                        + "WHERE playlists.id=?::uuid AND playlistsongs.playlistid=playlists.id AND songs.id = playlistsongs.songid "
                        + "ORDER BY inserttime ASC");
        pStat.setString(1, playlist.getId().toString());

        ResultSet rSet = pStat.executeQuery();
        List<PlaylistItem> playlistItems = new ArrayList<>();

        while (rSet.next()) {
            playlistItems.add(new PlaylistItem(
                    new Song(UUID.fromString(rSet.getString("songid")), rSet.getString("songname"), null, null,
                            rSet.getInt("songtrack"), URI.create(rSet.getString("songuri"))),
                    rSet.getObject("psinserttime", LocalDateTime.class)));
        }

        return playlistItems;
    }

    public String getNameOfPlaylist(Playlist playlist) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT name FROM playlists WHERE id=?::uuid");
        pStat.setString(1, playlist.getId().toString());

        ResultSet rSet = pStat.executeQuery();

        if (rSet.next()) {
            return rSet.getString("name");
        }

        return null;
    }

    public Song getSongById(String songId) throws SQLException {
        PreparedStatement pStat = config.dbConnection()
                .prepareStatement("SELECT id, name, track, artistid, albumid, uri FROM songs WHERE songs.id=?::uuid");
        pStat.setString(1, songId);

        ResultSet rSet = pStat.executeQuery();

        if (rSet.next()) {
            return new Song(UUID.fromString(rSet.getString("id")), rSet.getString("name"),
                    new Album(UUID.fromString(rSet.getString("albumid")), null, null, null),
                    new Artist(UUID.fromString(rSet.getString("artistid")), null, null, null), rSet.getInt("track"),
                    URI.create(rSet.getString("uri")));
        }

        return null;
    }

    public Artist getArtistOfSong(Song song) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT * FROM artists WHERE id::text=?");
        pStat.setString(1, song.getArtist().getId().toString());

        ResultSet rSet = pStat.executeQuery();

        if (rSet.next()) {
            return new Artist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
        }

        return null;
    }

    public Album getAlbumOfSong(Song song) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT * FROM albums WHERE id::text=?");
        pStat.setString(1, song.getAlbum().getId().toString());

        ResultSet rSet = pStat.executeQuery();

        if (rSet.next()) {
            return new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null,
                    new Artist(UUID.fromString(rSet.getString("artistId")), null, null, null));
        }

        return null;
    }

    public Artist getArtistById(String artistId) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT * FROM artists WHERE id::text=?");
        pStat.setString(1, artistId);

        ResultSet rSet = pStat.executeQuery();
        if (rSet.next()) {
            return new Artist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
        }

        return null;
    }

    public List<Album> getAlbumsOfArtist(Artist artist) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT * FROM albums WHERE artistid::text=?");
        pStat.setString(1, artist.getId().toString());

        ResultSet rSet = pStat.executeQuery();
        List<Album> albums = new ArrayList<>();

        while (rSet.next()) {
            albums.add(new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null));
        }

        return albums;
    }

    public List<Song> getSongsOfArtist(Artist artist) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack, songs.uri AS songuri, artists.id AS artistid, artists.name AS artistname, albums.id AS albumid, albums.name as albumname FROM songs, artists, albums WHERE songs.artistid=?::uuid AND songs.artistId = artists.id AND songs.albumId = albums.id");
        pStat.setString(1, artist.getId().toString());

        ResultSet rSet = pStat.executeQuery();
        List<Song> songs = new ArrayList<>();

        while (rSet.next()) {
            songs.add(new Song(UUID.fromString(rSet.getString("songid")), rSet.getString("songname"),
                    new Album(UUID.fromString(rSet.getString("albumid")), null, null, null), artist,
                    rSet.getInt("songtrack"), URI.create(rSet.getString("songuri"))));
        }

        return songs;
    }

    public List<Song> getSongsOfAlbum(Album album) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement(
                "SELECT songs.id AS songid, songs.name AS songname, songs.track AS songtrack, songs.uri AS songuri FROM songs WHERE songs.albumid=?::uuid");
        pStat.setString(1, album.getId().toString());

        ResultSet rSet = pStat.executeQuery();
        List<Song> songs = new ArrayList<>();

        while (rSet.next()) {
            songs.add(new Song(UUID.fromString(rSet.getString("songid")), rSet.getString("songname"), album, null,
                    rSet.getInt("songtrack"), URI.create(rSet.getString("songuri"))));
        }

        return songs;
    }

    public Artist getArtistOfAlbum(Album album) throws SQLException {
        PreparedStatement pStat = config.dbConnection()
                .prepareStatement("SELECT id, name FROM artists WHERE id=?::uuid");
        pStat.setString(1, album.getArtist().getId().toString());

        ResultSet rSet = pStat.executeQuery();

        if (rSet.next()) {
            return new Artist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
        }

        return null;
    }

    public Album getAlbumById(String albumId) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT * FROM albums WHERE id::text=?");
        pStat.setString(1, albumId);

        ResultSet rSet = pStat.executeQuery();
        if (rSet.next()) {
            return new Album(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null,
                    new Artist(UUID.fromString(rSet.getString("artistId")), null, null, null));
        }

        return null;
    }

    public List<Song> searchSongs(String search) throws SQLException {
        PreparedStatement pStat = config.dbConnection().prepareStatement("SELECT * FROM search_index WHERE document @@ plainto_tsquery(?)");
        pStat.setString(1, search);

        ResultSet rSet = pStat.executeQuery();
        ArrayList<Song> songs = new ArrayList<> ();

        while (rSet.next()) {
            songs.add(new Song(UUID.fromString(rSet.getString("songid")), rSet.getString("songname"), new Album(UUID.fromString(rSet.getString("albumId"))), new Artist(UUID.fromString(rSet.getString("artistid"))), rSet.getInt("songtrack"), URI.create(rSet.getString("songuri"))));
        }

        return songs;
    }

    @Scheduled(fixedRate=5 * 1000 * 60) //Every 5 minutes reindex
    public void refreshSearchIndex() throws SQLException {
        LOGGER.info("Started refreshing search_index materialized view");
        config.dbConnection().prepareStatement("REFRESH MATERIALIZED VIEW search_index").execute();
        LOGGER.info("Finished refreshing search_index materialized view");
    }
}