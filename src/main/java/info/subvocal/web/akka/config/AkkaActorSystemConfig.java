package info.subvocal.web.akka.config;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;

/**
 * Created by paul on 18/01/15.
 */
@Configuration
public class AkkaActorSystemConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaActorSystemConfig.class);

    // todo work out why we have to create system with the same system name?
    private static String systemName = "Workers";

    @Inject
    private ApplicationContext applicationContext;

    @Bean
    public ActorSystem workerActorSystem() {
        LOGGER.info("workerActorSystem called");
        ActorSystem workerActorSystem = ActorSystem.create(systemName);
        SpringExtProvider.get(workerActorSystem).initialize(applicationContext);
        return workerActorSystem;
    }
}
