package info.subvocal.service.master.actor.distributed;

import info.subvocal.service.sentiment.entity.Sentiment;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *  Complete set of message classes that describe work tasks
 */
public abstract class Work implements Serializable {
    public final String workId;

    public Work(String workId) {
        this.workId = workId;
    }

    public abstract WorkType getWorkType();

    @Override
    public String toString() {
        return "Work{" +
                "workId='" + workId + '\'' +
                '}';
    }

    // todo the master component should not need to maintain the list of supported work types
    public enum WorkType {
        CREATE_SENTIMENT, COUNT_SENTIMENT
    }

    public static final class CreateSentiment extends Work {

        private Sentiment sentiment;

        public CreateSentiment(String workId, @NotNull Sentiment sentiment) {
            super(workId);
            this.sentiment = sentiment;
        }

        public Sentiment getSentiment() {
            return sentiment;
        }

        @Override
        public WorkType getWorkType() {
            return WorkType.CREATE_SENTIMENT;
        }
    }

    public static final class CountSentiment extends Work {

        private Sentiment sentiment;

        public CountSentiment(String workId, @NotNull Sentiment sentiment) {
            super(workId);
            this.sentiment = sentiment;
        }

        public Sentiment getSentiment() {
            return sentiment;
        }

        @Override
        public WorkType getWorkType() {
            return WorkType.COUNT_SENTIMENT;
        }
    }
}
