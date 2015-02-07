package info.subvocal.service.sentiment;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;

import static info.subvocal.spring.SpringExtension.SpringExtProvider;

/**
 *
 */
@Profile("sentiment")
@Configuration
public class WorkerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerConfig.class);

    @Inject
    private ApplicationContext applicationContext;

    // todo work out why we have to create system with the same system name?
    private static String systemName = "Workers";

    @Bean
    public ActorSystem workerActorSystem() {
        LOGGER.info("workerActorSystem called");
        ActorSystem workerActorSystem = ActorSystem.create(systemName);
        SpringExtProvider.get(workerActorSystem).initialize(applicationContext);
        return workerActorSystem;
    }
}
