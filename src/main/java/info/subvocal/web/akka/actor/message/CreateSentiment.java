package info.subvocal.web.akka.actor.message;

import info.subvocal.sentiment.entity.Sentiment;
import info.subvocal.sentiment.entity.SentimentType;

/**
 * Message that indicates a sentiment to be created
 */
public class CreateSentiment extends Sentiment {

    public CreateSentiment(String url, SentimentType sentimentType, String createdByUserId) {
        super(url, sentimentType, createdByUserId);
    }
}
