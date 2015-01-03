package info.subvocal.web.akka.actor;

import akka.actor.UntypedActor;
import info.subvocal.web.akka.actor.message.CreateSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

/**
 * An actor that acts aggregates sub tasks/actors such as persistence and metrics for Sentiment.
 * Sub tasks are initiated asynchronously.
 *
 * In future this should be change so that this actor simple broadcasts the event to system, or maybe a pub/sub model.
 *
 * @note The scope here is prototype since we want to create a new actor
 * instance for each use of this bean.
 */
@Named("SentimentActor")
@Scope("prototype")
public class SentimentActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentActor.class);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof CreateSentiment) {

            LOGGER.info("SentimentActor: Received message: {}", message);

            // persist the sentiment
            getContext().actorSelection("/user/sentimentPersistenceActor").tell(message, null);

            // count it

        } else {
            unhandled(message);
        }
    }
}
