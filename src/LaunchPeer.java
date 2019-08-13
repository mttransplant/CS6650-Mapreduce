import java.util.Map;

import MapReduce.Mapper;
import MapReduce.Reducer;
import MapReduce.WordCountMapper;
import MapReduce.WordCountReducer;

// TODO: disallow a User to submit a job if the network has only 1 peer (i.e., itself)

public class LaunchPeer implements Runnable {
  private String job;
  private Peer peer;

  public LaunchPeer(String job, int port) {
    this.job = job;
    this.peer = new Peer(port);
    this.peer.join();
  }

  @Override
  public void run() {
    String[] wordArray = job.split("\\W+");

    JobData jobData = new JobData(wordArray);
    JobId jobId = new JobId(this.peer.getUuid(), jobData.getSize());
    Mapper mapper = new WordCountMapper();
    Reducer reducer = new WordCountReducer();

    this.peer.createJob(jobId, jobData, mapper, reducer);
    this.peer.submitJob(jobId);

    Map<String, JobResult> results = this.peer.getResults();

    while (results.size() == 0) {
      System.out.println("No results yet, trying again...");

      try {
        Thread.sleep(10 * 1000);
      } catch (InterruptedException ie) {

      }

      results = this.peer.getResults();
    }

    JobResult result = results.get(jobId.getJobIdNumber());
    result.print();
  }
}