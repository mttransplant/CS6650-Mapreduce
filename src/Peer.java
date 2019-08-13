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
public class Peer implements User, Coordinator, JobManager, RemotePeer {
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
      System.out.println(String.format("UnkownHoustException encountered launching Peer: %s", uhe.getMessage()));
    } catch (RemoteException re) {
      System.out.println(String.format("RemoteException encountered launching Peer: %s", re.getMessage()));
    } catch (NotBoundException nbe) {
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
      System.out.println("Sorry, but the service couldn't be reached; please try again later.");
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
      System.out.println("Sorry, but there was a problem leaving the service; you may wish to retry this action later.");
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
    System.out.println(String.format("Setting job results for job %s...", jobId.getJobIdNumber()));

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
    int index = this.random.nextInt(this.availablePeers.size());
    Uuid selected = this.availablePeers.get(index);
    try {
      RemoteJobManager jobManager = (RemoteJobManager) getRemoteRef(selected, MembershipManager.JOB_MANAGER);
      jobManager.manageJob(jobId);
      System.out.println(String.format("Job %s assigned to JobManager %s...", jobId.getJobIdNumber(), selected.toString()));
    } catch (RemoteException | NotBoundException ex){
      System.out.println("Recursion due to the following exception:");
      ex.printStackTrace();
      assignJobToJobManager((jobId));
    }
  }

  /* ---------- RemoteCoordinator methods ---------- */

  @Override
  public void addPeer(Uuid peer) {
    synchronized (this.availablePeers) {
      System.out.println(String.format("Peer %s is being added at port %d.", peer.toString(), peer.getClientPort()));
      this.availablePeers.add(peer);
    }
  }

  @Override
  public void removePeer(Uuid peer) {
    System.out.println(String.format("Peer %s is being removed from port %d.", peer.toString(), peer.getClientPort()));
    synchronized (this.availablePeers) {
      this.availablePeers.remove(peer);
    }
  }

  @Override
  public Uuid getActivePeer() throws RemoteException, NotBoundException {
    synchronized (this.availablePeers) {
      // iterate randomly through available peers, return first "live" peer, remove any encountered "dead" peers
      while(true) {
        int index = this.random.nextInt(this.availablePeers.size());

        try {
          RemoteUser user = (RemoteUser) getRemoteRef(this.availablePeers.get(index), MembershipManager.USER);
          return user.getUuid();
        } catch (RemoteException | NotBoundException ex1) {
          this.service.removeMember(this.availablePeers.get(index));
        }
      }
    }
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
  // this is the main method that coordinates all the necessary activities to complete a Job once a JobId has been received
  // this method is called every time a JobId is added to the managedJobIds list.
  synchronized private void processJobIdQueue() throws RemoteException, NotBoundException {
    while (this.managedJobIds.size() > 0) {
      System.out.println(String.format("JobManager %s staring job processing...", this.uuid.toString()));

      JobId jobId = this.managedJobIds.get(0);

      try {
        Job job = retrieveJob(jobId); // reach out to the submitter to get the full Job
        List<Task> taskList = splitJobToTasks(job); // split the job payload into Tasks
        List<TaskResult> taskResults = submitTasks(taskList); // submit the Tasks to TaskManagers. submitTasks is a large method
        TaskResult finalTaskResult = mergeTaskResults(taskResults); // merge the TaskResults
        JobResult jobResult = new JobResultImpl(job, finalTaskResult.getStatus(), finalTaskResult.getResults()); // construct the JobResult object
        returnResults(jobResult); // send the JobResult to the submitter
      } catch (RemoteException | NotBoundException e) {
        System.out.println("JobManager.processJobIdQueue: Unable to reach user to return job. JobId removed from queue. " + e.getMessage());
        e.printStackTrace();
        throw e;
      }

      this.managedJobIds.remove(0); // this job has completed, regardless of delivery to User, remove it from the queue
    }
  }

  // this is the method that, given a JobId, will reach out to the submitter to get the full Job
  @Override
  public Job retrieveJob(JobId jobId) throws RemoteException, NotBoundException {
    System.out.println(String.format("JobManager %s retrieving job...", this.uuid.toString()));

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

  // this method takes a Job, gets its data, and splits that into a list of Tasks
  private List<Task> splitJobToTasks(Job job) {
    System.out.println(String.format("JobManager %s splitting job into tasks...", this.uuid.toString()));

    List<Task> taskList = new ArrayList<>();
    List<JobData> splitData = job.getSplitData(100);
    TaskId taskId = new TaskId(job.getUserUuid(), job.getJobId());

    for (JobData jd : splitData) {
      Task task = new TaskImpl(taskId, job.getUserUuid(), this.uuid, jd, job.getMapper(), job.getReducer());
      taskList.add(task);
    }

    return taskList;
  }

  // this method takes a list of Tasks and in two phases, assigns the TaskManagers to apply the Mapper and then Reducer functions.
  @Override
  public List<TaskResult> submitTasks(List<Task> tasks) throws RemoteException, NotBoundException {
    System.out.println(String.format("JobManager %s submitting tasks...", this.uuid.toString()));

    boolean mapIsCompleted = false;
    boolean reduceIsCompleted = false;
    List<TaskResult> taskResultList, reduceTaskResultList = new ArrayList<>();

    // attempt to complete both the Map and Reduce processes. Will retry the Map process if it fails before proceeding to the Reduce task
    while (!reduceIsCompleted) {
      // request the number of TaskManagers in proportion to the size of the list of Tasks
      List<RemoteTaskManager> mapRtms = requestTaskManagers(tasks.size());
      List<RemoteTaskManager> reduceRtms = requestTaskManagers(Math.max(1, tasks.size()/2));

      // extract the Uuids of the TaskManagers that will be assigned as Reducers
      List<Uuid> reducerIds = new ArrayList<>();
      for (RemoteTaskManager r : reduceRtms) {
        try {
          reducerIds.add(r.getUuid());
        } catch (Exception ex) {
          ex.printStackTrace();
          throw ex;
        }
      }

      CompletionService<TaskResult> completionService = establishTaskCompletionService(mapRtms, tasks, true, reducerIds);

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

  // local method that will reach out to the coordinator to request a set of available TaskManagers
  @Override
  public List<RemoteTaskManager> requestTaskManagers(int num) throws RemoteException, NotBoundException {
    System.out.println(String.format("JobManager %s requesting task managers...", this.uuid.toString()));

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
        System.out.println("JobManager.requestTaskManagers: Unable to reach assigned TaskManager. Moving on without it.");
        ex.printStackTrace();
        throw ex;
      }
    } catch (RemoteException re) {
      System.out.println("JobManager.requestTaskManagers: Unable to reach coordinator");
      re.printStackTrace();

      try {
        System.out.println("Encountered dead Coordinator while requesting TaskManagers; getting new Coordinator ref and recursing...");

        // report old Coordinator as dead
        this.service.removeMember(this.coordinator.getUuid());

        // get new Coordinator
        this.coordinatorUuid = this.service.getNewCoordinator();
        this.coordinator = (RemoteCoordinator) getRemoteRef(this.coordinatorUuid, MembershipManager.COORDINATOR);

        // recurse
        return requestTaskManagers(num);
      } catch (RemoteException ex) {
        ex.printStackTrace();
        throw ex;
      }
    }

    return rtms;
  }

  // establish the completion service that will be used to submit a Map or Reduce task to the TaskManager
  private CompletionService<TaskResult> establishTaskCompletionService(List<RemoteTaskManager> rtmList, List<Task> taskList, boolean isMapTask, List<Uuid> reducerIds) {
    System.out.println(String.format("JobManager %s establishing task completion service...", this.uuid.toString()));

    CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(this.taskExecutor);

    if (isMapTask) {
      for (Task task : taskList) {
        RemoteTaskManager rtm = nextRtm(rtmList);

        completionService.submit(new Callable<TaskResult>() {
          public TaskResult call() throws RemoteException, NotBoundException {
            TaskResult tr;
            try {
              tr = rtm.performMapTask(task, reducerIds);
            } catch (RemoteException | NotBoundException e) {
              System.out.println("JobManager.establishTaskCompletionService Remote Exception: " + e.getMessage());
              e.printStackTrace();
              throw e;
            }
            return tr;
          }
        });
      }
    } else {
      for (RemoteTaskManager rtm : rtmList) {
        completionService.submit(new Callable<TaskResult>() {
          public TaskResult call() throws RemoteException {
            TaskResult tr;
            try {
              tr = rtm.performReduceTask(taskList.get(0));
            } catch (RemoteException e) {
              System.out.println("JobManager.establishTaskCompletionService Remote Exception: " + e.getMessage());
              e.printStackTrace();
              throw e;
            }
            return tr;
          }
        });
      }
    }

    return completionService;
  }

  // method used to select the next TaskManager to complete a Map task
  private RemoteTaskManager nextRtm(List<RemoteTaskManager> rtmList) {
    Random r = new Random();
    int index = r.nextInt(rtmList.size());
    return rtmList.get(index);
  }

  // run the executor that will administer the previously established completion services
  private List<TaskResult> executeTaskCompletionService(CompletionService<TaskResult> completionService, int tasksSize) throws InterruptedException, ExecutionException, TimeoutException {
    System.out.println(String.format("JobManager %s executing task completion service...", this.uuid.toString()));

    List<TaskResult> responses = new ArrayList<>();
    Future<TaskResult> r;

    try {
      for (int t = 0; t < tasksSize; t++ ) {
        r = completionService.take();
        TaskResult tr = r.get(Task.TIMEOUT, Task.TIMEUNIT);
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

  // helper method to merge the list of TaskResults from all TaskManagers into a single TaskResult
  private TaskResult mergeTaskResults(List<TaskResult> taskResults) {
    System.out.println(String.format("JobManager %s merging task results...", this.uuid.toString()));

    Map<String, Integer> aggregate = new HashMap<>();

    for (TaskResult taskResult: taskResults) {
      aggregate.putAll(taskResult.getResults().getResultData());
    }
    return new ReduceTaskResult(aggregate);
  }

  // method to return the JobResult to the submitter
  @Override
  public void returnResults(JobResult jobResult) {
    System.out.println(String.format("JobManager %s is returning results for Job %s", this.uuid.toString(), jobResult.getJobId().getJobIdNumber()));

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

  // method called by the coordinator to alert the JobManager that it has been assigned a JobId
  @Override
  public void manageJob(JobId jobId) throws RemoteException, NotBoundException {
    this.managedJobIds.add(jobId);
    processJobIdQueue();
  }

  /* ---------- RemoteTaskManager methods ---------- */

  @Override
  public TaskResult performMapTask(Task task, List<Uuid> reducerIds) throws RemoteException, NotBoundException {
//    System.out.println(String.format("TaskManager %s is performing its map Task %s for job %s", this.uuid.toString(), task.getTaskId().getTaskId(), task.getTaskId().getJobId().getJobIdNumber()));

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
        throw e;
      } catch (RemoteException e) {
        System.out.println("TaskManager.performMapTask RemoteException: " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    }

    return new MapTaskResult(map);
  }

  @Override
  public void submitMapResult(String key, int value, JobId jobId) {
//    System.out.println(String.format("TaskManager %s is submitting its MapResult for Job %s", this.uuid.toString(), jobId.getJobIdNumber()));

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
//    System.out.println(String.format("TaskManager %s is performing its reduce Task %s for Job %s", this.uuid.toString(), task.getTaskId().getTaskId(), task.getTaskId().getJobId().getJobIdNumber()));

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
  }

  /* ---------- Identify methods ---------- */

  @Override
  public Uuid getUuid() {
    return this.uuid;
  }
}