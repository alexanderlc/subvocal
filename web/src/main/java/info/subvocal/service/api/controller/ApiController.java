package info.subvocal.service.api.controller;

import akka.actor.ActorRef;
import akka.util.Timeout;
import info.subvocal.service.api.actor.ApiBrokerActor;
import info.subvocal.service.api.controller.exception.BackendFailureException;
import info.subvocal.service.master.actor.distributed.Work;
import info.subvocal.service.sentiment.entity.Sentiment;
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
    private ActorRef frontend;

    @ResponseBody
    @RequestMapping(value = "_ping", method = GET)
    public ResponseEntity<Boolean> ping() {
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "sentiment", method = POST)
    public ResponseEntity<String> createSentiment(
            @Valid @RequestBody Sentiment sentiment,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>("Invalid sentiment", HttpStatus.BAD_REQUEST);
        }

        return handleResponseFromApiBroker(
                ask(frontend, new Work.CreateSentiment(nextWorkId(), sentiment), askTimeOut())
        );
    }

    @ResponseBody
    @RequestMapping(value = "sentiments", method = GET)
    public ResponseEntity<List<Sentiment>> getSentiments(
            @RequestParam String url
    ) throws Exception {

        return null;
    }


    /**
     * @param futureResult for some work to result in an API response
     * @param <T> The response type
     * @return OK response entity
     */
    private <T> ResponseEntity<T> handleResponseFromApiBroker(Future<Object> futureResult) {
        try {
            Object response = Await.result(futureResult, waitDuration());

            if (response instanceof ApiBrokerActor.NotOk) {
                throw new BackendFailureException(response.toString());
            }

            T result = (T) response;
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            // convert to a runtime exception
            LOGGER.error("Failed handleResponseFromApiBroker: {}", e.getMessage());
            throw new BackendFailureException(e.getMessage());
        }
    }

    /**
     * The time we wait for a future to comeback from akka
     */
    private Timeout askTimeOut() {
        return new Timeout(Duration.create(2, TimeUnit.SECONDS));
    }

    /**
     * The time we wait for our work to be completed
     */
    private FiniteDuration waitDuration() {
        return Duration.create(10, TimeUnit.SECONDS);
    }

    private String nextWorkId() {
        return UUID.randomUUID().toString();
    }


    // The apiBrokerActor actor will exist for the duration of a single API method call
    // use the Spring Extension to create props for a named actor bean
    // Add a random name to avoid any non-unique name exceptions
    private ActorRef apiBrokerActor() {
//        return actorSystem.actorOf(
//                SpringExtProvider.get(actorSystem).props("ApiBrokerActor"), "apiBrokerActor_" + UUID.randomUUID());
        return null;
    }

    // todo invalid param usage should not be caught here and result in 400 response code
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody String handleException(Exception e) throws Exception {
        String message = "Api general failure: " + e.getMessage();
        LOGGER.error(message, e);
        return message;
    }

    @ExceptionHandler(BackendFailureException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody String handleBackendFailureException(Exception e) throws Exception {
        String message = "Backend rejected request: " + e.getMessage();
        LOGGER.error(message, e);
        return message;
    }
}
