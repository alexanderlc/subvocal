package info.subvocal.web.akka.actor.message;

import info.subvocal.sentiment.entity.Sentiment;

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

    @Override
    public String toString() {
        return "Work{" +
                "workId='" + workId + '\'' +
                '}';
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
    }

    public static final class Square extends Work {
        private int operand;

        public Square(String workId, int operand) {
            super(workId);
            this.operand = operand;
        }

        public int getOperand() {
            return operand;
        }
    }
}
