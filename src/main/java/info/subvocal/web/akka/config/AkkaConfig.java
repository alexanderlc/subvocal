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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

    private Address realJoinAddress;

    private static String systemName = "Workers";
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

    @Bean
    public ActorSystem backendActorSystem() throws InterruptedException {

        // todo work out the detail for why we have 2 masters/backend, with only the 2nd one connected to the frontend

        BackendStartupResponse backendStartupResponse = startBackend(null, "backend");
        Address joinAddress = backendStartupResponse.getJoinAddress();
        Thread.sleep(1000);
        realJoinAddress = startBackend(joinAddress, "backend").getJoinAddress();
        return backendStartupResponse.getActorSystem();
    }

    @Bean
    public ActorSystem workerActorSystem() throws InterruptedException {
        ActorSystem workerActorSystem = startWorker(realJoinAddress);
        Thread.sleep(1000);
        return workerActorSystem;
    }

    @Bean
    public ActorRef frontend() {
        return startFrontend(realJoinAddress);
    }

    private static BackendStartupResponse startBackend(Address joinAddress, String role) {

        LOGGER.info("startBackend called");
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[" + role + "]").
                withFallback(ConfigFactory.load());

        // create the backend actor system
        ActorSystem system = ActorSystem.create(systemName, conf);

        // todo work out how this cluster stuff works
        Address realJoinAddress =
                (joinAddress == null) ? Cluster.get(system).selfAddress() : joinAddress;
        Cluster.get(system).join(realJoinAddress);

        // create the master singleton
        system.actorOf(ClusterSingletonManager.defaultProps(Master.props(workTimeout), "active",
                PoisonPill.getInstance(), role), "master");

        return new BackendStartupResponse(system, realJoinAddress);
    }

    private static class BackendStartupResponse {
        private ActorSystem actorSystem;
        private Address joinAddress;

        private BackendStartupResponse(ActorSystem actorSystem, Address joinAddress) {
            this.actorSystem = actorSystem;
            this.joinAddress = joinAddress;
        }

        public ActorSystem getActorSystem() {
            return actorSystem;
        }

        public Address getJoinAddress() {
            return joinAddress;
        }
    }

    private static ActorSystem startWorker(Address contactAddress) {
        LOGGER.info("startWorker called");

        // create the worker actor system
        ActorSystem system = ActorSystem.create(systemName);

        Set<ActorSelection> initialContacts = new HashSet<ActorSelection>();
        initialContacts.add(system.actorSelection(contactAddress + "/user/receptionist"));

        // create the client client
        ActorRef clusterClient = system.actorOf(ClusterClient.defaultProps(initialContacts),
                "clusterClient");

        // create the worker - they manage their own executor
        system.actorOf(Worker.props(clusterClient, Props.create(WorkExecutor.class)), "worker");

        return system;
    }

    private static ActorRef startFrontend(Address joinAddress) {
        LOGGER.info("startFrontend called");
        // create the frontend actor system
        ActorSystem system = ActorSystem.create(systemName);

        Cluster.get(system).join(joinAddress);

        ActorRef frontend = system.actorOf(Props.create(ApiBrokerActor.class), "frontend");
//        system.actorOf(Props.create(WorkProducer.class, frontend), "producer");
//        system.actorOf(Props.create(WorkResultConsumer.class), "consumer");

        return frontend;
    }
}
