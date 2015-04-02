package monasca.statsd;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/** 
 * 
 * A simple StatsD client implementation facilitating event reporting.
 *
 * <p>Upon instantiation, this client will establish a socket connection to a StatsD instance
 * running on the specified host and port. Events are then sent over this connection as they are
 * received by the client.
 * </p>
 *
 * <p>Six key methods are provided for the submission of events for the application under
 * scrutiny:
 * <ul>
 *   <li>{@link #event(String, String)}</li>
 *   <li>{@link #event(String, String, AlertType)}</li>
 *   <li>{@link #event(String, String, long)}</li>
 *   <li>{@link #event(String, String, Priority)}</li>
 *   <li>{@link #event(String, String, String...)}</li>
 *   <li>{@link #event(String, String, long, String, Priority, String, AlertType, Map<String, String>)}</li>
 * </ul>
 * From the perspective of the application, these methods are blocking.
 * </p>
 *
 * <p>As part of a clean system shutdown, the {@link #stop()} method should be invoked
 * on any StatsD clients.</p>
 * 
 */
public class BlockingStatsDEventClient extends StatsDClientBase {

    private static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {
        @Override
        public void handle(Exception e) { /* No-op */ }
    };

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
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDEventClient(String prefix, String hostname, int port) throws StatsDClientException {
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
     *     dimensions to be added to all content sent (each of them should be in the format key:value)
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDEventClient(String prefix, String hostname, int port, Map<String, String> defaultDimensions) throws StatsDClientException {
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
     *     dimensions to be added to all content sent (each of them should be in the format key:value)
     * @param errorHandler
     *     handler to use when an exception occurs during usage
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDEventClient(String prefix, String hostname, int port, Map<String, String> defaultDimensions,
            StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        super(prefix, hostname, port, defaultDimensions, errorHandler);
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    public void stop() {
        super.stop();
    }

    /**
     * Submit event with title and message
     * @param title
     * @param message
     */
    public boolean event( String title, String message ) {
        return event(title, message, 0, null, null, null, null, null);
    }
    
    /**
     * Submit event with title, message and a time
     * @param title
     * @param message
     * @param dateHappened - It should be in seconds
     */
    public boolean event( String title, String message, long dateHappened ) {
    	    return event(title, message, dateHappened, null, null, null, null, null);
    }
    
    /**
     * Submit event with title, message and priority for the message
     * @param title
     * @param message
     * @param priority
     */
    public boolean event( String title, String message, Priority priority ) {
    	    return event( title, message, 0L, null, priority, null, null, null);
    }
    
    /**
     * Submit event with title, message and alter type
     * @param title
     * @param message
     * @param alertType
     */
    public boolean event( String title, String message, AlertType alertType ) {
    	    return event( title, message, 0, null, null, null, alertType, null);
    }
    
    /**
     * Submit message with title, message and tags
     * @param title
     * @param message
     * @param tags
     */
    public boolean event( String title, String message, Map<String, String> dimensions ) {
        return event( title, message, 0L, null, null, null, null, dimensions);
    }
    
    /**
     * 
     * Reports an event to the monasca agent
     * 
     * @param title
     * @param message
     * @param dateHappened
     * @param aggregationKey
     * @param priority
     * @param sourceTypeName
     * @param alterType
     * @param dimensions
     */
    public boolean event(String title, String message, long dateHappened, String aggregationKey, 
			Priority priority, String sourceTypeName, AlertType alterType, Map<String, String> dimensions) {
        boolean success = true;
        	if (title != null && message != null) {
    	    	    try {
                blockingSend(prepareMessage(title, message, dateHappened, aggregationKey, priority, sourceTypeName, alterType, dimensions));
            } catch (IOException e) {
                success = false;
            }
        	}
        	return success;
    }
    
    protected String prepareMessage(String title, String message, long dateHappened, String aggregationKey, 
			Priority priority, String sourceTypeName, AlertType alterType, Map<String, String> dimensions) {
        	StringBuilder sb = new StringBuilder();
        	sb.append(String.format("_e{%d,%d}:%s|%s", title.length(), message.length(), title, message));
        	if (dateHappened > 0) {
        		sb.append(String.format("|d:%d", dateHappened));
        	}
        	if (super.address.getHostName() != null) {
        		sb.append(String.format("|h:%s", super.address.getHostName()));
        	}
        	if (aggregationKey != null) {
        		sb.append(String.format("|k:%s", aggregationKey));
        	}
        	if (priority != null) {
        		sb.append(String.format("|p:%s", priority.name()));
        	}
        	if (sourceTypeName != null) {
        		sb.append(String.format("|s:%s", sourceTypeName));
        	}
        	if (alterType != null) {
        		sb.append(String.format("|t:%s", alterType.name()));
        	}
        	sb.append(dimensionString(dimensions));
        	return sb.toString();
    }

    protected void blockingSend(String message) throws IOException {
        final ByteBuffer sendBuffer = ByteBuffer.allocate(PACKET_SIZE_BYTES);
        sendBuffer.put(message.getBytes());
        super.blockingSend(sendBuffer);
    }
}
