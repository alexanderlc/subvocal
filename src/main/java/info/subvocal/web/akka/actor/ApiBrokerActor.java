package info.subvocal.web.akka.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.dispatch.Mapper;
import akka.dispatch.Recover;
import akka.util.Timeout;
import info.subvocal.web.akka.actor.message.Work;
import info.subvocal.web.akka.actor.worker.Master;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static akka.pattern.Patterns.ask;

/**
 * An actor that behaves as a broker between the REST api and the rest of the actor system.
 *
 * There will be a new actorOf/instance for every API request/message that exists only for the lifetime of the request.
 * This is done so the API controller maintains concurrent operation.
 *
 * The Frontend actor sends the work to the active master via the DistributedPubSubMediator.
 * It doesn't care about the exact location of the master.
 * Somewhere in the cluster there should be one master actor running.
 * The message is sent with ask/? to be able to reply to the client (ApiController) when the job has been accepted
 * or denied by the master.
 *
 * todo try to remove the spring stuff, this actor does not/might not need to be spring managed, try creating normally in the controller
 */
@Named("ApiBrokerActor")
@Scope("prototype")
public class ApiBrokerActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiBrokerActor.class);

    final ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();

    private Map<String, ActorRef> apiCallbacks = new HashMap<>();

    {
        // subscribe to work results, we are assuming all work results are relevant for the API to response with
        mediator.tell(new DistributedPubSubMediator.Subscribe(Master.ResultsTopic, getSelf()), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {

        LOGGER.info("ApiBrokerActor: Received message: {}", message);

        if (Work.class.isAssignableFrom(message.getClass())) {

            apiCallbacks.put(((Work) message).workId, getSender());
            forwardWorkToBackend(message);

            // todo Work out if we still want one actor per request
            // the actor's job is now done, stop
//            getContext().stop(getSelf());

        } else if (message instanceof DistributedPubSubMediator.SubscribeAck) {
            // do nothing

        } else if (message instanceof Master.WorkResult) {
            Master.WorkResult workResult = (Master.WorkResult) message;
            LOGGER.info("Consumed result: {}", workResult.result);

            // supply the work result to the original calling actor
            apiCallbacks.get(workResult.workId).tell(workResult.result, getSelf());
            // clean up work
            apiCallbacks.remove(workResult.workId);
        }else {
            unhandled(message);
        }
    }

    /**
     * Ask the cluster to fulfill the request by processing the work
     * @param message a supported API request
     */
    private void forwardWorkToBackend(Object message) {
        try {

            LOGGER.info("Forwarding message to master for distribution");

            // work is sent via the master actor, f being a future message
            DistributedPubSubMediator.Send send
                    = new DistributedPubSubMediator.Send("/user/master/active", message, false);

            Future<Object> f =
                    ask(mediator, send,
                            new Timeout(Duration.create(5, "seconds")));

            final ExecutionContext ec = getContext().system().dispatcher();

            // reply to the client (ApiController) when the job has been accepted or denied by the master
            Future<Object> res = f.map(new Mapper<Object, Object>() {
                @Override
                public Object apply(Object msg) {
                    if (msg instanceof Master.Ack)
                        return Ok.getInstance();
                    else
                        return super.apply(msg);
                }
            }, ec).recover(new Recover<Object>() {
                @Override
                public Object recover(Throwable failure) throws Throwable {
                    return NotOk.getInstance();
                }
            }, ec);

            // todo handle the NotOK case back to the caller
            // todo overwise wait for the real response
//            pipe(res, ec).to(getSender());

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    public static final class Ok implements Serializable {
        private Ok() {}

        private static final Ok instance = new Ok();

        public static Ok getInstance() {
            return instance;
        }

        @Override
        public String toString() {
            return "Ok";
        }
    }

    public static final class NotOk implements Serializable {
        private NotOk() {}

        private static final NotOk instance = new NotOk();

        public static NotOk getInstance() {
            return instance;
        }

        @Override
        public String toString() {
            return "NotOk";
        }
    }
}
