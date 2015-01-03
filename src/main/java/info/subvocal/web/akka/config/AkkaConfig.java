package info.subvocal.web.akka.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;

/**
 * The Akka application configuration.
 */
@Configuration
class AkkaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaConfig.class);

    // reference for creation of ActorRef beans
    private ActorSystem actorSystem;

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
}
