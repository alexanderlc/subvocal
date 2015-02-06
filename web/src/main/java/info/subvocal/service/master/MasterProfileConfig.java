package info.subvocal.service.master;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.contrib.pattern.ClusterSingletonManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import info.subvocal.service.master.actor.distributed.Master;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * Created by paul on 06/02/15.
 */
@Profile("master")
@Configuration
@ComponentScan("info.subvocal.service.master")
public class MasterProfileConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterProfileConfig.class);
    private static FiniteDuration workTimeout = Duration.create(10, "seconds");

    // todo work out why we have to create system with the same system name?
    private static String systemName = "Workers";

    @Bean
    public ActorRef master() throws InterruptedException {

        // todo work out the detail for why we have 2 masters/backend, with only the 2nd one connected to the frontend
        ActorRef master = startBackend("backend1");
        Thread.sleep(1000);

        return master;
    }

    @Bean
    public ActorRef master2() throws InterruptedException {
        ActorRef master = startBackend("backend2");
        Thread.sleep(1000);
        return master;
    }

    private static ActorRef startBackend(String role) {

        LOGGER.info("startBackend called");
        Config conf = ConfigFactory.load().getConfig(role).
                withFallback(ConfigFactory.load());

        // create the backend actor system
        ActorSystem system = ActorSystem.create(systemName, conf);

        // create the master singleton
        return system.actorOf(ClusterSingletonManager.defaultProps(Master.props(workTimeout), "active",
                PoisonPill.getInstance(), "backend"), "master");
    }
}
