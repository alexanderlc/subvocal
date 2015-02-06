package info.subvocal.web.akka.actor.worker.distributed;

import akka.japi.Procedure;

/**
 * Base class for behaviours, which are procedures -> functions without a return value
 */
public abstract class Behavior implements Procedure<Object> {
    public abstract void apply(Object message);
}
