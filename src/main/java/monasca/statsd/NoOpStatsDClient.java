package monasca.statsd;

import java.util.Map;

/**
 * A No-Op StatsDClient, which can be substituted in when metrics are not
 * required.
 * 
 */
public final class NoOpStatsDClient extends StatsDClientBase implements StatsDClient {
    public NoOpStatsDClient(String prefix, String hostname, int port, Map<String, String> defaultDimensions,
            StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        super(prefix, hostname, port, defaultDimensions, errorHandler);
    }
    @Override public void stop() { }
    @Override public void count(String aspect, long delta, Map<String, String> defaultDimensions) { }
    @Override public void incrementCounter(String aspect, Map<String, String> defaultDimensions) { }
    @Override public void increment(String aspect, Map<String, String> defaultDimensions) { }
    @Override public void decrementCounter(String aspect, Map<String, String> defaultDimensions) { }
    @Override public void decrement(String aspect, Map<String, String> defaultDimensions) { }
    @Override public void recordGaugeValue(String aspect, double value, Map<String, String> defaultDimensions) { }
    @Override public void gauge(String aspect, double value, Map<String, String> defaultDimensions) { }
    @Override public void recordGaugeValue(String aspect, long value, Map<String, String> defaultDimensions) { }
    @Override public void gauge(String aspect, long value, Map<String, String> defaultDimensions) { }
    @Override public void recordExecutionTime(String aspect, long timeInMs, Map<String, String> defaultDimensions) { }
    @Override public void time(String aspect, long value, Map<String, String> defaultDimensions) { }
    @Override public void recordHistogramValue(String aspect, double value, Map<String, String> defaultDimensions) { }
    @Override public void histogram(String aspect, double value, Map<String, String> defaultDimensions) { }
    @Override public void recordHistogramValue(String aspect, long value, Map<String, String> defaultDimensions) { }
    @Override public void histogram(String aspect, long value, Map<String, String> defaultDimensions) { }
}
