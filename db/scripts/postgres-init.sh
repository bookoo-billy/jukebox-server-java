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

    GRANT ALL PRIVILEGES ON DATABASE jukebox TO jukebox;
EOSQL
