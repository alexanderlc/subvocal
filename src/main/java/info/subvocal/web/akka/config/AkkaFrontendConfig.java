package info.subvocal.web.akka.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import info.subvocal.web.akka.actor.ApiBrokerActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * Created by paul on 18/01/15.
 */
@Configuration
public class AkkaFrontendConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaFrontendConfig.class);

    private static final String SYSTEM_NAME = "Workers";

    @Inject
    private Address clusterJoinAddress;

    @Bean
    public ActorRef frontend() {
        LOGGER.info("startFrontend called");
        // create the frontend actor system
        ActorSystem system = ActorSystem.create(SYSTEM_NAME);
        Cluster.get(system).join(clusterJoinAddress);
        return system.actorOf(Props.create(ApiBrokerActor.class), "frontend");
    }
}
