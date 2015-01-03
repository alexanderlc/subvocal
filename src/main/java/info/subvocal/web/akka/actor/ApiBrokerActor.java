package info.subvocal.web.akka.actor;

import akka.actor.UntypedActor;
import info.subvocal.web.akka.actor.message.CreateSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

/**
 * An actor that behaves as a broker between the REST api and the rest of the actor system.
 *
 * There will be a new actorOf/instance for every API request/message that exists only for the lifetime of the request.
 * This is done so the API controller maintains concurrent operation.
 *
 * todo try to remove the spring stuff, this actor does not/might not need to be spring managed, try creating normally in the controller
 */
@Named("ApiBrokerActor")
@Scope("prototype")
public class ApiBrokerActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiBrokerActor.class);

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CreateSentiment) {

            LOGGER.info("ApiBrokerActor: Received message: {}", message);

            // tell the sentimentActor to handle this
            getContext().actorSelection("/user/sentimentActor").tell(message, null);

            // the actor's job is now done, stop
            getContext().stop(getSelf());

        } else {
            unhandled(message);
        }
    }
}
