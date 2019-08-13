import MapReduce.KeyValuePair;
import MapReduce.Mapper;
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

/**
 * a class to represent a Peer in a peer-to-peer Map/Reduce service
 */
public class Peer implements User, Coordinator, JobManager, TaskManager, RemotePeer {
  private int clientPort;
  private RemoteMembershipManager service;
  private Uuid uuid;
  private Uuid coordinatorUuid;
  private RemoteCoordinator coordinator;
  private final List<Uuid> availablePeers;
  private Map<String, Job> userJobs;
  private List<JobId> managedJobIds;
  private Map<String, JobResult> jobResults;
  private Map<String, JobResult> unDeliveredJobResults;
  private Map<String, List<KeyValuePair>> mapResults;
  private boolean isCoordinator;
  private ExecutorService taskExecutor;
  private Random random;
  private Registry localRegistry;

  /**
   * a constructor to initiate this Peer
   *
   * @param clientPort the port over which other Peers will communicate with this Peer
   */
  public Peer(int clientPort) {
    this.clientPort = clientPort;
    this.availablePeers = new LinkedList<>();
    this.userJobs = new HashMap<>();
    this.managedJobIds = new LinkedList<>();
    this.jobResults = new HashMap<>();
    this.unDeliveredJobResults = new HashMap<>();
    this.mapResults = new HashMap<>();
    this.isCoordinator = false;
    this.taskExecutor = Executors.newFixedThreadPool(5);
    this.random = new Random();

    try {
      // connect to the MembershipService and get a Uuid
      Registry remoteRegistry = LocateRegistry.getRegistry(MembershipManager.SERVICE_HOST, MembershipManager.MANAGER_PORT);
      this.service = (RemoteMembershipManager) remoteRegistry.lookup(MembershipManager.SERVICE_NAME);
      this.uuid = this.service.generateUuid(InetAddress.getLocalHost(), this.clientPort);

      // create a local registry... or simply get it if it already exists
      try {
        this.localRegistry = LocateRegistry.createRegistry(this.clientPort);
      } catch (RemoteException re) {
        this.localRegistry = LocateRegistry.getRegistry(this.clientPort);
      }

      // create references to the Remote Peer interface
      RemotePeer peer = this;

      // get a stub for this Remote Peer
      RemotePeer peerStub = (RemotePeer) UnicastRemoteObject.exportObject(peer, this.clientPort);

      // register this Peer as a RemotePeer
      this.localRegistry.rebind(getUuid().toString(), peerStub);
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
      this.coordinatorUuid = this.service.addMember(this.uuid);
      this.coordinator = (RemoteCoordinator) getRemoteRef(coordinatorUuid, MembershipManager.COORDINATOR);
    } catch (RemoteException | NotBoundException ex) {
      // TODO: figure out if this exception needs to be caught and, if so, what needs to happen in the catch clause
      ex.printStackTrace();
    }
  }

  @Override
  public void createJob(JobId jobId, JobData data, Mapper mapper, Reducer reducer) {
    Job job = new JobImpl(this.uuid, jobId, data, mapper, reducer);
    this.userJobs.put(jobId.getJobIdNumber(), job);
  }

  @Override
  public void submitJob(JobId jobId) {
    boolean attemptSuccessful;

    try {
      attemptSuccessful = this.coordinator.assignJob(jobId);

      while(!attemptSuccessful) { // if the coordinator has been decommissioned but is still an active peer
        System.out.println("Attempt to submit the job unsuccessful, trying again...");

        // get new coordinator
        this.coordinatorUuid = this.service.getNewCoordinator();
        this.coordinator = (RemoteCoordinator) getRemoteRef(coordinatorUuid, MembershipManager.COORDINATOR);

        // try again (recurse)
        attemptSuccessful = this.coordinator.assignJob(jobId);
      }
    } catch (RemoteException | NotBoundException | NullPointerException ex1) { // if the coordinator has crashed or left the network
      try {
        // have MembershipManager remove old (dead) coordinator
        this.service.removeMember(this.coordinatorUuid);

        // get new coordinator
        this.coordinatorUuid = this.service.getNewCoordinator();
        this.coordinator = (RemoteCoordinator) getRemoteRef(coordinatorUuid, MembershipManager.COORDINATOR);

        // try again (recurse)
        submitJob(jobId);
      } catch (RemoteException | NotBoundException ex2) {
        // TODO: determine if a better action is needed here
        System.out.println("Sorry, but your job couldn't be processed at this time; please re-submit later.");
        ex2.printStackTrace();
      }
    }
  }

  @Override
  public Map<String, Job> getJobs() {
    return this.userJobs;
  }

  @Override
  public Map<String, JobResult> getResults() {
    return this.jobResults;
  }

  @Override
  public void leave() {
    try {
      this.isCoordinator = false;
      this.localRegistry.unbind(getUuid().toString());
      this.service.removeMember(this.uuid);
    } catch (RemoteException | NotBoundException ex) {
      // TODO: figure out if this exception needs to be caught and, if so, what needs to happen in the catch clause
      ex.printStackTrace();
    }
  }

  /* ---------- RemoteUser methods ---------- */

  @Override
  public Job getJob(JobId jobId) {
    return this.userJobs.get(jobId.getJobIdNumber());
  }

  @Override
  public void setJobResult(JobId jobId, JobResult results) {
    System.out.println("Setting job results...");

    this.jobResults.put(jobId.getJobIdNumber(), results);
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
      System.out.println("Job assigned to JobManager...");
    } catch (RemoteException | NotBoundException ex){
      // TODO: determine if recursion is safe here???
      System.out.println("Recursion due to the following exception:");
      ex.printStackTrace();
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
        int index = this.random.nextInt(this.availablePeers.size());

        try {
          RemoteUser user = (RemoteUser) getRemoteRef(this.availablePeers.get(index), MembershipManager.USER);
          return user.getUuid();
        } catch (RemoteException | NotBoundException ex1) {
          try {
            this.service.removeMember(this.availablePeers.get(index));
          } catch (RemoteException | NotBoundException ex2) {
            // TODO: handle this exception better
            ex2.printStackTrace();
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
  public boolean assignJob(JobId jobId) {
    if (this.isCoordinator) {
      assignJobToJobManager(jobId);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<Uuid> getTaskManagers(int numRequested) {
    List<Uuid> taskManagers = new ArrayList<>();
    int numToReturn;

    synchronized (this.availablePeers) {
      if (numRequested < this.availablePeers.size() && numRequested < MembershipManager.MAX_TASK_MANAGERS_PER_JOB) {
        numToReturn = numRequested;
      } else {
        numToReturn = Math.min(this.availablePeers.size(), MembershipManager.MAX_TASK_MANAGERS_PER_JOB);
      }

      int[] indexes = ThreadLocalRandom.current().ints(0, this.availablePeers.size()).limit(numToReturn).toArray();

      int i = 0;

      while (i < indexes.length) {
        taskManagers.add(this.availablePeers.get(indexes[i]));
        i++;
      }
    }

    return taskManagers;
  }

  /* ---------- JobManager methods ---------- */

  synchronized private void processJobIdQueue() {
    while (this.managedJobIds.size() > 0) {
      System.out.println("Staring job processing...");

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
        e.printStackTrace();
      }

      this.managedJobIds.remove(0);
    }
  }

  @Override
  public Job retrieveJob(JobId jobId) throws RemoteException, NotBoundException {
    System.out.println("Retrieving job...");

    try {
      RemoteUser user = (RemoteUser) getRemoteRef(jobId.getSubmitter(), MembershipManager.USER);
      return user.getJob(jobId);
    } catch (RemoteException e) {
      System.out.println("JobManager.retrieveJob: RemoteException: " + e.getMessage());
      throw e;
    } catch (NotBoundException e) {
      System.out.println("JobManager.retrieveJob: NotBoundException: " + e.getMessage());
      throw e;
    }
  }

  private List<Task> splitJobToTasks(Job job) {
    System.out.println("Splitting job into tasks...");

    List<Task> taskList = new ArrayList<>();
    List<JobData> splitData = job.getSplitData(1000);
    TaskId taskId = new TaskId(job.getUserUuid(), job.getJobId());

    for (JobData jd : splitData) {
      Task task = new TaskImpl(taskId, job.getUserUuid(), this.uuid, jd, job.getMapper(), job.getReducer());
      taskList.add(task);
    }

    return taskList;
  }

  @Override
  public List<TaskResult> submitTasks(List<Task> tasks) {
    System.out.println("Submitting tasks...");

    boolean mapIsCompleted = false;
    boolean reduceIsCompleted = false;
    List<TaskResult> taskResultList, reduceTaskResultList = new ArrayList<>();

    // TODO: determine if we want to pass in different numbers based on the size of the job
    List<RemoteTaskManager> mapRtms = requestTaskManagers(10);
    List<RemoteTaskManager> reduceRtms = requestTaskManagers(5);

    List<Uuid> reducerIds = new ArrayList<>();

    for (RemoteTaskManager r : reduceRtms) {
      try {
        reducerIds.add(r.getUuid());
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    CompletionService<TaskResult> completionService = establishTaskCompletionService(mapRtms, tasks, true, reducerIds);

    while (!reduceIsCompleted) {
      try {
        taskResultList = executeTaskCompletionService(completionService, tasks.size());
        System.out.println("JobManager.submitTasks completed MapTask of size: " + taskResultList.size());
        mapIsCompleted = true;
      } catch (TimeoutException | InterruptedException | ExecutionException e) {
        System.out.println("JobManager.submitTasks encountered an error executing the MapTask. Will restart Task: " + e.getMessage());
        e.printStackTrace();
      }

      CompletionService<TaskResult> reduceCompletionService = establishTaskCompletionService(reduceRtms, tasks, false, reducerIds);

      if (mapIsCompleted) {
        try {
          // TODO: fix the tasksSize....!!!
          reduceTaskResultList = executeTaskCompletionService(reduceCompletionService, reducerIds.size());
          reduceIsCompleted = true;
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
          System.out.println("JobManager.submitTasks encountered an error executing the ReduceTask. Will restart Task: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }

    return reduceTaskResultList;
  }

  @Override
  public List<RemoteTaskManager> requestTaskManagers(int num) {
    System.out.println("Requesting task managers...");

    if (num > MembershipManager.MAX_TASK_MANAGERS_PER_JOB) {
      num = MembershipManager.MAX_TASK_MANAGERS_PER_JOB;
    }

    List<RemoteTaskManager> rtms = new ArrayList<>();
    List<Uuid> rtmUuids;

    try {
      rtmUuids = this.coordinator.getTaskManagers(num);

      try {
        for (Uuid uuid : rtmUuids) {
          rtms.add((RemoteTaskManager) getRemoteRef(uuid, MembershipManager.TASK_MANAGER));
        }
      } catch (RemoteException | NotBoundException ex) {
        // TODO: determine best course of action if allocated a "dead" TaskManager
        ex.printStackTrace();
      }
    } catch (RemoteException re) {
      System.out.println("JobManager.requestTaskManagers: Unable to reach coordinator");
      re.printStackTrace();
      // TODO: Need a way to request a different Coordinator
    }

    return rtms;
  }

  // establish the completion service that will be used to submit a task to the TaskManager
  private CompletionService<TaskResult> establishTaskCompletionService(List<RemoteTaskManager> rtmList, List<Task> taskList, boolean isMapTask, List<Uuid> reducerIds) {
    System.out.println("Establishing task completion service...");

    CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(this.taskExecutor);

    if (isMapTask) {
      for (Task task : taskList) {
        RemoteTaskManager rtm = nextRtm(rtmList);

        completionService.submit(new Callable<TaskResult>() {
          public TaskResult call() throws InterruptedException{
            TaskResult tr;
            try {
              tr = rtm.performMapTask(task, reducerIds);
            } catch (RemoteException e) {
              System.out.println("JobManager.establishTaskCompletionService Remote Exception: " + e.getMessage());
              e.printStackTrace();
              throw new InterruptedException("establishTaskCompletionService: RemoteException: " + e.getMessage());
            }
            return tr;
          }
        });
      }
    } else {
      for (RemoteTaskManager rtm : rtmList) {
        completionService.submit(new Callable<TaskResult>() {
          public TaskResult call() throws InterruptedException{
            TaskResult tr;
            try {
              tr = rtm.performReduceTask(taskList.get(0));
            } catch (RemoteException e) {
              System.out.println("JobManager.establishTaskCompletionService Remote Exception: " + e.getMessage());
              e.printStackTrace();
              throw new InterruptedException("establishTaskCompletionService: RemoteException: " + e.getMessage());
            }
            return tr;
          }
        });
      }
    }

    return completionService;
  }

  private RemoteTaskManager nextRtm(List<RemoteTaskManager> rtmList) {
    Random r = new Random();
    int index = r.nextInt(rtmList.size());
    return rtmList.get(index);
  }

  // run the executor that will administer the previously established completion services
  private List<TaskResult> executeTaskCompletionService(CompletionService<TaskResult> completionService, int tasksSize) throws InterruptedException, ExecutionException, TimeoutException{
    System.out.println("Executing task completion service...");

    List<TaskResult> responses = new ArrayList<>();
    Future<TaskResult> r;

    try {
      for (int t = 0; t < tasksSize; t++ ) {
        r = completionService.take();
        TaskResult tr = r.get(Task.TIMEOUT, TimeUnit.SECONDS);
        responses.add(tr);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw e;
    } catch (ExecutionException e) {
      System.out.println("JobManager.executeTaskCompletionService Execution Exception: " + e.getMessage());
      throw e;
    } catch (TimeoutException e) {
      System.out.println("JobManager.executeTaskCompletionService: TaskManager didn't return results in time.");
      throw e;
    }
    return responses;
  }

  private TaskResult mergeTaskResults(List<TaskResult> taskResults) {
    System.out.println("Merging task results...");

    Map<String, Integer> aggregate = new HashMap<>();

    for (TaskResult taskResult: taskResults) {
      aggregate.putAll(taskResult.getResults().getResultData());
    }
    return new ReduceTaskResult(aggregate);
  }

  @Override
  public void returnResults(JobResult jobResult) {
    System.out.println("Returning results...");

    try {
      RemoteUser user = (RemoteUser) getRemoteRef(jobResult.getUserUuid(), MembershipManager.USER);
      user.setJobResult(jobResult.getJobId(), jobResult);
    } catch (RemoteException | NotBoundException e) {
      this.unDeliveredJobResults.put(jobResult.getJobId().getJobIdNumber(), jobResult);
      System.out.println("JobManager.returnResults: Unable to deliver results. Saving results for future delivery.");
      e.printStackTrace();
    }
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
  public TaskResult performMapTask(Task task, List<Uuid> reducerIds) {
    // Mapping Phase
    Mapper mapper = task.getMapper();
    Map<String, Integer> map = new HashMap<>();
    int numReducers = reducerIds.size();

    for (String line : task.getDataset().getJobData()) {
      mapper.map(line, map);
    }

    for (String key : map.keySet()) {
      int partition = mapper.getPartition(key, numReducers);

      try {
        RemoteTaskManager reducer = (RemoteTaskManager) getRemoteRef(reducerIds.get(partition), MembershipManager.TASK_MANAGER);
        reducer.submitMapResult(key, map.get(key), task.getTaskId().getJobId()); // submit results to corresponding reducer
      } catch (NotBoundException e) {
        System.out.println("TaskManager.performMapTask NotBoundException: " + e.getMessage());
        e.printStackTrace();
      } catch (RemoteException e) {
        System.out.println("TaskManager.performMapTask RemoteException: " + e.getMessage());
        e.printStackTrace();
      }
    }

    return new MapTaskResult(map);
  }

  @Override
  public void submitMapResult(String key, int value, JobId jobId) {
    // TODO: make mapResults thread-safe
    List<KeyValuePair> list;

    if (this.mapResults.containsKey(jobId.getJobIdNumber())) {
      list = this.mapResults.get(jobId.getJobIdNumber());
      list.add(new KeyValuePair(key, value));
      this.mapResults.put(jobId.getJobIdNumber(), list);
    } else {
      list = new ArrayList<>();
      list.add(new KeyValuePair(key, value));
      this.mapResults.put(jobId.getJobIdNumber(), list);
    }
  }

  @Override
  public TaskResult performReduceTask(Task task) {
    // Reduce Phase
    Reducer reducer = task.getReducer();
    Map<String, Integer> map = new HashMap<>();
    reducer.reduce(this.mapResults.get(task.getTaskId().getJobId().getJobIdNumber()), map);

    return new ReduceTaskResult(map);
  }

  /* ---------- Communicate methods ---------- */

  @Override
  public Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(uuid.getAddress().getHostName(), uuid.getClientPort());
    return registry.lookup(uuid.toString());
//    return registry.lookup(uuid.toString() + peerRole);
  }

  /* ---------- Identify methods ---------- */

  @Override
  public Uuid getUuid() {
    return this.uuid;
  }
}