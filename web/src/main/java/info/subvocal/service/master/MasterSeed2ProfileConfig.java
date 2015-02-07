package info.subvocal.service.master;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.contrib.pattern.ClusterSingletonManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import info.subvocal.service.master.actor.distributed.Master;
import info.subvocal.web.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * A backup seed and master
 */
@Profile("master-seed2")
@Configuration
public class MasterSeed2ProfileConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterSeed2ProfileConfig.class);
    private static FiniteDuration workTimeout = Duration.create(10, "seconds");

    @Bean
    public ActorRef master2() throws InterruptedException {
        ActorRef master = startBackend("master-seed2");
        Thread.sleep(1000);
        return master;
    }

    private ActorRef startBackend(String role) {

        LOGGER.info("startBackend called for role {}", role);
        Config conf = ConfigFactory.load().getConfig(role).
                withFallback(ConfigFactory.load());

        // create the backend actor system
        ActorSystem system = ActorSystem.create(Application.CLUSTER_NAME, conf);

        // create the master singleton
        return system.actorOf(ClusterSingletonManager.defaultProps(Master.props(workTimeout), "active",
                PoisonPill.getInstance(), "backend"), "master");
    }
}
