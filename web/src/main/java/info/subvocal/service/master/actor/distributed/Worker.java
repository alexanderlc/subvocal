package info.subvocal.service.master.actor.distributed;

import akka.actor.ActorInitializationException;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.DeathPactException;
import akka.actor.OneForOneStrategy;
import akka.actor.ReceiveTimeout;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.contrib.pattern.ClusterClient.SendToAll;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.UUID;

import static akka.actor.SupervisorStrategy.Directive;
import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;
import static info.subvocal.service.master.actor.distributed.Master.Ack;
import static info.subvocal.service.master.actor.distributed.MasterWorkerProtocol.RegisterWorker;
import static info.subvocal.service.master.actor.distributed.MasterWorkerProtocol.WorkFailed;
import static info.subvocal.service.master.actor.distributed.MasterWorkerProtocol.WorkIsDone;
import static info.subvocal.service.master.actor.distributed.MasterWorkerProtocol.WorkIsReady;
import static info.subvocal.service.master.actor.distributed.MasterWorkerProtocol.WorkerRequestsWork;

/**
 * We should support many worker nodes and we assume that they can be unstable. Therefore we don't let the worker nodes
 * be members of the cluster, instead they communicate with the cluster through the Cluster Client. The worker doesn't
 * have to know exactly where the master is located.
 *
 * The worker register itself periodically to the master, see the registerTask. This has the nice characteristics that
 * master and worker can be started in any order, and in case of master fail over the worker re-register itself to the
 * new master.
 *
 * When the worker receives work from the master it delegates the actual processing to a child actor, WorkExecutor,
 * to keep the worker responsive while executing the work.
 *
 * NOTE: The worker operates as a finite state machine (FSM); IDLE (start state), WORKING and waitForWorkIsDoneAck.
 * These are separate behaviours that handle different sets of messages that can occur between the master,
 * the worker and the work executor.
 * The behaviours are switched using the actor context become() method.
 *
 * Update: Worker is abstract. Subclasses can work on a single WorkType, and provide an executor to do the actual work.
 */
public abstract class Worker extends UntypedActor {

    /**
     * Actor through which the worker will communicate to the master (and therefore the source of the work)
     */
    @Inject
    private ActorRef clusterClient;

    /**
     * A scheduled tasks to regularly re-register with the master
     */
    private Cancellable registerTask;

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final String workerId = UUID.randomUUID().toString();

    /**
     * ID of the piece of work currently being executed
     */
    private String currentWorkId = null;

    @PostConstruct
    public void init() {
        /**
         * The time between worker registrations with the master
         */
        final FiniteDuration registerInterval = Duration.create(10, "seconds");

        this.registerTask = getContext().system().scheduler().schedule(Duration.Zero(), registerInterval,
                clusterClient, new SendToAll("/user/master/active", new RegisterWorker(workerId, getWorkType())),
                getContext().dispatcher(), getSelf());
    }

    protected abstract Work.WorkType getWorkType();

    protected abstract ActorRef getWorkerExecutor();

    private String workId() {
        if (currentWorkId != null)
            return currentWorkId;
        else
            throw new IllegalStateException("Not working");
    }

    /**
     * How to handle the child work executor actor.
     * Report exceptions as work failures to the master, then restart the child.
     *
     * @return SuperStrategy for workExecutor
     */
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(-1, Duration.Inf(),
                new Function<Throwable, Directive>() {
                    @Override
                    public Directive apply(Throwable t) {
                        if (t instanceof ActorInitializationException)
                            return stop();
                        else if (t instanceof DeathPactException)
                            return stop();
                        else if (t instanceof Exception) {
                            if (currentWorkId != null) {
                                sendToMaster(new WorkFailed(workerId, getWorkType(), workId()));
                            }
                            getContext().become(idle);
                            return restart();
                        }
                        else {
                            return escalate();
                        }
                    }
                }
        );
    }

    @Override
    public void postStop() {
        registerTask.cancel();
    }

    /**
     * Note: the worker
     * @param message
     */
    public void onReceive(Object message) {
        // the worker does not support message handling
        unhandled(message);
    }

    /**
     * Behavior, which are procedures -> functions without a return value:
     * for how the worker handles messages in the idle state.
     */
    private final Behavior idle = new Behavior() {
        public void apply(Object message) {
            if (message instanceof MasterWorkerProtocol.WorkIsReady)
                sendToMaster(new MasterWorkerProtocol.WorkerRequestsWork(workerId, getWorkType()));
            else if (Work.class.isAssignableFrom(message.getClass())) {
                Work work = (Work) message;
                log.info("Got work: {}", work.toString());
                currentWorkId = work.workId;
                getWorkerExecutor().tell(work, getSelf());
                getContext().become(working);
            }
            else unhandled(message);
        }
    };

    /**
     * Message handling for the working state
     */
    private final Behavior working = new Behavior() {
        public void apply(Object message) {
            if (message instanceof WorkComplete) {
                Object result = ((WorkComplete) message).result;
                log.info("Work is complete. Result {}.", result);
                sendToMaster(new WorkIsDone(workerId, getWorkType(), workId(), result));
                getContext().setReceiveTimeout(Duration.create(5, "seconds"));
                getContext().become(waitForWorkIsDoneAck(result));
            }
            else if (message instanceof Work) {
                log.info("Yikes. Master told me to do work, while I'm working.");
            }
            else {
                unhandled(message);
            }
        }
    };

    /**
     * @param result work result
     * @return Behaviour for message handling when work is done,
     * but waiting for acknowledgement of this fact from master.
     */
    private Behavior waitForWorkIsDoneAck(final Object result) {
        return new Behavior() {
            public void apply(Object message) {
                if (message instanceof Ack && ((Ack) message).workId.equals(workId())) {
                    sendToMaster(new WorkerRequestsWork(workerId, getWorkType()));
                    getContext().setReceiveTimeout(Duration.Undefined());
                    getContext().become(idle);
                }
                else if (message instanceof ReceiveTimeout) {
                    log.info("No ack from master, retrying (" + workerId + " -> " + workId() + ")");
                    sendToMaster(new WorkIsDone(workerId, getWorkType(), workId(), result));
                }
                else {
                    unhandled(message);
                }
            }
        };
    }

    {
        getContext().become(idle);
    }

    @Override
    public void unhandled(Object message) {
        if (message instanceof Terminated && ((Terminated) message).getActor().equals(getWorkerExecutor())) {
            getContext().stop(getSelf());
        }
        else if (message instanceof WorkIsReady) {
            // do nothing
        }
        else {
            super.unhandled(message);
        }
    }

    private void sendToMaster(Object msg) {
        clusterClient.tell(new SendToAll("/user/master/active", msg), getSelf());
    }

    public static final class WorkComplete implements Serializable {
        public final Object result;

        public WorkComplete(Object result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "WorkComplete{" +
                    "result=" + result +
                    '}';
        }
    }
}
