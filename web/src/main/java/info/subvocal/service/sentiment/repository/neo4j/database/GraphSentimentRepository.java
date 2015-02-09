package info.subvocal.service.sentiment.repository.neo4j.database;

import info.subvocal.service.sentiment.repository.neo4j.entity.Url;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created by paul on 08/02/15.
 */
public interface GraphSentimentRepository extends GraphRepository<Url> {

}
