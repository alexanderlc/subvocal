package info.subvocal.service.sentiment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Immutable Sentiment
 */
public class Sentiment implements Comparable, Serializable {

    /**
     * URL for the subject of the sentiment
     */
    @NotNull
    private String url;

    /**
     * The type of sentiment felt for the subject
     */
    @NotNull
    private SentimentType sentimentType;

    /**
     * The time at which the sentiment was made
     */
    private Date created;

    /**
     * UUID of the user/session responsible for the sentiment
     */
    @NotNull
    private String createdByUserId;

    @JsonCreator
    public Sentiment(
            @JsonProperty("url") @NotNull String url,
            @JsonProperty("sentimentType") @NotNull SentimentType sentimentType,
            @JsonProperty("createdByUserId") @NotNull String createdByUserId) {
        this.url = url;
        this.sentimentType = sentimentType;
        this.createdByUserId = createdByUserId;
        created = new Date();
    }

    public String getUrl() {
        return url;
    }

    public SentimentType getSentimentType() {
        return sentimentType;
    }

    public Date getCreated() {
        return created;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    /**
     * This equals allows sentiments to be compared in collections.
     *
     * Created date is not including, as users cannot have the same sentiment twice.
     *
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sentiment sentiment = (Sentiment) o;

        if (!createdByUserId.equals(sentiment.createdByUserId)) return false;
        if (sentimentType != sentiment.sentimentType) return false;
        if (!url.equals(sentiment.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 1;
        if (sentimentType != null) {
            result = 31 * result + sentimentType.hashCode();
        }
        if (createdByUserId != null) {
            result = 31 * result + createdByUserId.hashCode();
        }
        return result;
    }

    /**
     *  The compare to is used by the sorted set on created date (InMemorySentimentRepository)
     */
    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;
        if (o == null || getClass() != o.getClass()) throw new IllegalArgumentException("Invalid class for compareTo");

        Sentiment sentiment = (Sentiment) o;

        return this.getCreated().compareTo(sentiment.getCreated());
    }

    @Override
    public String toString() {
        return "Sentiment{" +
                "url='" + url + '\'' +
                ", sentimentType=" + sentimentType +
                ", created=" + created +
                ", createdByUserId='" + createdByUserId + '\'' +
                '}';
    }
}
