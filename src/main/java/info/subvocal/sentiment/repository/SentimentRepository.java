package info.subvocal.sentiment.repository;

import info.subvocal.sentiment.entity.Sentiment;
import info.subvocal.sentiment.entity.SentimentType;

/**
 *  Handles persistence of sentiment entities.
 *
 *  Future implementation will write to MongoDB
 */
public interface SentimentRepository {

    Sentiment createSentiment(String url, SentimentType sentimentType, String createdByUserId);

    int countSentimentForUrl(String url);
}
