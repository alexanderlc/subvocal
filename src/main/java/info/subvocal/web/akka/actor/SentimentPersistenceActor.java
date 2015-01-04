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
        LOGGER.info("SentimentPersistenceActor: Received message: {}", message);

        if (message instanceof CreateSentiment) {
            CreateSentiment createSentiment = (CreateSentiment) message;
            sentimentRepository.createSentiment(
                    createSentiment.getSentiment().getUrl(),
                    createSentiment.getSentiment().getSentimentType(),
                    createSentiment.getSentiment().getCreatedByUserId());

        } else if (message instanceof Get10Sentiments) {
            handleGet10Sentiments((Get10Sentiments) message);

        } else {
            unhandled(message);
        }
    }

    private void handleGet10Sentiments(Get10Sentiments get10Sentiments) {
        try {
            getSender().tell(
                sentimentRepository.get10MostRecentSentimentsForUrl(get10Sentiments.getUrl()),
                getSelf());
        } catch (Exception e) {
            // todo review error handling
            getSender().tell(new akka.actor.Status.Failure(e), getSelf());
        }
    }

    public static class Get10Sentiments {
        private String url;

        public Get10Sentiments(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
