import java.util.Map;

import MapReduce.Mapper;
import MapReduce.Reducer;
import MapReduce.WordCountMapper;
import MapReduce.WordCountReducer;

// TODO: disallow a User to submit a job if the network has only 1 peer (i.e., itself)

public class LaunchPeer {
  public static void main(String[] args) {
    int numPeers = 40;
    int portBase = 5000;
    Peer[] peers = new Peer[numPeers];

    for (int i = 0; i < numPeers; i++) {
      peers[i] = new Peer(portBase + i);
      peers[i].join();
    }

    String[] wordArray = "a a a b b b c c c d d d e e e f f f g g g h h h i i i j j j k k k l l l m m m n n n o o o p p p q q q r r r s s s t t t u u u v v v w w w x x x y y y z z z".split("\\W+");

    JobData jobData = new JobData(wordArray);
    JobId jobId = new JobId(peers[1].getUuid(), jobData.getSize());
    Mapper mapper = new WordCountMapper();
    Reducer reducer = new WordCountReducer();

    peers[1].createJob(jobId, jobData, mapper, reducer);
    peers[1].submitJob(jobId);

    Map<String, JobResult> results = peers[1].getResults();

    while (results.size() == 0) {
      System.out.println("No results yet, trying again...");

      try {
        Thread.sleep(10 * 1000);
      } catch (InterruptedException ie) {

      }

      results = peers[1].getResults();
    }

    JobResult result = results.get(jobId.getJobIdNumber());
    result.print();
  }
}