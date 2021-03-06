package info.subvocal.service.sentiment.repository.inmemory;

import info.subvocal.service.sentiment.entity.Sentiment;
import info.subvocal.service.sentiment.entity.SentimentType;
import info.subvocal.service.sentiment.repository.SentimentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *  Mickey mouse in memory implementations that supports concurrent access and duplicate checking.
 */
@Repository
public class InMemorySentimentRepository implements SentimentRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemorySentimentRepository.class);

    private Map<String, Set<Sentiment>> sentimentDb = new ConcurrentHashMap<>();

    @Override
    public Sentiment createSentiment(String url, SentimentType sentimentType, String createdByUserId) {

        if (!sentimentDb.containsKey(url)) {
            // initialise sentiment set for url
            // todo review options for concurrent sets
            sentimentDb.put(url, new ConcurrentSkipListSet<Sentiment>());
        }

        Sentiment newSentiment = new Sentiment(url, sentimentType, createdByUserId);

        Set<Sentiment> existingSentiments = sentimentDb.get(url);

        // todo this block probably needs a lock around it

        // todo this doesn't work
        if (existingSentiments.contains(newSentiment)) {
            // todo test how this behaves in the actor system, perhaps use a checked exception?
            throw new IllegalStateException("Sentiment already exists for this user");
        } else {
            // "save in db"
            sentimentDb.get(url).add(newSentiment);
            LOGGER.info("Sentiment persisted: {}", newSentiment);
            return newSentiment;
        }
    }

    @Override
    public int countSentimentForUrl(String url) {
        if (sentimentDb.containsKey(url)) {
            return sentimentDb.get(url).size();
        } else {
            return 0;
        }
    }

    @Override
    public List<Sentiment> get10MostRecentSentimentsForUrl(@NotNull String url) {
        // todo test this and show set sorted by date is correct direction
        List<Sentiment> sentiments = new ArrayList<>(sentimentDb.get(url));
        if (sentiments.size() > 10) {
            sentiments = sentiments.subList(0, 10);
        }

        return sentiments;
    }
}
