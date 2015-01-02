package info.subvocal.web.akka.actor;

import akka.actor.UntypedActor;
import info.subvocal.sentiment.repository.SentimentRepository;
import info.subvocal.web.akka.actor.message.CreateSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * An actor that persists each sentiment created.
 *
 */
@Named("SentimentPersistenceActor")
@Scope("prototype")
public class SentimentPersistenceActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentActor.class);

    @Inject
    private SentimentRepository sentimentRepository;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof CreateSentiment) {

            LOGGER.info("SentimentPersistenceActor: Received message: {}", message);

            CreateSentiment createSentiment = (CreateSentiment) message;
            sentimentRepository.createSentiment(
                    createSentiment.getSentiment().getUrl(),
                    createSentiment.getSentiment().getSentimentType(),
                    createSentiment.getSentiment().getCreatedByUserId());

        } else {
            unhandled(message);
        }
    }
}
