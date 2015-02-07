package info.subvocal.service.api;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import info.subvocal.service.api.actor.ApiBrokerActor;
import info.subvocal.web.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Config for the API service
 */
@Profile("api")
@Configuration
@ComponentScan("info.subvocal.service.api")
public class ApiProfileConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiProfileConfig.class);

    @Bean
    public ActorRef frontend() {
        LOGGER.info("startFrontend called");
        // create the frontend actor system
        ActorSystem system = ActorSystem.create(Application.CLUSTER_NAME);
        return system.actorOf(Props.create(ApiBrokerActor.class), "frontend");
    }
}
