package com.bookoo.jukeboxserver.domain;

import java.util.Set;
import java.util.UUID;

public class Artist {
    private UUID id;
    private String name;
    private Set<Album> albums;
    private Set<Song> songs;

    public Artist() {
        this.id = null;
        this.name = null;
        this.albums = null;
        this.songs = null;
    }

    public Artist(UUID id) {
        this.id = id;
        this.name = null;
        this.albums = null;
        this.songs = null;
    }

    public Artist(UUID id, String name, Set<Album> albums, Set<Song> songs) {
        this.id = id;
        this.name = name;
        this.albums = albums;
        this.songs = songs;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(Set<Album> albums) {
        this.albums = albums;
    }

    public Set<Song> getSongs() {
        return songs;
    }

    public void setSongs(Set<Song> songs) {
        this.songs = songs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((albums == null) ? 0 : albums.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((songs == null) ? 0 : songs.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Artist other = (Artist) obj;
        if (albums == null) {
            if (other.albums != null)
                return false;
        } else if (!albums.equals(other.albums))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (songs == null) {
            if (other.songs != null)
                return false;
        } else if (!songs.equals(other.songs))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Artist [albums=" + albums + ", name=" + name + ", songs=" + songs + ", id=" + id + "]";
    }
}