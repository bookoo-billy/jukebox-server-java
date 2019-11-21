package com.bookoo.jukeboxserver;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;

import com.bookoo.jukeboxserver.albumdetails.AlbumGraphQLDataFetchers;
import com.bookoo.jukeboxserver.albumdetails.AlbumGraphQLDataMutators;
import com.bookoo.jukeboxserver.artistdetails.ArtistGraphQLDataFetchers;
import com.bookoo.jukeboxserver.artistdetails.ArtistGraphQLDataMutators;
import com.bookoo.jukeboxserver.playlistdetails.PlaylistGraphQLDataFetchers;
import com.bookoo.jukeboxserver.playlistdetails.PlaylistGraphQLDataMutators;
import com.bookoo.jukeboxserver.songdetails.SongGraphQLDataFetchers;
import com.bookoo.jukeboxserver.songdetails.SongGraphQLDataMutators;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;

@Component
public class GraphQLProvider {

    private GraphQL jukebox;

    @Autowired
    private SongGraphQLDataFetchers songFetchers;

    @Autowired
    private SongGraphQLDataMutators songMutators;

    @Autowired
    private ArtistGraphQLDataFetchers artistFetchers;

    @Autowired
    private ArtistGraphQLDataMutators artistMutators;

    @Autowired
    private AlbumGraphQLDataFetchers albumFetchers;

    @Autowired
    private AlbumGraphQLDataMutators albumMutators;

    @Autowired
    private PlaylistGraphQLDataFetchers playlistFetchers;

    @Autowired
    private PlaylistGraphQLDataMutators playlistMutators;

    public GraphQLProvider() {
        this.songFetchers = new SongGraphQLDataFetchers();
        this.songMutators = new SongGraphQLDataMutators();
        this.artistFetchers = new ArtistGraphQLDataFetchers();
        this.artistMutators = new ArtistGraphQLDataMutators();
        this.albumFetchers = new AlbumGraphQLDataFetchers();
        this.albumMutators = new AlbumGraphQLDataMutators();
    }

    @Bean
    public GraphQL jukebox() { 
        return jukebox;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.jukebox = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
      TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
      RuntimeWiring runtimeWiring = buildWiring();
      SchemaGenerator schemaGenerator = new SchemaGenerator();
      return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                    .type(TypeRuntimeWiring.newTypeWiring("Query")
                            .dataFetcher("songById", songFetchers.getSongByIdDataFetcher())
                            .dataFetcher("artistById", artistFetchers.getArtistByIdDataFetcher())
                            .dataFetcher("albumById", albumFetchers.getAlbumByIdDataFetcher())
                            .dataFetcher("playlistById", playlistFetchers.getPlaylistByIdDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Song")
                            .dataFetcher("artist", songFetchers.getArtistOfSongDataFetcher())
                            .dataFetcher("album", songFetchers.getAlbumOfSongDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Artist")
                            .dataFetcher("albums", artistFetchers.getAlbumsOfArtistDataFetcher())
                            .dataFetcher("songs", artistFetchers.getSongsOfArtistDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Album")
                            .dataFetcher("artist", albumFetchers.getArtistOfAlbumDataFetcher())
                            .dataFetcher("songs", albumFetchers.getSongsOfAlbumDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Playlist")
                            .dataFetcher("items", playlistFetchers.getItemsOfPlaylistDataFetcher())
                            .dataFetcher("name", playlistFetchers.getNameOfPlaylistDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                            .dataFetcher("createSong", songMutators.createSongMutator())
                            .dataFetcher("createArtist", artistMutators.createArtistMutator())
                            .dataFetcher("createAlbum", albumMutators.createAlbumMutator())
                            .dataFetcher("createPlaylist", playlistMutators.createPlaylistMutator())
                            .dataFetcher("addSongToPlaylist", playlistMutators.addSongToPlaylistMutator())
                            .dataFetcher("removeSongFromPlaylist", playlistMutators.removeSongFromPlaylistMutator()))
                    .build();
    }
}
