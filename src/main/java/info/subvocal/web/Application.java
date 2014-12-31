package info.subvocal.web;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import info.subvocal.web.akka.actor.CountingActor.Count;
import info.subvocal.web.akka.actor.CountingActor.Get;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;

/**
 *  main method for app
 */
@Configuration
@ComponentScan("info.subvocal.web.controller")
@EnableWebMvc
//@EnableTransactionManagement(proxyTargetClass = true)
@EnableAutoConfiguration(
        // stop the various stuff from auto firing after scanning our Controller classes
        exclude = {
                HibernateJpaAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                VelocityAutoConfiguration.class})
public class Application extends WebMvcConfigurerAdapter {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);

        // create a spring context and scan the classes
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext();
        ctx.scan("info.subvocal.web.akka");
        ctx.refresh();

        // get hold of the actor system
        ActorSystem system = ctx.getBean(ActorSystem.class);
        // use the Spring Extension to create props for a named actor bean
        ActorRef counter = system.actorOf(
                SpringExtProvider.get(system).props("CountingActor"), "counter");

        // tell it to count three times
        counter.tell(new Count(), null);
        counter.tell(new Count(), null);
        counter.tell(new Count(), null);

        // print the result
        FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);
        Future<Object> result = ask(counter, new Get(),
                Timeout.durationToTimeout(duration));
        try {
            System.out.println("Got back " + Await.result(result, duration));
        } catch (Exception e) {
            System.err.println("Failed getting result: " + e.getMessage());
            throw e;
        } finally {
            system.shutdown();
            system.awaitTermination();
        }
    }
}
