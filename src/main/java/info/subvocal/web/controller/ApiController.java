package info.subvocal.web.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import info.subvocal.sentiment.entity.Sentiment;
import info.subvocal.web.akka.actor.CountingActor;
import info.subvocal.web.akka.actor.SentimentPersistenceActor;
import info.subvocal.web.akka.actor.message.CreateSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static info.subvocal.web.akka.spring.SpringExtension.SpringExtProvider;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *  Public API REST endpoints
 */
@RestController
@RequestMapping(value = "/api/v1.0", produces = "application/json")
public class ApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Inject
    private ActorSystem actorSystem;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody String handleException(Exception e) throws Exception {
        String message = "Api general failure: " + e.getMessage();
        LOGGER.error(message, e);
        return message;
    }

    @ResponseBody
    @RequestMapping(value = "_ping", method = GET)
    public ResponseEntity<Boolean> ping() {
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "sentiment", method = POST)
    // todo use custom exception and add an exception handler
    public ResponseEntity<String> createSentiment(
            @Valid @RequestBody Sentiment sentiment,
            BindingResult bindingResult
    ) throws Exception {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>("Invalid sentiment", HttpStatus.BAD_REQUEST);
        }

        try {
            // Tell the sentiment actor to create the sentiment.
            // This is a fire and forget, the API is async and does not guarantee it will actually be created
            apiBrokerActor().tell(new CreateSentiment(sentiment), null);
            return new ResponseEntity<>("Create sentiment request received", HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.error("Failed to initiate sentiment request: {}" + e);
            throw e;
        }
    }

    @ResponseBody
    @RequestMapping(value = "sentiments", method = GET)
    public ResponseEntity<List<Sentiment>> getSentiments(
            @RequestParam String url
    ) throws Exception {

        final Timeout t = new Timeout(Duration.create(5, TimeUnit.SECONDS));

        Future<Object> futureResult
                = ask(apiBrokerActor(), new SentimentPersistenceActor.Get10Sentiments(url), t); // using 1000ms timeout

        try {
            List<Sentiment> result = (List<Sentiment>) Await.result(futureResult, Duration.create(5, TimeUnit.SECONDS));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Failed getting result: " + e.getMessage());
            throw e;
        }
    }

    @ResponseBody
    @RequestMapping(value = "_count-once", method = GET)
    public ResponseEntity<Integer> count() throws Exception {

        // The counter actor will exist for the duration of a single method call

        // use the Spring Extension to create props for a named actor bean
        ActorRef counter = actorSystem.actorOf(
                SpringExtProvider.get(actorSystem).props("CountingActor"), "counter_"
                        + UUID.randomUUID());

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
//            actorSystem.stop(counter);
        }
    }

    // The apiBrokerActor actor will exist for the duration of a single API method call
    // use the Spring Extension to create props for a named actor bean
    // Add a random name to avoid any non-unique name exceptions
    private ActorRef apiBrokerActor() {
        return actorSystem.actorOf(
                SpringExtProvider.get(actorSystem).props("ApiBrokerActor"), "apiBrokerActor_" + UUID.randomUUID());
    }
}
