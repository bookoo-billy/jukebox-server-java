package com.bookoo.jukeboxserver.songdetails;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;

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

@Component()
public class GraphQLProvider {

    private GraphQL songs;

    @Autowired
    private PostgresGraphQLDataFetchers graphQLDataFetchers;

    @Autowired
    private PostgresGraphQLDataMutators graphQLDataMutators;

    @Bean
    public GraphQL songs() { 
        return songs;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.songs = GraphQL.newGraphQL(graphQLSchema).build();
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
                            .dataFetcher("songById", graphQLDataFetchers.getSongByIdDataFetcher())
                            .dataFetcher("artistById", graphQLDataFetchers.getArtistByIdDataFetcher())
                            .dataFetcher("albumById", graphQLDataFetchers.getAlbumByIdDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Song")
                            .dataFetcher("artist", graphQLDataFetchers.getArtistOfSongDataFetcher())
                            .dataFetcher("album", graphQLDataFetchers.getAlbumOfSongDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Artist")
                            .dataFetcher("albums", graphQLDataFetchers.getAlbumsOfArtistDataFetcher())
                            .dataFetcher("songs", graphQLDataFetchers.getSongsOfArtistDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Album")
                            .dataFetcher("artist", graphQLDataFetchers.getArtistOfAlbumDataFetcher())
                            .dataFetcher("songs", graphQLDataFetchers.getSongsOfAlbumDataFetcher()))
                    .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                            .dataFetcher("createSong", graphQLDataMutators.createSongMutator())
                            .dataFetcher("createArtist", graphQLDataMutators.createArtistMutator())
                            .dataFetcher("createAlbum", graphQLDataMutators.createAlbumMutator()))
                    .build();
    }
}
