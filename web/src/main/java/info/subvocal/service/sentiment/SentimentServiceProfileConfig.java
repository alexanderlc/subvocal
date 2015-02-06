package info.subvocal.service.sentiment;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.contrib.pattern.ClusterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static info.subvocal.spring.SpringExtension.SpringExtProvider;

/**
 * Sentiment service config
 */
@Profile("sentiment")
@Configuration
@ComponentScan("info.subvocal.service.sentiment")
public class SentimentServiceProfileConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentServiceProfileConfig.class);

    @Inject
    private ActorSystem workerActorSystem;

    @Bean
    public ActorRef clusterClient() {
        LOGGER.info("Info clusterClient");
        Set<ActorSelection> initialContacts = new HashSet<>();
        // todo out impact of removing the join address here?
        initialContacts.add(workerActorSystem.actorSelection("/user/receptionist"));

        // create the client client
        return workerActorSystem.actorOf(ClusterClient.defaultProps(initialContacts),
                "clusterClient");
    }

    @Bean
    public ActorRef sentimentWorker() {
        LOGGER.info("Fired sentimentWorker");
        return workerActorSystem.actorOf(
                SpringExtProvider.get(workerActorSystem).props("SentimentWorker"), "sentimentWorker");
    }

    @Bean
    public ActorRef sentimentWorkerExecutor() {
        LOGGER.info("Fired sentimentWorkerExecutor");
        return workerActorSystem.actorOf(
                SpringExtProvider.get(workerActorSystem).props("SentimentWorkerExecutor"), "sentimentWorkerExecutor");
    }
}
