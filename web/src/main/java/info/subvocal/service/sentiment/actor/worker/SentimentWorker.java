package info.subvocal.service.sentiment.actor.worker;

import akka.actor.ActorRef;
import info.subvocal.service.master.actor.distributed.Work;
import info.subvocal.service.master.actor.distributed.Worker;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *  Sentiment worker
 */
@Named("SentimentWorker")
@Scope("prototype")
public class SentimentWorker extends Worker {

    @Inject
    private ActorRef sentimentWorkerExecutor;

    @Override
    public Work.WorkType getWorkType() {
        return Work.WorkType.CREATE_SENTIMENT;
    }

    @Override
    protected ActorRef getWorkerExecutor() {
        return sentimentWorkerExecutor;
    }

}
