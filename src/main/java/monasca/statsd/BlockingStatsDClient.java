package monasca.statsd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.util.Precision;

/**
 * A simple StatsD client implementation facilitating metrics recording.
 * 
 * <p>Upon instantiation, this client will establish a socket connection to a StatsD instance
 * running on the specified host and port. Metrics are then sent over this connection as they are
 * received by the client.
 * </p>
 * 
 * <p>Three key methods are provided for the submission of data-points for the application under
 * scrutiny:
 * <ul>
 *   <li>{@link #incrementCounter} - adds one to the value of the specified named counter</li>
 *   <li>{@link #recordGaugeValue} - records the latest fixed value for the specified named gauge</li>
 *   <li>{@link #recordExecutionTime} - records an execution time in milliseconds for the specified named operation</li>
 *   <li>{@link #recordHistogramValue} - records a value, to be tracked with average, maximum, and percentiles</li>
 * </ul>
 * </p>
 * 
 * <p>As part of a clean system shutdown, the {@link #stop()} method should be invoked
 * on any StatsD clients.</p>
 * 
 * <p>This class is a blocking implementation. It is preferable to use with already existing threading systems or logging systems like slf4j(log4j implementation)</p>
 * 
 */
public class BlockingStatsDClient extends StatsDClientBase implements StatsDClient {

	protected static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {
        @Override public void handle(Exception e) { /* No-op */ }
    };

    /**
     * Create a new StatsD client communicating with a StatsD instance on the
     * specified host and port. All messages sent via this client will have
     * their keys prefixed with the specified string. The new client will
     * attempt to open a connection to the StatsD server immediately upon
     * instantiation, and may throw an exception if a connection cannot
     * be established. Once a client has been instantiated in this way, all
     * exceptions thrown during subsequent usage are consumed, guaranteeing
     * that failures in metrics will not affect normal code execution.
     *
     * @param prefix
     *     the prefix to apply to keys sent via this client
     * @param hostname
     *     the host name of the targeted StatsD server
     * @param port
     *     the port of the targeted StatsD server
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDClient(String prefix, String hostname, int port) throws StatsDClientException {
        this(prefix, hostname, port, null, NO_OP_HANDLER);
    }

    /**
     * Create a new StatsD client communicating with a StatsD instance on the
     * specified host and port. All messages send via this client will have
     * their keys prefixed with the specified string. The new client will
     * attempt to open a connection to the StatsD server immediately upon
     * instantiation, and may throw an exception if that a connection cannot
     * be established. Once a client has been instantiated in this way, all
     * exceptions thrown during subsequent usage are consumed, guaranteeing
     * that failures in metrics will not affect normal code execution.
     * 
     * @param prefix
     *     the prefix to apply to keys sent via this client
     * @param hostname
     *     the host name of the targeted StatsD server
     * @param port
     *     the port of the targeted StatsD server
     * @param defaultDimensions
     *     dimensions to be added to all content sent
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDClient(String prefix, String hostname, int port, Map<String, String> defaultDimensions)
        throws StatsDClientException {
        this(prefix, hostname, port, defaultDimensions, NO_OP_HANDLER);
    }

    /**
     * Create a new StatsD client communicating with a StatsD instance on the
     * specified host and port. All messages send via this client will have
     * their keys prefixed with the specified string. The new client will
     * attempt to open a connection to the StatsD server immediately upon
     * instantiation, and may throw an exception if that a connection cannot
     * be established. Once a client has been instantiated in this way, all
     * exceptions thrown during subsequent usage are passed to the specified
     * handler and then consumed, guaranteeing that failures in metrics will
     * not affect normal code execution.
     * 
     * @param prefix
     *     the prefix to apply to keys sent via this client
     * @param hostname
     *     the host name of the targeted StatsD server
     * @param port
     *     the port of the targeted StatsD server
     * @param defaultDimensions
     *     dimensions to be added to all content sent
     * @param errorHandler
     *     handler to use when an exception occurs during usage
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDClient(String prefix, String hostname, int port, Map<String, String> defaultDimensions,
            StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        super(prefix, hostname, port, defaultDimensions, errorHandler);
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    @Override
    public void stop() {
        super.stop();
    }

    /**
     * Adjusts the specified counter by a given delta.
     * 
     * 
     * @param aspect
     *     the name of the counter to adjust
     * @param delta
     *     the amount to adjust the counter by
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void count(String aspect, long delta, Map<String, String> dimensions) {
    	    blockingSend(String.format("%s%s:%d|c%s", prefix, aspect, delta, dimensionString(dimensions)));
    }

    /**
     * Increments the specified counter by one.
     * 
     * 
     * @param aspect
     *     the name of the counter to increment
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void incrementCounter(String aspect, Map<String, String> dimensions) {
        count(aspect, 1, dimensions);
    }

    /**
     * Convenience method equivalent to {@link #incrementCounter(String, Map<String, String>)}. 
     */
    @Override
    public void increment(String aspect, Map<String, String> dimensions) {
        incrementCounter(aspect, dimensions);
    }

    /**
     * Decrements the specified counter by one.
     * 
     * 
     * @param aspect
     *     the name of the counter to decrement
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void decrementCounter(String aspect, Map<String, String> dimensions) {
        count(aspect, -1, dimensions);
    }

    /**
     * Convenience method equivalent to {@link #decrementCounter(String, Map<String, String>)}. 
     */
    @Override
    public void decrement(String aspect, Map<String, String> dimensions) {
        decrementCounter(aspect, dimensions);
    }

    /**
     * Records the latest fixed value for the specified named gauge.
     * 
     * 
     * @param aspect
     *     the name of the gauge
     * @param value
     *     the new reading of the gauge
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void recordGaugeValue(String aspect, double value, Map<String, String> dimensions) {
    	    blockingSend(String.format("%s%s:%f|g%s", prefix, aspect, Precision.round(value, 6), dimensionString(dimensions)));
    }

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, double, Map<String, String>)}.
     */
    @Override
    public void gauge(String aspect, double value, Map<String, String> dimensions) {
        recordGaugeValue(aspect, value, dimensions);
    }


    /**
     * Records the latest fixed value for the specified named gauge.
     * 
     * 
     * @param aspect
     *     the name of the gauge
     * @param value
     *     the new reading of the gauge
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void recordGaugeValue(String aspect, long value, Map<String, String> dimensions) {
    	    blockingSend(String.format("%s%s:%d|g%s", prefix, aspect, value, dimensionString(dimensions)));
    }

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, int, Map<String, String>)}. 
     */
    @Override
    public void gauge(String aspect, long value, Map<String, String> dimensions) {
        recordGaugeValue(aspect, value, dimensions);
    }

    /**
     * Records an execution time in milliseconds for the specified named operation.
     * 
     * 
     * @param aspect
     *     the name of the timed operation
     * @param timeInMs
     *     the time in milliseconds
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void recordExecutionTime(String aspect, long timeInMs, Map<String, String> dimensions) {
        blockingSend(String.format("%s%s:%d|ms%s", prefix, aspect, timeInMs, dimensionString(dimensions)));
    }

    /**
     * Convenience method equivalent to {@link #recordExecutionTime(String, long, Map<String, String>)}.
     */
    @Override
    public void time(String aspect, long value, Map<String, String> dimensions) {
        recordExecutionTime(aspect, value, dimensions);
    }

    /**
     * Records a value for the specified named histogram.
     *
     *
     * @param aspect
     *     the name of the histogram
     * @param value
     *     the value to be incorporated in the histogram
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void recordHistogramValue(String aspect, double value, Map<String, String> dimensions) {
    	    blockingSend(String.format("%s%s:%f|h%s", prefix, aspect, Precision.round(value, 6), dimensionString(dimensions)));
    }

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, double, Map<String, String>)}.
     */
    @Override
    public void histogram(String aspect, double value, Map<String, String> dimensions) {
        recordHistogramValue(aspect, value, dimensions);
    }

    /**
     * Records a value for the specified named histogram.
     * 
     * 
     * @param aspect
     *     the name of the histogram
     * @param value
     *     the value to be incorporated in the histogram
     * @param dimensions
     *     map of dimensions to be added to the data
     */
    @Override
    public void recordHistogramValue(String aspect, long value, Map<String, String> dimensions) {
        blockingSend(String.format("%s%s:%d|h%s", prefix, aspect, value, dimensionString(dimensions)));
    }

    /**
     * Convenience method equivalent to {@link #recordHistogramValue(String, int, Map<String, String>)}. 
     */
    @Override
    public void histogram(String aspect, long value, Map<String, String> dimensions) {
        recordHistogramValue(aspect, value, dimensions);
    }

    private void blockingSend(String message) throws IOException {
        final ByteBuffer sendBuffer = ByteBuffer.allocate(PACKET_SIZE_BYTES);
        sendBuffer.put(message.getBytes());
        super.blockingSend(sendBuffer);
    }
}
