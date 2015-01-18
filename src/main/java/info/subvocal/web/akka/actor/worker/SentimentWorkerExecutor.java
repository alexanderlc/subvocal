package info.subvocal.web.akka.actor.worker;

import akka.actor.UntypedActor;
import info.subvocal.sentiment.entity.Sentiment;
import info.subvocal.sentiment.repository.SentimentRepository;
import info.subvocal.web.akka.actor.message.Work;
import info.subvocal.web.akka.actor.worker.distributed.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by paul on 18/01/15.
 */
@Named("SentimentWorkerExecutor")
@Scope("prototype")
public class SentimentWorkerExecutor extends UntypedActor {

    @Inject
    private SentimentRepository sentimentRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentWorkerExecutor.class);

    @Override
    public void onReceive(Object message) {
        if (message instanceof Work.CreateSentiment) {
            Sentiment createSentiment = ((Work.CreateSentiment) message).getSentiment();

            LOGGER.info("Create sentiment {}", createSentiment);
            sentimentRepository.createSentiment(
                    createSentiment.getUrl(),
                    createSentiment.getSentimentType(),
                    createSentiment.getCreatedByUserId());

            getSender().tell(new Worker.WorkComplete(createSentiment), getSelf());
        }
    }
}