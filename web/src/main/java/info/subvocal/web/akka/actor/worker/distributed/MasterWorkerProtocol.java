package info.subvocal.web.akka.actor.worker.distributed;

import info.subvocal.web.akka.actor.message.Work;

import java.io.Serializable;

/**
 * Source modified from:
 * https://github.com/typesafehub/activator-akka-distributed-workers-java/blob/master/src/main/java/worker/Master.java
 */
public abstract class MasterWorkerProtocol {

    // Messages from/to Workers

    /**
     * Workers can only register for a single WorkType
     */
    public static final class RegisterWorker implements Serializable {
        public final String workerId;
        public final Work.WorkType workType;

        public RegisterWorker(String workerId, Work.WorkType workType) {
            this.workerId = workerId;
            this.workType = workType;
        }

        @Override
        public String toString() {
            return "RegisterWorker{" +
                    "workerId='" + workerId + '\'' +
                    "workType='" + workType + '\'' +
                    '}';
        }
    }

    public static final class WorkerRequestsWork implements Serializable {
        public final String workerId;
        public final Work.WorkType workType;

        public WorkerRequestsWork(String workerId, Work.WorkType workType) {
            this.workerId = workerId;
            this.workType = workType;
        }

        @Override
        public String toString() {
            return "WorkerRequestsWork{" +
                    "workerId='" + workerId + '\'' +
                    "workType='" + workType + '\'' +
                    '}';
        }
    }

    public static final class WorkIsDone implements Serializable {
        public final String workerId;
        public final Work.WorkType workType;
        public final String workId;
        public final Object result;

        public WorkIsDone(String workerId, Work.WorkType workType, String workId, Object result) {
            this.workerId = workerId;
            this.workType = workType;
            this.workId = workId;
            this.result = result;
        }

        @Override
        public String toString() {
            return "WorkIsDone{" +
                    "workerId='" + workerId + '\'' +
                    ", workId='" + workId + '\'' +
                    ", result=" + result +
                    '}';
        }
    }

    public static final class WorkFailed implements Serializable {
        public final String workerId;
        public final Work.WorkType workType;
        public final String workId;

        public WorkFailed(String workerId, Work.WorkType workType, String workId) {
            this.workerId = workerId;
            this.workType = workType;
            this.workId = workId;
        }

        @Override
        public String toString() {
            return "WorkFailed{" +
                    "workerId='" + workerId + '\'' +
                    ", workId='" + workId + '\'' +
                    '}';
        }
    }

    // Messages to Workers

    public static final class WorkIsReady implements Serializable {
        private static final WorkIsReady instance = new WorkIsReady();
        public static WorkIsReady getInstance() {
            return instance;
        }
    }
}