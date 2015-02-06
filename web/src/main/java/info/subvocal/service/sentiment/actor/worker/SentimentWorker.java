package info.subvocal.service.sentiment.actor.worker;

import info.subvocal.service.master.actor.distributed.Work;
import info.subvocal.service.master.actor.distributed.Worker;
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
