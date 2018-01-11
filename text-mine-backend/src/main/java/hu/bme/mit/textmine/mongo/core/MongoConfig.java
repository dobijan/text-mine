package hu.bme.mit.textmine.mongo.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

@Configuration
@EnableMongoRepositories(basePackages = "hu.bme.mit.textmine.mongo")
public class MongoConfig extends AbstractMongoConfiguration {

    @Value("${mongo.host}")
    private String mongoHost;

    @Value("${mongo.port}")
    private Integer mongoPort;

    @Value("${mongo.database}")
    private String mongoDatabase;

    @Override
    protected String getDatabaseName() {
        return this.mongoDatabase;
    }

    @Override
    protected String getMappingBasePackage() {
        return "hu.bme.mit.textmine.mongo";
    }

    @SuppressWarnings("deprecation")
    @Override
    public MongoClient mongoClient() {
        MongoClient c = new MongoClient(this.mongoHost, this.mongoPort);
        c.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        return c;
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }
}
