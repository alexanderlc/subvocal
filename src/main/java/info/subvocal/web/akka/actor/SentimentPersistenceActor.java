package info.subvocal.web.akka.actor;

import akka.actor.UntypedActor;
import info.subvocal.sentiment.repository.SentimentRepository;
import info.subvocal.web.akka.actor.message.CreateSentiment;
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

    @Inject
    private SentimentRepository sentimentRepository;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof CreateSentiment) {
            CreateSentiment createSentiment = (CreateSentiment) message;
            sentimentRepository.createSentiment(
                    createSentiment.getUrl(),
                    createSentiment.getSentimentType(),
                    createSentiment.getCreatedByUserId());

        } else {
            unhandled(message);
        }
    }
}
