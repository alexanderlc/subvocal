package info.subvocal.web.akka.actor.message;

import info.subvocal.sentiment.entity.Sentiment;

import javax.validation.constraints.NotNull;

/**
 * Message that indicates a sentiment to be created
 */
public class CreateSentiment {

    private Sentiment sentiment;

    public CreateSentiment(@NotNull Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }
}
