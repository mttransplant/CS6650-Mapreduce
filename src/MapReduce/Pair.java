package MapReduce;

/**
 * This is a generic intended to be used for KeyValuePair as well as other future extensions
 * @param <X>
 * @param <Y>
 */

public class Pair<X, Y> {
    private final X key;
    private final Y value;

    // default initializer
    public Pair(X key, Y value) {
        this.key = key;
        this.value = value;
    }
}