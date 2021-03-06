package info.subvocal.service.master.actor.distributed;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.contrib.pattern.DistributedPubSubMediator.Put;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * The heart of the solution is the Master actor that manages outstanding work and notifies registered workers when new
 * work is available.
 *
 * The Master actor is a singleton within the nodes with role "backend" in the cluster.
 * This means that there will be one active master actor in the cluster. It runs on the oldest node.
 *
 * Source modified from:
 * https://github.com/typesafehub/activator-akka-distributed-workers-java/blob/master/src/main/java/worker/Master.java
 *
 * Workers register itself to the master with RegisterWorker. Each worker has an unique identifier and the master keeps
 * track of the workers, including current ActorRef (sender of RegisterWorker message) that can be used for sending
 * notifications to the worker. This ActorRef is not a direct link to the worker actor, but messages sent to it will be
 * delivered to the worker. When using the cluster client messages are are tunneled via the receptionist on some node in
 * the cluster to avoid inbound connections from other cluster nodes to the client.
 *
 * When the master receives Work from front end it adds the work item to the queue of pending work and notifies idle
 * workers with WorkIsReady message.
 *
 * To be able to restore same state in case of fail over to a standby master actor the changes (domain events) are
 * stored in an append only transaction log and can be replayed when standby actor is started. This event sourcing is
 * not implemented in the example yet. The Eventsourced library can be used for that. When the domain event has been
 * saved successfully the master replies with an acknowledgement message (Ack) to the front end. The master also keeps
 * track of accepted work identifiers to be able to discard duplicates sent from the front end.
 *
 * When a worker receives WorkIsReady it sends back WorkerRequestsWork to the master, which hands out the work, if any,
 * to the worker. The master keeps track of that the worker is busy and expect a result within a deadline. For long
 * running jobs the worker could send progress messages, but that is not implemented in the example.
 *
 * When the worker sends WorkIsDone the master updates its state of the worker and sends acknowledgement back to the
 * worker. This message must also be idempotent as the worker will re-send if it doesn't receive the acknowledgement.
 *
 * Update: workers now register for a single WorkType, they can only perform work of these type.
 * Master now has to keep track of Workers and Work by WorkType.
 */
public class Master extends UntypedActor {
    public static String ResultsTopic = "results";

    // todo thus far this code is in the AkkaConfig for the other actors
    public static Props props(FiniteDuration workTimeout) {
        return Props.create(Master.class, workTimeout);
    }

    // configurable timeout for all work items
    private final FiniteDuration workTimeout;

    // the akka extension that facilitates the pub/sub pattern
    private final ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();

    // todo review akka logging compared to slf4j
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Cancellable cleanupTask;

    // all registered workers and their state, by WorkType
    private Map<Work.WorkType, Map<String, WorkerState>> workersByWorkType;

    // outstanding queue of work by WorkType
    private Map<Work.WorkType, Queue<Work>> pendingWorkByWorkType;

    // complementary to the pendingWork, this set allows exists check on a workId
    private Set<String> workIds = new LinkedHashSet<String>();

    public Master(FiniteDuration workTimeout) {
        this.workTimeout = workTimeout;

        // setup maps by work type
        this.workersByWorkType = new HashMap<>();
        this.pendingWorkByWorkType = new HashMap<>();
        for (Work.WorkType workType : Work.WorkType.values()) {
            this.workersByWorkType.put(workType, new HashMap<String, WorkerState>());
            this.pendingWorkByWorkType.put(workType, new LinkedList<Work>());
        }

        // The master actor is made available for both front end and workers by registering itself
        // in the DistributedPubSubMediator.
        mediator.tell(new Put(getSelf()), getSelf());

        // fire the clean task periodically
        this.cleanupTask = getContext().system().scheduler().schedule(
                workTimeout.div(2), workTimeout.div(2), getSelf(), CleanupTick, getContext().dispatcher(), getSelf());
    }

    @Override
    public void postStop() {
        cleanupTask.cancel();
    }

    @Override
    public void onReceive(Object message) {
        log.debug("Message received by Master" + message + " from " + getSender());

        if (message instanceof MasterWorkerProtocol.RegisterWorker) {
            MasterWorkerProtocol.RegisterWorker msg =
                    (MasterWorkerProtocol.RegisterWorker) message;
            String workerId = msg.workerId;
            Work.WorkType workType = msg.workType;
            if (workersByWorkType.get(workType).containsKey(workerId)) {
                // for existing workers update our actor ref each message
                // (presumably this is in case the worker restarted/died?)
                workersByWorkType.get(workType)
                        .put(workerId, workersByWorkType.get(workType).get(workerId).copyWithRef(getSender()));
            } else {
                log.info("Worker registered for {}: {}", workType, workerId);
                workersByWorkType.get(workType).put(workerId, new WorkerState(getSender(), Idle.instance));
                if (!pendingWorkByWorkType.get(workType).isEmpty())
                    getSender().tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
            }
        }
        else if (message instanceof MasterWorkerProtocol.WorkerRequestsWork) {
            MasterWorkerProtocol.WorkerRequestsWork msg = (MasterWorkerProtocol.WorkerRequestsWork) message;
            String workerId = msg.workerId;
            Work.WorkType workType = msg.workType;
            if (!pendingWorkByWorkType.get(workType).isEmpty()) {
                WorkerState state = workersByWorkType.get(workType).get(workerId);
                if (state != null && state.status.isIdle()) {
                    Work work = pendingWorkByWorkType.get(workType).remove();
                    log.info("Giving worker {} some work {} of type {}", workerId, work.toString(), workType);
                    // TODO store in Eventsourced
                    getSender().tell(work, getSelf());
                    workersByWorkType.get(workType)
                            .put(workerId, state.copyWithStatus(new Busy(work, workTimeout.fromNow())));
                }
            }
        }
        else if (message instanceof MasterWorkerProtocol.WorkIsDone) {
            MasterWorkerProtocol.WorkIsDone msg = (MasterWorkerProtocol.WorkIsDone) message;
            String workerId = msg.workerId;
            Work.WorkType workType = msg.workType;
            String workId = msg.workId;
            WorkerState state = workersByWorkType.get(workType).get(workerId);
            if (state != null && state.status.isBusy() && state.status.getWork().workId.equals(workId)) {
                Work work = state.status.getWork();
                Object result = msg.result;
                log.info("Work is done: {} => {} by worker {}", work, result, workerId);
                // TODO store in Eventsourced
                workersByWorkType.get(workType).put(workerId, state.copyWithStatus(Idle.instance));
                mediator.tell(new DistributedPubSubMediator.Publish(ResultsTopic,
                        new WorkResult(workId, result)), getSelf());
                getSender().tell(new Ack(workId), getSelf());
            } else {
                if (workIds.contains(workId)) {
                    // previous Ack was lost, confirm again that this is done
                    getSender().tell(new Ack(workId), getSelf());
                }
            }
        }
        else if (message instanceof MasterWorkerProtocol.WorkFailed) {
            MasterWorkerProtocol.WorkFailed msg = (MasterWorkerProtocol.WorkFailed) message;
            String workerId = msg.workerId;
            Work.WorkType workType = msg.workType;
            String workId = msg.workId;
            WorkerState state = workersByWorkType.get(workType).get(workerId);
            if (state != null && state.status.isBusy() && state.status.getWork().workId.equals(workId)) {
                log.info("Work failed: {}", state.status.getWork());
                // TODO store in Eventsourced

                // allow the worker to take more work and retry the work item (until cleanup occurs)
                workersByWorkType.get(workType).put(workerId, state.copyWithStatus(Idle.instance));
                pendingWorkByWorkType.get(workType).add(state.status.getWork());
                notifyWorkers(workType);
            }
        }
        else if (message instanceof Work) {
            Work work = (Work) message;
            // idempotent
            if (workIds.contains(work.workId)) {
                getSender().tell(new Ack(work.workId), getSelf());
            } else {
                log.info("Accepted work: {}", work);
                // TODO store in Eventsourced
                pendingWorkByWorkType.get(work.getWorkType()).add(work);
                workIds.add(work.workId);
                getSender().tell(new Ack(work.workId), getSelf());
                notifyWorkers(work.getWorkType());
            }
        }
        else if (message == CleanupTick) {

            // review current work for items that are overdue
            // todo looks to try the work and remove the worker from the pool - does this risk letting bad work take out all the workers?

            for (Work.WorkType workType : workersByWorkType.keySet()) {
                Iterator<Map.Entry<String, WorkerState>> iterator =
                        workersByWorkType.get(workType).entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, WorkerState> entry = iterator.next();
                    String workerId = entry.getKey();
                    WorkerState state = entry.getValue();
                    if (state.status.isBusy()) {
                        if (state.status.getDeadLine().isOverdue()) {
                            Work work = state.status.getWork();
                            log.info("Work timed out: {}", work);
                            // TODO store in Eventsourced
                            iterator.remove();
                            pendingWorkByWorkType.get(workType).add(work);
                            notifyWorkers(workType);
                        }
                    }
                }
            }
        }
        else {
            unhandled(message);
        }
    }

    private void notifyWorkers(Work.WorkType workType) {
        if (!pendingWorkByWorkType.get(workType).isEmpty()) {
            // could pick a few random instead of all
            for (WorkerState state: workersByWorkType.get(workType).values()) {
                if (state.status.isIdle())
                    state.ref.tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
            }
        }
    }

    private static abstract class WorkerStatus {
        protected abstract boolean isIdle();
        private boolean isBusy() {
            return !isIdle();
        };
        protected abstract Work getWork();
        protected abstract Deadline getDeadLine();
    }

    private static final class Idle extends WorkerStatus {
        private static final Idle instance = new Idle();
        public static Idle getInstance() {
            return instance;
        }

        @Override
        protected boolean isIdle() {
            return true;
        }

        @Override
        protected Work getWork() {
            throw new IllegalAccessError();
        }

        @Override
        protected Deadline getDeadLine() {
            throw new IllegalAccessError();
        }

        @Override
        public String toString() {
            return "Idle";
        }
    }

    private static final class Busy extends WorkerStatus {
        private final Work work;
        private final Deadline deadline;

        private Busy(Work work, Deadline deadline) {
            this.work = work;
            this.deadline = deadline;
        }

        @Override
        protected boolean isIdle() {
            return false;
        }

        @Override
        protected Work getWork() {
            return work;
        }

        @Override
        protected Deadline getDeadLine() {
            return deadline;
        }

        @Override
        public String toString() {
            return "Busy{" +
                    "work=" + work +
                    ", deadline=" + deadline +
                    '}';
        }
    }

    private static final class WorkerState {
        public final ActorRef ref;
        public final WorkerStatus status;

        private WorkerState(ActorRef ref, WorkerStatus status) {
            this.ref = ref;
            this.status = status;
        }

        private WorkerState copyWithRef(ActorRef ref) {
            return new WorkerState(ref, this.status);
        }

        private WorkerState copyWithStatus(WorkerStatus status) {
            return new WorkerState(this.ref, status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WorkerState that = (WorkerState) o;

            if (!ref.equals(that.ref)) return false;
            if (!status.equals(that.status)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = ref.hashCode();
            result = 31 * result + status.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "WorkerState{" +
                    "ref=" + ref +
                    ", status=" + status +
                    '}';
        }
    }

    private static final Object CleanupTick = new Object() {
        @Override
        public String toString() {
            return "CleanupTick";
        }
    };

    public static final class WorkResult implements Serializable {
        public final String workId;
        public final Object result;

        public WorkResult(String workId, Object result) {
            this.workId = workId;
            this.result = result;
        }

        @Override
        public String toString() {
            return "WorkResult{" +
                    "workId='" + workId + '\'' +
                    ", result=" + result +
                    '}';
        }
    }

    public static final class Ack implements Serializable {
        final String workId;

        public Ack(String workId) {
            this.workId = workId;
        }

        @Override
        public String toString() {
            return "Ack{" +
                    "workId='" + workId + '\'' +
                    '}';
        }
    }

    // TODO cleanup old workers
    // TODO cleanup old workIds

}
