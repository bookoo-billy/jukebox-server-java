package com.bookoo.jukeboxserver.artistdetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import com.bookoo.jukeboxserver.config.Config;
import com.bookoo.jukeboxserver.domain.Artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class ArtistGraphQLDataMutators {

    @Autowired
    private Config config;

    @SuppressWarnings("unchecked")
    public DataFetcher<Artist> createArtistMutator() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = (Map<String, Object>) dataFetchingEnvironment.getArguments().get("input");
            String name = (String) map.get("name");

            try {
                PreparedStatement pStat = config.dbConnection()
                                                .prepareStatement("INSERT INTO artists(name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name=? RETURNING *");
                pStat.setString(1, name);
                pStat.setString(2, name);

                if (pStat.execute()) {
                    ResultSet rSet = pStat.getResultSet();
                    if (rSet.next()) {
                        return new Artist(UUID.fromString(rSet.getString("id")), rSet.getString("name"), null, null);
                    }
                }

                throw new RuntimeException("Unknown error while adding song: " + pStat.getResultSet());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}