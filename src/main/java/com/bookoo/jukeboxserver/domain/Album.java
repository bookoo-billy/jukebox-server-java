package com.bookoo.jukeboxserver.domain;

import java.util.List;
import java.util.UUID;

public class Album {
    private UUID id;
    private String name;
    private List<Song> songs;
    private Artist artist;

    public Album() {
        this.id = null;
        this.name = null;
        this.songs = null;
        this.artist = null;
    }

    public Album(UUID id) {
        this.id = id;
        this.name = null;
        this.songs = null;
        this.artist= null;
    }

    public Album(UUID id, String name, List<Song> songs, Artist artist) {
        this.id = id;
        this.name = name;
        this.songs = songs;
        this.artist = artist;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((songs == null) ? 0 : songs.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((artist == null) ? 0 : artist.hashCode());
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
        Album other = (Album) obj;
        if (songs == null) {
            if (other.songs != null)
                return false;
        } else if (!songs.equals(other.songs))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (artist == null) {
            if (other.artist != null)
                return false;
        } else if (!artist.equals(other.artist))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Album [songs=" + songs + ", name=" + name + ", id=" + id + ", artist=" + artist + "]";
    }
}