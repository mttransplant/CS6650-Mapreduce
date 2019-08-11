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
    Peer peer1 = new Peer(2099);
    peer1.join();

    Peer peer2 = new Peer(3099);
    peer2.join();

    String[] wordArray = "This string has five\nwords.".split("\\W+");
    List<String> wordList = Arrays.asList(wordArray);

    JobData jobData = new JobData(wordList);
    JobId jobId = new JobId(peer2.getUuid(), jobData.getSize());
    Mapper mapper = new WordCountMapper();
    Reducer reducer = new WordCountReducer();

    peer2.createJob(jobId, jobData, mapper, reducer);

    peer2.submitJob(jobId);

    Map<String, JobResult> results = peer2.getResults();

    JobResult result;

    if (results.size() > 0) {
      result = results.get(jobId.getJobIdNumber());
      result.print();
    }

    peer1.leave();
    peer2.leave();
  }
}