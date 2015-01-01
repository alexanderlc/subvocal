package info.subvocal.web.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import info.subvocal.web.akka.actor.CountingActor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;

import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *  Public API REST endpoints
 */
@RestController
@RequestMapping(value = "/api/v1.0", produces = "application/json")
public class ApiController {

    @Inject
    private ActorSystem actorSystem;

    @ResponseBody
    @RequestMapping(value = "_ping", method = GET)
    public ResponseEntity<Boolean> ping() {
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "_count-once", method = GET)
    public ResponseEntity<Integer> count() throws Exception {

        // The counter actor will exist for the duration of a single method call

        // use the Spring Extension to create props for a named actor bean
        ActorRef counter = actorSystem.actorOf(
                SpringExtProvider.get(actorSystem).props("CountingActor"), "counter");

        // tell it to count once
        counter.tell(new CountingActor.Count(), null);

        // print and return the result
        FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);
        Future<Object> result = ask(counter, new CountingActor.Get(),
                Timeout.durationToTimeout(duration));
        try {
            Integer countResult = (Integer) Await.result(result, duration);
            System.out.println("Got back " + countResult);
            return new ResponseEntity<>(countResult, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Failed getting result: " + e.getMessage());
            throw e;
        } finally {
            // we are done with the actor - stop it
            actorSystem.stop(counter);
        }
    }
}
