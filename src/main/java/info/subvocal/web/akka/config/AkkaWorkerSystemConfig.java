package info.subvocal.web.akka.config;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.contrib.pattern.ClusterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;

/**
 *  We want the worker system to be spring managed so we can inject our service layer
 */
@Configuration
public class AkkaWorkerSystemConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaWorkerSystemConfig.class);

    @Inject
    private ActorSystem workerActorSystem;

    @Inject
    private Address clusterJoinAddress;

    @Bean
    public ActorRef clusterClient() {
        LOGGER.info("Info clusterClient");
        Set<ActorSelection> initialContacts = new HashSet<>();
        initialContacts.add(workerActorSystem.actorSelection(clusterJoinAddress + "/user/receptionist"));

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

//    private static ActorSystem startWorker(Address contactAddress) {


        // create the workers - they manage their own executor
//        system.actorOf(
//                workerProps(
//                        SquareWorker.class,
//                        clusterClient,
//                        workerExecutorProps(SquareWorker.SquareWorkerExecutor.class)),
//                "square-worker1");
//
//        system.actorOf(
//                workerProps(
//                        SquareWorker.class,
//                        clusterClient,
//                        workerExecutorProps(SquareWorker.SquareWorkerExecutor.class)),
//                "square-worker2");

//        system.actorOf(
//                workerProps(
//                        SentimentWorker.class,
//                        clusterClient,
//                        workerExecutorProps(SentimentWorkerExecutor.class)),
//                "sentiment-worker1");

//        return system;
//    }
}
