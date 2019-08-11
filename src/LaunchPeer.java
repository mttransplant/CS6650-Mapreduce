import java.util.Arrays;
import java.util.List;
import java.util.Map;

import MapReduce.Mapper;
import MapReduce.Reducer;
import MapReduce.WordCountMapper;
import MapReduce.WordCountReducer;

// TODO: disallow a User to submit a job if the network has only 1 peer (i.e., itself)

public class LaunchPeer {
  public static void main(String[] args) {
    int numPeers = 10;
    int portBase = 5000;
    Peer[] peers = new Peer[numPeers];

    for (int i = 0; i < numPeers; i++) {
      peers[i] = new Peer(portBase + i);
      peers[i].join();
    }

    String[] wordArray = "This string has five\nwords.".split("\\W+");
    List<String> wordList = Arrays.asList(wordArray);

    JobData jobData = new JobData(wordList);
    JobId jobId = new JobId(peers[1].getUuid(), jobData.getSize());
    Mapper mapper = new WordCountMapper();
    Reducer reducer = new WordCountReducer();

    peers[0].leave();
    peers[1].createJob(jobId, jobData, mapper, reducer);
    peers[1].submitJob(jobId);

    Map<String, JobResult> results = peers[1].getResults();

    JobResult result;

    if (results.size() > 0) {
      result = results.get(jobId.getJobIdNumber());
      result.print();
    }
  }
}