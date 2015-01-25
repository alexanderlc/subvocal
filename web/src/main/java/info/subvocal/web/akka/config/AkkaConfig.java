package info.subvocal.web.akka.config;

import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.PoisonPill;
import akka.contrib.pattern.ClusterSingletonManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import info.subvocal.web.akka.actor.worker.distributed.Master;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;

/**
 * The Akka application configuration.
 *
 * // todo remove the manual join with seed names, Docker should handle much of this config
 */
@Configuration
class AkkaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaConfig.class);

    // reference for creation of ActorRef beans
    private ActorSystem actorSystem;

    private static String systemName = "Workers";
    private static FiniteDuration workTimeout = Duration.create(10, "seconds");

    // the application context is needed to initialize the Akka Spring Extension
    @Inject
    private ApplicationContext applicationContext;

    @Bean
    public Address clusterJoinAddress() throws InterruptedException {

        // todo work out the detail for why we have 2 masters/backend, with only the 2nd one connected to the frontend

        BackendStartupResponse backendStartupResponse = startBackend(null, "backend1");
        Address joinAddress = backendStartupResponse.getJoinAddress();
        Thread.sleep(1000);
        return startBackend(joinAddress, "backend2").getJoinAddress();
    }

    private static BackendStartupResponse startBackend(Address joinAddress, String role) {

        LOGGER.info("startBackend called");
        Config conf = ConfigFactory.load().getConfig(role).
                withFallback(ConfigFactory.load());

        // create the backend actor system
        ActorSystem system = ActorSystem.create(systemName, conf);

        // todo work out how this cluster stuff works
//        Address realJoinAddress =
//                (joinAddress == null) ? Cluster.get(system).selfAddress() : joinAddress;
//        Cluster.get(system).join(realJoinAddress);

        // create the master singleton
        system.actorOf(ClusterSingletonManager.defaultProps(Master.props(workTimeout), "active",
                PoisonPill.getInstance(), "backend"), "master");

        return new BackendStartupResponse(system, null);
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
}
