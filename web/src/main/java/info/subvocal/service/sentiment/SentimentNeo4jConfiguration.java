package info.subvocal.service.sentiment;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.TypeRepresentationStrategy;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.neo4j.support.typerepresentation.NoopRelationshipTypeRepresentationStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 */
@EnableTransactionManagement
@Import(RepositoryRestMvcConfiguration.class)
@EnableScheduling
@EnableAutoConfiguration
@Configuration
@EnableNeo4jRepositories(basePackages = "info.subvocal.service.sentiment.repository.neo4j.database")
public class SentimentNeo4jConfiguration extends Neo4jConfiguration {

    public SentimentNeo4jConfiguration() {
        setBasePackage("info.subvocal.service.sentiment.repository.neo4j.entity");
    }

    public static final String URL = System.getenv("NEO4J_URL") != null
            ? System.getenv("NEO4J_URL") : "http://localhost:7474/db/data/";

    @Bean
    public GraphDatabaseService graphDatabaseService() {
        return new SpringRestGraphDatabase(URL);
    }

    @Override
    public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy() throws Exception {
        return new NoopRelationshipTypeRepresentationStrategy();
    }
}
