package info.subvocal.web.akka.actor.worker;

import info.subvocal.web.akka.actor.message.Work;
import info.subvocal.web.akka.actor.worker.distributed.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

/**
 *  Sentiment worker
 */
@Named("SentimentWorker")
@Scope("prototype")
public class SentimentWorker extends Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentWorker.class);

    @Override
    public Work.WorkType getWorkType() {
        return Work.WorkType.CREATE_SENTIMENT;
    }

}
