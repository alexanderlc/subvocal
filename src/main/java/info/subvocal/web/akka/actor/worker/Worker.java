package info.subvocal.web.akka.actor.worker;

/**
 * We should support many worker nodes and we assume that they can be unstable. Therefore we don't let the worker nodes
 * be members of the cluster, instead they communicate with the cluster through the Cluster Client. The worker doesn't
 * have to know exactly where the master is located.
 *
 * The worker register itself periodically to the master, see the registerTask. This has the nice characteristics that
 * master and worker can be started in any order, and in case of master fail over the worker re-register itself to the
 * new master.
 */
public class Worker {
}
