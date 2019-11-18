package com.bookoo.jukeboxserver.domain;

import java.net.URI;
import java.util.UUID;

public class Song {
    private UUID id;
    private String name;
    private Album album;
    private Artist artist;
    private Integer track;
    private URI uri;

    public Song(UUID id, String name, Album album, Artist artist, Integer track, URI uri) {
        this.id = id;
        this.name = name;
        this.album = album;
        this.artist = artist;
        this.track = track;
        this.uri = uri;
    }

    public UUID getUuid() {
        return id;
    }

    public void setUuid(UUID uuid) {
        this.id = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Integer getTrack() {
        return track;
    }

    public void setTrack(Integer track) {
        this.track = track;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((album == null) ? 0 : album.hashCode());
        result = prime * result + ((artist == null) ? 0 : artist.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((track == null) ? 0 : track.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Song other = (Song) obj;
        if (album == null) {
            if (other.album != null)
                return false;
        } else if (!album.equals(other.album))
            return false;
        if (artist == null) {
            if (other.artist != null)
                return false;
        } else if (!artist.equals(other.artist))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (track == null) {
            if (other.track != null)
                return false;
        } else if (!track.equals(other.track))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Song [album=" + album + ", artist=" + artist + ", name=" + name + ", track=" + track + ", id=" + id
                + ", uri=" + uri + "]";
    }
}