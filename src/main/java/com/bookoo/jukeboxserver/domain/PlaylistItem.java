package com.bookoo.jukeboxserver.domain;

import java.time.LocalDateTime;

public class PlaylistItem {
    private Song song;
    private LocalDateTime timestamp;

    public PlaylistItem(Song song, LocalDateTime timestamp) {
        this.song = song;
        this.timestamp = timestamp;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((song == null) ? 0 : song.hashCode());
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
        PlaylistItem other = (PlaylistItem) obj;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        if (song == null) {
            if (other.song != null)
                return false;
        } else if (!song.equals(other.song))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PlaylistItem [timestamp=" + timestamp + ", song=" + song + "]";
    }
}