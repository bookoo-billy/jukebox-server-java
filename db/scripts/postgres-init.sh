#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL

    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    CREATE TABLE IF NOT EXISTS artists (
        id uuid DEFAULT uuid_generate_v4(),
        name VARCHAR(500) NOT NULL,
        PRIMARY KEY (id),
        UNIQUE (name)
    );

    CREATE TABLE IF NOT EXISTS albums (
        id uuid DEFAULT uuid_generate_v4(),
        name VARCHAR(500) NOT NULL,
        artistid uuid REFERENCES artists(id),
        PRIMARY KEY (id),
        UNIQUE (name, artistid)
    );

    CREATE TABLE IF NOT EXISTS songs (
        id uuid DEFAULT uuid_generate_v4(),
        name VARCHAR(500) NOT NULL,
        track SMALLINT,
        artistid uuid REFERENCES artists(id),
        albumid uuid REFERENCES albums(id),
        uri VARCHAR(1000),
        PRIMARY KEY (id),
        UNIQUE (name, artistid, albumid)
    );

    CREATE TABLE IF NOT EXISTS albumsongs (
        albumid uuid REFERENCES albums(id),
        songid uuid REFERENCES songs(id),
        PRIMARY KEY (albumid, songid)
    );

    CREATE TABLE IF NOT EXISTS playlists (
        id uuid DEFAULT uuid_generate_v4(),
        name VARCHAR(500) NOT NULL,
        PRIMARY KEY (id),
        UNIQUE (name)
    );

    CREATE TABLE IF NOT EXISTS playlistsongs (
        playlistid uuid REFERENCES playlists(id),
        songid uuid REFERENCES songs(id),
        inserttime timestamp DEFAULT now(),
        PRIMARY KEY (playlistid, songid, inserttime)
    );

    CREATE MATERIALIZED VIEW search_index AS
    SELECT artists.name AS artistname, albums.name AS albumname, songs.name AS songname,
            artists.id AS artistid, albums.id AS albumid, songs.id AS songid,
            songs.track AS songtrack, songs.uri AS songuri,
            setweight(to_tsvector('english', artists.name), 'A') ||
            setweight(to_tsvector('english', albums.name), 'B') ||
            setweight(to_tsvector('english', songs.name), 'C') as document
    FROM artists, albums, songs
    WHERE songs.artistid = artists.id AND
            songs.albumid = albums.id
    GROUP BY artists.id, albums.id, songs.id;
EOSQL
