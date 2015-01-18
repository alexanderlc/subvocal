package info.subvocal.web.akka.config;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterClient;
import akka.contrib.pattern.ClusterSingletonManager;
import info.subvocal.web.akka.actor.ApiBrokerActor;
import info.subvocal.web.akka.actor.worker.Master;
import info.subvocal.web.akka.actor.worker.WorkExecutor;
import info.subvocal.web.akka.actor.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;

/**
 * The Akka application configuration.
 */
@Configuration
class AkkaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaConfig.class);

    // reference for creation of ActorRef beans
    private ActorSystem actorSystem;

    private static FiniteDuration workTimeout = Duration.create(10, "seconds");

    // the application context is needed to initialize the Akka Spring Extension
    @Inject
    private ApplicationContext applicationContext;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {
        LOGGER.debug("Fired actorSystem");
        ActorSystem system = ActorSystem.create("AkkaJavaSpring");
        // initialize the application context in the Akka Spring Extension
        SpringExtProvider.get(system).initialize(applicationContext);
        this.actorSystem = system;
        return system;
    }

    /**
     * @return Single actor for Sentiment
     */
    @Bean
    public ActorRef sentimentActor() {
        LOGGER.debug("Fired sentimentActor");
        return actorSystem.actorOf(
                SpringExtProvider.get(actorSystem).props("SentimentActor"), "sentimentActor");
    }

    /**
     * @return Single actor for Sentiment Persistence
     */
    @Bean
    public ActorRef sentimentPersistenceActor() {
        LOGGER.debug("Fired sentimentPersistenceActor");
        return actorSystem.actorOf(
                SpringExtProvider.get(actorSystem).props("SentimentPersistenceActor"), "sentimentPersistenceActor");
    }

    /**
     *  what follows is a simplified setup with a single actor system
     */

    @Bean
    public ActorRef master() throws InterruptedException {

        // todo work out the detail for why we have 2 masters/backend, with only the 2nd one connected to the frontend

        BackendStartupResponse backendStartupResponse = startBackend(actorSystem, null);
        Address joinAddress = backendStartupResponse.getJoinAddress();
        Thread.sleep(5000);
        return backendStartupResponse.getMaster();
    }

    @Bean
    public ActorRef worker() throws InterruptedException {
        ActorRef worker = startWorker(actorSystem);
        Thread.sleep(5000);
        return worker;
    }

    @Bean
    public ActorRef frontend() {
        return startFrontend(actorSystem);
    }

    private static BackendStartupResponse startBackend(ActorSystem actorSystem, String role) {

        LOGGER.info("startBackend called");

        // todo work out how this cluster stuff works
        Address joinAddress = Cluster.get(actorSystem).selfAddress();
        Cluster.get(actorSystem).join(joinAddress);

        // create the master singleton
        ActorRef master = actorSystem.actorOf(ClusterSingletonManager.defaultProps(Master.props(workTimeout), "active",
                PoisonPill.getInstance(), role), "master");

        return new BackendStartupResponse(master, joinAddress);
    }

    private static class BackendStartupResponse {
        private ActorRef master;
        private Address joinAddress;

        private BackendStartupResponse(ActorRef master, Address joinAddress) {
            this.master = master;
            this.joinAddress = joinAddress;
        }

        public ActorRef getMaster() {
            return master;
        }

        public Address getJoinAddress() {
            return joinAddress;
        }
    }

    private static ActorRef startWorker(ActorSystem actorSystem) {
        LOGGER.info("startWorker called");

        Address contactAddress = Cluster.get(actorSystem).selfAddress();

        Set<ActorSelection> initialContacts = new HashSet<>();
        initialContacts.add(actorSystem.actorSelection(contactAddress + "/user/receptionist"));

        // create the client client
        ActorRef clusterClient = actorSystem.actorOf(ClusterClient.defaultProps(initialContacts),
                "clusterClient");

        // create the worker - they manage their own executor
        return actorSystem.actorOf(Worker.props(clusterClient, Props.create(WorkExecutor.class)), "worker");
    }

    private static ActorRef startFrontend(ActorSystem actorSystem) {
        LOGGER.info("startFrontend called");

        return actorSystem.actorOf(Props.create(ApiBrokerActor.class), "frontend");
    }
}
