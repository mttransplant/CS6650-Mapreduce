import MapReduce.Mapper;
import MapReduce.Pair;
import MapReduce.Reducer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 1. aggregate: mergeTaskResults()... establish a delegate for this method should be established in the Job and/or Reduce interface (Nay)
 * 2. try to make Task-related interfaces/classes generic if reasonable (Dan)
 */

public class Peer implements User, Coordinator, JobManager, TaskManager, RemotePeer {
  private RemoteMembershipManager service;
  private Uuid uuid;
  private RemoteCoordinator coordinator;
  private final List<Uuid> availablePeers;
  private Map<String, Job> userJobs;
  private List<JobId> managedJobIds;
  private Map<String, JobResult> jobResults;
  private Map<String, JobResult> unDeliveredJobResults;
  private List<Uuid> reducerIds;
  private List<Pair> mapResults;
  private boolean isCoordinator;
  private ExecutorService taskExecutor;
  private Random random;

  public Peer() {
    this.availablePeers = new LinkedList<>();
    this.userJobs = new HashMap<>();
    this.managedJobIds = new LinkedList<>();
    this.jobResults = new HashMap<>();
    this.unDeliveredJobResults = new HashMap<>();
    this.reducerIds = new LinkedList<>();
    this.mapResults = new LinkedList<>();
    this.isCoordinator = false;
    this.taskExecutor = Executors.newFixedThreadPool(5);
    this.random = new Random();

    try {
      // connect to the MembershipService and get a Uuid
      Registry remoteRegistry = LocateRegistry.getRegistry(MembershipManager.SERVICE_HOST, MembershipManager.MANAGER_PORT);
      this.service = (RemoteMembershipManager) remoteRegistry.lookup(MembershipManager.SERVICE_NAME);
      this.uuid = this.service.generateUuid(InetAddress.getLocalHost());

      // create a local registry... or simply get it if it already exists
      Registry localRegistry;

      try {
        localRegistry = LocateRegistry.createRegistry(MembershipManager.CLIENT_PORT);
      } catch (RemoteException re) {
        localRegistry = LocateRegistry.getRegistry(MembershipManager.CLIENT_PORT);
      }

      // create references to the Remote Peer interface
      RemotePeer peer = this;

      // get a stub for this Remote Peer
      RemotePeer peerStub = (RemotePeer) UnicastRemoteObject.exportObject(peer, MembershipManager.CLIENT_PORT);

      // register this Peer as a RemotePeer
      localRegistry.rebind(getUuid().toString(), peerStub);
    } catch (UnknownHostException uhe) {
      // TODO: handle this exception better?
      System.out.println(String.format("UnkownHoustException encountered launching Peer: %s", uhe.getMessage()));
    } catch (RemoteException re) {
      // TODO: then handle this exception better?
      System.out.println(String.format("RemoteException encountered launching Peer: %s", re.getMessage()));
    } catch (NotBoundException nbe) {
      // TODO: then handle this exception better too?
      System.out.println(String.format("NotBoundException encountered launching Peer: %s", nbe.getMessage()));
    }
  }

  /* ---------- User methods ---------- */

  @Override
  public void join() {
    try {
      Uuid coord = this.service.addMember(this.uuid);
      this.coordinator = (RemoteCoordinator) getRemoteRef(coord, MembershipManager.COORDINATOR);
    } catch (RemoteException | NotBoundException ex) {
      // TODO: figure out if this exception needs to be caught and, if so, what needs to happen in the catch clause
    }
  }

  @Override
  public void createJob(JobId jobId, JobData data, Mapper mapper, Reducer reducer) {
    Job job = new JobImpl(this.uuid, jobId, data, mapper, reducer);
    this.userJobs.put(jobId.getJobIdNumber(), job);
  }

  @Override
  public void submitJob(JobId jobId) {
    try {
      this.coordinator.assignJob(jobId);
    } catch (RemoteException re) {
      // TODO: get a new coordinator from the MembershipManager
      // TODO: ping coordinator (as user)... if dead, forcibly remove (perform this "service" on behalf of the network
    }
  }

  @Override
  public void leave() {
    try {
      this.service.removeMember(this.uuid);
    } catch (RemoteException | NotBoundException ex) {
      // TODO: figure out if this exception needs to be caught and, if so, what needs to happen in the catch clause
    }
  }

  /* ---------- RemoteUser methods ---------- */

  @Override
  public Job getJob(JobId jobId) {
    return this.userJobs.get(jobId.getJobIdNumber());
  }

  @Override
  public void setJobResult(JobId jobId, JobResult results) {
    // TODO: implement this functionality to be used from within a JobManager
  }

  @Override
  public void setAsCoordinator() {
    this.isCoordinator = true;
  }

  @Override
  public void unbindCoordinator() {
    this.isCoordinator = false;
  }

  @Override
  public boolean hasMinimumResources() {
    Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() > MembershipManager.MIN_MEMORY && runtime.availableProcessors() > MembershipManager.MIN_PROCESSORS;
  }

  /* ---------- Coordinator methods ---------- */

  @Override
  public void assignJobToJobManager(JobId jobId) {
    // TODO: for now, this just picks a random Peer as the job manager
    int index = this.random.nextInt(this.availablePeers.size());
    Uuid selected = this.availablePeers.get(index);
    try {
      RemoteJobManager jobManager = (RemoteJobManager) getRemoteRef(selected, MembershipManager.JOB_MANAGER);
      jobManager.manageJob(jobId);
    } catch (RemoteException | NotBoundException ex){
      // TODO: determine if recursion is safe here???
      assignJobToJobManager((jobId));
    }
  }

  /* ---------- RemoteCoordinator methods ---------- */

  @Override
  public void addPeer(Uuid peer) {
    synchronized (this.availablePeers) {
      System.out.println("A peer is being added.");
      this.availablePeers.add(peer);
    }
  }

  @Override
  public void removePeer(Uuid peer) {
    System.out.println("A peer is being removed.");
    synchronized (this.availablePeers) {
      this.availablePeers.remove(peer);
    }
  }

  @Override
  public Uuid getActivePeer() {
    synchronized (this.availablePeers) {
      // iterate randomly through available peers, return first "live" peer, remove any encountered "dead" peers
      while(true) {
        int index = random.nextInt(this.availablePeers.size());

        try {
          RemoteUser user = (RemoteUser) getRemoteRef(this.availablePeers.get(index), MembershipManager.USER);
          return user.getUuid();
        } catch (RemoteException | NotBoundException ex1) {
          try {
            this.service.removeMember(this.availablePeers.get(index));
          } catch (RemoteException | NotBoundException ex2) {
            // TODO: handle this exception
          }
        }
      }
    }

    // TODO: handle situation where there are no active peers-- currently infinite recursion!!!
    // TODO: beware of peer pinging itself; is this all right?
  }

  @Override
  public List<Uuid> getActivePeers() {
    return new LinkedList<>(this.availablePeers);
  }

  @Override
  public void setActivePeers(List<Uuid> activePeers) {
    this.availablePeers.clear();
    this.availablePeers.addAll(activePeers);
  }

  @Override
  public void assignJob(JobId jobId) {
    if (this.isCoordinator) {
      assignJobToJobManager(jobId);
    } else {
      // TODO: handle case where the User's coordinator is no longer active
    }
  }

  @Override
  public List<RemoteTaskManager> getTaskManagers(int numRequested) {
    List<RemoteTaskManager> taskManagers = new LinkedList<>();
    int numToReturn;
    Uuid toBeRemoved;

    synchronized (this.availablePeers) {
      if (numRequested < availablePeers.size() && numRequested < MembershipManager.MAX_TASK_MANAGERS_PER_JOB) {
        numToReturn = numRequested;
      } else {
        numToReturn = Math.min(this.availablePeers.size(), MembershipManager.MAX_TASK_MANAGERS_PER_JOB);
      }

      int[] indexes = ThreadLocalRandom.current().ints(0, this.availablePeers.size()).limit(numToReturn).toArray();

      int i = 0;

      try {
        while (i < indexes.length) {
          RemoteTaskManager rtm = (RemoteTaskManager) getRemoteRef(this.availablePeers.get(i), MembershipManager.TASK_MANAGER);
          taskManagers.add(rtm);
          i++;
        }
        return taskManagers;
      } catch (RemoteException | NotBoundException ex) {
        toBeRemoved = this.availablePeers.get(i);
      }
    }

    try {
      this.service.removeMember(toBeRemoved);
    } catch (RemoteException | NotBoundException ex2) {
      // TODO: determine if something needs to be done here
    }

    // recurse after "dead" peer has been removed from the list of available peers
    return getTaskManagers(numRequested);
  }

  /* ---------- JobManager methods ---------- */

  @Override
  public Job retrieveJob(JobId jobId) throws RemoteException, NotBoundException {
    try {
      RemoteUser user = (RemoteUser) getRemoteRef(jobId.getSubmitter(),MembershipManager.USER);
      return user.getJob(jobId);
    } catch (RemoteException e) {
      System.out.println("JobManager.retrieveJob: RemoteException: " + e.getMessage());
      throw e;
    } catch (NotBoundException e) {
      System.out.println("JobManager.retrieveJob: NotBoundException: " + e.getMessage());
      throw e;
    }
  }

  private RemoteTaskManager nextRtm(List<RemoteTaskManager> rtmList) {
    Random r = new Random();
    int index = r.nextInt(rtmList.size());
    return rtmList.get(index);
  }

  // establish the completion service that will be used to submit a task to the TaskManager
  private CompletionService<TaskResult> establishTaskCompletionService(List<RemoteTaskManager> rtmList, List<Task> taskList) {
    CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(taskExecutor);
    for (Task task : taskList) {
      RemoteTaskManager rtm = nextRtm(rtmList);
      completionService.submit(new Callable<TaskResult>() {
        public TaskResult call() throws InterruptedException{
          TaskResult tr;
          try {
            tr = rtm.performMapTask(task);
          } catch (RemoteException e) {
            System.out.println("JobManager.establishTaskCompletionService Remote Exception: " + e.getMessage());
            throw new InterruptedException("establishTaskCompletionService: RemoteException: " + e.getMessage());
          }
          return tr;
        }
      });
    }
    return completionService;
  }

  // run the executor that will administer the previously established completion services
  private List<TaskResult> executeTaskCompletionService(CompletionService<TaskResult> completionService, int tasksSize) {
    List<TaskResult> responses = new ArrayList<>();
    Future<TaskResult> r;

    try {
      for (int t = 0; t<tasksSize; t++ ) {
        r = completionService.take();
        TaskResult tr = r.get(Task.TIMEOUT, TimeUnit.SECONDS);
        responses.add(tr);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      System.out.println("JobManager.executeTaskCompletionService Execution Exception: " + e.getMessage());
    } catch (TimeoutException e) {
      System.out.println("JobManager.executeTaskCompletionService: TaskManager didn't return results in time.");
    }
    return responses;
  }

  @Override
  public List<TaskResult> submitTasks(List<Task> tasks) {
    List<RemoteTaskManager> rtmList = requestTaskManagers();
    CompletionService<TaskResult> completionService = establishTaskCompletionService(rtmList, tasks);
    List<TaskResult> taskResultList = executeTaskCompletionService(completionService, tasks.size());
    List<Task> missingTasks = checkAllTasksReturned(tasks, taskResultList);

    // TODO: (Dan thinks)-- rework this so that it just resubmits the whole job if any tasks weren't returned
    while (missingTasks.size() > 0) {
      completionService = establishTaskCompletionService(rtmList, missingTasks);
      List<TaskResult> moreTaskResults = executeTaskCompletionService(completionService, missingTasks.size());
      taskResultList.addAll(moreTaskResults);
      missingTasks = checkAllTasksReturned(tasks, taskResultList);
    }

    return taskResultList;
  }

  @Override
  public void returnResults(JobResult jobResult) {
    try {
      RemoteUser user = (RemoteUser) getRemoteRef(jobResult.getUserUuid(), MembershipManager.USER);
      user.setJobResult(jobResult.getJobId(), jobResult);
    } catch (RemoteException | NotBoundException e) {
      this.unDeliveredJobResults.put(jobResult.getJobId().getJobIdNumber(), jobResult);
      System.out.println("JobManager.returnResults: Unable to deliver results. Saving results for future delivery.");
    }
  }

  @Override
  public List<RemoteTaskManager> requestTaskManagers() {
    List<RemoteTaskManager> rtms = null;
    try {
      // TODO: determine if we want to pass in a different number based on the size of the job
      rtms = this.coordinator.getTaskManagers(MembershipManager.MAX_TASK_MANAGERS_PER_JOB);
    } catch (RemoteException re) {
      System.out.println("JobManager.requestTaskManagers: Unable to reach coordinator");
      // TODO: Need a way to request a different Coordinator
    }
    return rtms;
  }

  private List<Task> checkAllTasksReturned(List<Task> tasks, List<TaskResult> taskResultList) {
    List<Task> missingTasks = new ArrayList<>();
    for (Task t : tasks) {
      String subTaskId = t.getTaskId().getTaskId();
      boolean idFound = false;
      for (TaskResult tr : taskResultList) {
        if (!idFound && tr.getTaskId().getTaskId().equals(subTaskId)) {
          idFound = true;
        }
      }
      if (!idFound) {
        missingTasks.add(t);
      }
    }
    return missingTasks;
  }

  private List<Task> splitJobToTasks(Job job) {
    List<Task> taskList = new ArrayList<>();
    List<JobData> splitData = job.getSplitData(1000);
    TaskId taskId = new TaskId(job.getUserUuid(), job.getJobId());

    for (JobData jd : splitData) {
      Task task = new TaskImpl(taskId, job.getUserUuid(), this.uuid, jd, job.getMapper(), job.getReducer());
      taskList.add(task);
    }

    return taskList;
  }

  synchronized private void processJobIdQueue() {
    while (this.managedJobIds.size() > 0) {
      JobId jobId = this.managedJobIds.get(0);

      try {
        Job job = retrieveJob(jobId);
        List<Task> taskList = splitJobToTasks(job);
        List<TaskResult> taskResults = submitTasks(taskList);
        TaskResult finalTaskResult = mergeTaskResults(taskResults);
        JobResult jobResult = new JobResultImpl(job, finalTaskResult.getStatus(), finalTaskResult.getResults());
        returnResults(jobResult);
      } catch (RemoteException | NotBoundException e) {
        System.out.println("JobManager.processJobIdQueue: Unable to reach user to return job. JobId removed from queue. " + e.getMessage());
      }

      this.managedJobIds.remove(0);
    }
  }

  private TaskResult mergeTaskResults(List<TaskResult> taskResults) {
    TaskResult taskResult = null;
    // TODO: Implement the merge of returned TaskResults into a finalTaskResult
    return taskResult;
  }


  /* ---------- RemoteJobManager methods ---------- */

  @Override
  public void manageJob(JobId jobId) {
    this.managedJobIds.add(jobId);
    processJobIdQueue();
  }

  /* ---------- TaskManager methods ---------- */



  /* ---------- RemoteTaskManager methods ---------- */

  @Override
  public TaskResult performMapTask(Task task) {
    // Mapping Phase
    Mapper mapper = task.getMapper();
    Map<String, Integer> map = new HashMap<>();
    for (String line : task.getDataset().getJobData()) {
      mapper.map(line, map);
    }

    for (String key : map.keySet()) {
      int partition = mapper.getPartition(key);

      try {
        RemoteTaskManager reducer = (RemoteTaskManager) getRemoteRef(reducerIds.get(partition), MembershipManager.TASK_MANAGER);
        reducer.submitMapResult(key, map.get(key)); // submit results to corresponding reducer
      } catch (NotBoundException e) {
        System.out.println("TaskManager.performMapTask NotBoundException: " + e.getMessage());
      } catch (RemoteException e) {
        System.out.println("TaskManager.performMapTask RemoteException: " + e.getMessage());
      }
    }

    // TODO: Return TaskResult object to JobManager
    return null;
  }

  @Override
  public void submitMapResult(String key, int value) {
    if (mapResults == null) {
      mapResults = new ArrayList<>();
    }
    mapResults.add(new Pair(key, value));
  }

  @Override
  public TaskResult performReduceTask(Task task) {
    // Reduce Phase
    Reducer reducer = task.getReducer();
    Map<String, Integer> map = new HashMap<>();
    reducer.reduce(mapResults, map);

    // TODO: return final aggregate result to JobManager
    return null;
  }

  /* ---------- Communicate methods ---------- */

  @Override
  public Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(uuid.getAddress().getHostName(), MembershipManager.CLIENT_PORT);
    return registry.lookup(uuid.toString());
//    return registry.lookup(uuid.toString() + peerRole);
  }

  /* ---------- Identify methods ---------- */

  @Override
  public Uuid getUuid() {
    return this.uuid;
  }
}