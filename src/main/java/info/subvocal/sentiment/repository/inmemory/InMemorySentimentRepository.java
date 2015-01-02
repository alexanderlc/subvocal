package info.subvocal.sentiment.repository.inmemory;

import info.subvocal.sentiment.entity.Sentiment;
import info.subvocal.sentiment.entity.SentimentType;
import info.subvocal.sentiment.repository.SentimentRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *  Mickey mouse in memory implementations that supports concurrent access and duplicate checking.
 */
@Repository
public class InMemorySentimentRepository implements SentimentRepository {

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
        if (existingSentiments.contains(newSentiment)) {
            // todo test how this behaves in the actor system, perhaps use a checked exception?
            throw new IllegalStateException("Sentiment already exists for this user");
        } else {
            // "save in db"
            sentimentDb.get(url).add(newSentiment);
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
}
