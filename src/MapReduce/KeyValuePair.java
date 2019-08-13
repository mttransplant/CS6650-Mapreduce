package MapReduce;

import java.io.Serializable;

public class KeyValuePair implements Serializable {
  String key;
  Integer value;

  public KeyValuePair(String key, Integer value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() { return this.key; }
  public Integer getValue() { return this.value; }
}