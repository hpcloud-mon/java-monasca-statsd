package monasca.statsd;

import java.util.Map;

/**
 * Describes a client connection to a StatsD server, which may be used to post metrics
 * in the form of counters, timers, and gauges.
 *
 * <p>Three key methods are provided for the submission of data-points for the application under
 * scrutiny:
 * <ul>
 *   <li>{@link #incrementCounter} - adds one to the value of the specified named counter</li>
 *   <li>{@link #recordGaugeValue} - records the latest fixed value for the specified named gauge</li>
 *   <li>{@link #recordExecutionTime} - records an execution time in milliseconds for the specified named operation</li>
 * </ul>
 *
 */
public interface StatsDClient {

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    void stop();

    /**
     * Adjusts the specified counter by a given delta.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the counter to adjust
     * @param delta
     *     the amount to adjust the counter by
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void count(String aspect, long delta, Map<String, String> dimensions);

    /**
     * Increments the specified counter by one.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the counter to increment
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void incrementCounter(String aspect, Map<String, String> dimensions);

    /**
     * Convenience method equivalent to {@link #incrementCounter(String, Map<String, String>)}.
     */
    void increment(String aspect, Map<String, String> dimensions);

    /**
     * Decrements the specified counter by one.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the counter to decrement
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void decrementCounter(String aspect, Map<String, String> dimensions);

    /**
     * Convenience method equivalent to {@link #decrementCounter(String, Map<String, String>)}.
     */
    void decrement(String aspect, Map<String, String> dimensions);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the gauge
     * @param value
     *     the new reading of the gauge
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void recordGaugeValue(String aspect, double value, Map<String, String> dimensions);

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, double, Map<String, String>)}.
     */
    void gauge(String aspect, double value, Map<String, String> dimensions);

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the gauge
     * @param value
     *     the new reading of the gauge
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void recordGaugeValue(String aspect, long value, Map<String, String> dimensions);

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, long, Map<String, String>)}.
     */
    void gauge(String aspect, long value, Map<String, String> dimensions);

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the timed operation
     * @param timeInMs
     *     the time in milliseconds
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void recordExecutionTime(String aspect, long timeInMs, Map<String, String> dimensions);

    /**
     * Convenience method equivalent to {@link #recordExecutionTime(String, long, Map<String, String>)}.
     */
    void time(String aspect, long value, Map<String, String> dimensions);

    /**
     * Records a value for the specified named histogram.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the histogram
     * @param value
     *     the value to be incorporated in the histogram
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void recordHistogramValue(String aspect, double value, Map<String, String> dimensions);

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, double, Map<String, String>)}.
     */
    void histogram(String aspect, double value, Map<String, String> dimensions);

    /**
     * Records a value for the specified named histogram.
     *
     * <p>This method is a Monasca extension, and may not work with other servers.</p>
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect
     *     the name of the histogram
     * @param value
     *     the value to be incorporated in the histogram
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    void recordHistogramValue(String aspect, long value, Map<String, String> dimensions);

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, long, Map<String, String>)}.
     */
    void histogram(String aspect, long value, Map<String, String> dimensions);

}
