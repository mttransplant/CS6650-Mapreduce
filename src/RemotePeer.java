/**
 * an interface to represent all of the remote methods needed to act as a remote peer
 * in all of its various roles
 */
public interface RemotePeer extends RemoteUser, RemoteCoordinator, RemoteJobManager, RemoteTaskManager { }