package info.subvocal.web.akka.actor.worker;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import info.subvocal.sentiment.entity.Sentiment;
import info.subvocal.web.akka.actor.message.Work;
import info.subvocal.web.akka.actor.worker.distributed.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

/**
 *  Dummy sentiment worker
 */
public class SentimentWorker extends Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentWorker.class);

    public SentimentWorker(ActorRef clusterClient, Props workExecutorProps, FiniteDuration registerInterval) {
        super(clusterClient, workExecutorProps, registerInterval);
    }

    @Override
    public Work.WorkType getWorkType() {
        return Work.WorkType.CREATE_SENTIMENT;
    }

    public static class SentimentWorkerExecutor extends UntypedActor {
        @Override
        public void onReceive(Object message) {
            if (message instanceof Work.CreateSentiment) {
                Sentiment sentiment = ((Work.CreateSentiment) message).getSentiment();

                LOGGER.info("TODO create sentiment {}", sentiment);
                getSender().tell(new Worker.WorkComplete(sentiment), getSelf());
            }
        }
    }
}
