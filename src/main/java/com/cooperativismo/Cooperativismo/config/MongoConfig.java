package com.cooperativismo.Cooperativismo.config;

import com.cooperativismo.Cooperativismo.repository.PautaRepositoryMongo;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.UUID;

@EnableMongoRepositories(basePackageClasses = PautaRepositoryMongo.class)
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "cooperativismoVotacao";
    }
}