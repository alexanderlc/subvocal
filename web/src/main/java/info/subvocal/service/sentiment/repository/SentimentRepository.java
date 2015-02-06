package info.subvocal.service.sentiment.repository;

import info.subvocal.service.sentiment.entity.Sentiment;
import info.subvocal.service.sentiment.entity.SentimentType;

import java.util.List;

/**
 *  Handles persistence of sentiment entities.
 *
 *  Future implementation will write to MongoDB
 *
 *  todo consider using Sentiment object rather than separate params
 */
public interface SentimentRepository {

    Sentiment createSentiment(String url, SentimentType sentimentType, String createdByUserId);

    int countSentimentForUrl(String url);

    List<Sentiment> get10MostRecentSentimentsForUrl(String url);
}
