package info.subvocal.web.akka.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import info.subvocal.web.akka.actor.message.CreateSentiment;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;

import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;

/**
 * An actor that acts as a broker between the REST api and the dependent actors such as persistence and metrics.
 *
 * In future this should be change so that this actor simple broadcasts the event to system, or maybe a pub/sub model.
 *
 * @note The scope here is prototype since we want to create a new actor
 * instance for each use of this bean.
 */
@Named("SentimentActor")
@Scope("prototype")
public class SentimentActor extends UntypedActor {

    @Inject
    private ActorSystem actorSystem;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof CreateSentiment) {

            // asynchronously persist the sentiment
            ActorRef sentimentPersistenceActor = actorSystem.actorOf(
                    SpringExtProvider.get(actorSystem).props("SentimentPersistenceActor"), "sentimentPersistenceActor");
            sentimentPersistenceActor.tell(message, null);

            // todo check if we need to stop the sentimentPersistenceActor
            // count it

        } else {
            unhandled(message);
        }
    }
}
