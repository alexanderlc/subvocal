package info.subvocal.sentiment.repository.inmemory;

import info.subvocal.service.sentiment.entity.SentimentType;
import info.subvocal.service.sentiment.repository.SentimentRepository;
import info.subvocal.service.sentiment.repository.inmemory.InMemorySentimentRepository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *  Unit tests for InMemorySentimentRepository
 */
public class InMemorySentimentRepositoryUnitTest {

    @Test
    public void testCreateSentiment_whenOk_ThenActuallyPersisted() {

        // given: new database
        SentimentRepository sentimentRepository = new InMemorySentimentRepository();
        String url = "/test.html";
        assertEquals(0, sentimentRepository.countSentimentForUrl(url));

        // when: add new sentiment
        sentimentRepository.createSentiment(url, SentimentType.DISLIKE, "1");

        // then: db should now have an entry for url
        assertEquals(1, sentimentRepository.countSentimentForUrl(url));
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSentiment_whenAddedTwice_ThenThrowsException() {

        // given: new database
        SentimentRepository sentimentRepository = new InMemorySentimentRepository();
        String url = "/test.html";
        assertEquals(0, sentimentRepository.countSentimentForUrl(url));

        // when: add same sentiment twice
        sentimentRepository.createSentiment(url, SentimentType.DISLIKE, "1");
        sentimentRepository.createSentiment(url, SentimentType.DISLIKE, "1");
    }
}
