package info.subvocal.web.akka.actor.worker;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import info.subvocal.web.akka.actor.message.Work;
import info.subvocal.web.akka.actor.worker.distributed.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

/**
 *  Example worker that can square
 */
public class SquareWorker extends Worker {

    public SquareWorker(ActorRef clusterClient, Props workExecutorProps, FiniteDuration registerInterval) {
        super();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SquareWorker.class);

    @Override
    public Work.WorkType getWorkType() {
        return Work.WorkType.SQUARE;
    }

    public static class SquareWorkerExecutor extends UntypedActor {
        @Override
        public void onReceive(Object message) {
            if (message instanceof Work.Square) {
                Integer n = ((Work.Square) message).getOperand();
                int n2 = n * n;
                String result = n + " * " + n + " = " + n2;
                LOGGER.debug("Produced result {}", result);
                getSender().tell(new Worker.WorkComplete(result), getSelf());
            }
        }
    }


}
