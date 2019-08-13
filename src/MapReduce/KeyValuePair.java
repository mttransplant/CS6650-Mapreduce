package MapReduce;

import java.io.Serializable;

/**
 * a class to represent a key String to value Integer mapping
 * used in Mapper and Reducer
 */
public class KeyValuePair implements Serializable {
  private String key;
  private Integer value;

  // default initializer
  public KeyValuePair(String key, Integer value) {
    this.key = key;
    this.value = value;
  }

  // method to return this KeyValuePair's key
  public String getKey() { return this.key; }

  // method to return this KeyValuePair's value
  public Integer getValue() { return this.value; }
}