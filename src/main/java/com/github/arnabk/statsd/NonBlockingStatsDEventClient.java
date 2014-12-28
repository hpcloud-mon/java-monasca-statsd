package com.github.arnabk.statsd;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.timgroup.statsd.StatsDClientErrorHandler;
import com.timgroup.statsd.StatsDClientException;

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
 *   <li>{@link #event(String, String)} - inherited from base class {@link BlockingStatsDEventClient}</li>
 *   <li>{@link #event(String, String, AlertType)} - inherited from base class {@link BlockingStatsDEventClient}</li>
 *   <li>{@link #event(String, String, long)} - inherited from base class {@link BlockingStatsDEventClient}</li>
 *   <li>{@link #event(String, String, Priority)} - inherited from base class {@link BlockingStatsDEventClient}</li>
 *   <li>{@link #event(String, String, String...)} - inherited from base class {@link BlockingStatsDEventClient}</li>
 *   <li>{@link #event(String, String, long, String, Priority, String, AlertType, String...)} - overriding from base class {@link BlockingStatsDEventClient} and converting it to non-blocking</li>
 * </ul>
 * From the perspective of the application, these methods are non-blocking, with the resulting
 * IO operations being carried out in a separate thread. Furthermore, these methods are guaranteed
 * not to throw an exception which may disrupt application execution.
 * </p>
 *
 * <p>As part of a clean system shutdown, the {@link #stop()} method should be invoked
 * on any StatsD clients.</p>
 * 
 * @author Arnab Karmakar
 *
 */
public final class NonBlockingStatsDEventClient extends BlockingStatsDEventClient {

    private static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {
        @Override public void handle(Exception e) { /* No-op */ }
    };
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        final ThreadFactory delegate = Executors.defaultThreadFactory();
        @Override public Thread newThread(Runnable r) {
            Thread result = delegate.newThread(r);
            result.setName("StatsD-" + result.getName());
            return result;
        }
    });
    
    private final BlockingQueue<EventMessage> blockingQueue = new LinkedBlockingDeque<EventMessage>();

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
     * @param hostname
     *     the host name of the targeted StatsD server
     * @param port
     *     the port of the targeted StatsD server
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public NonBlockingStatsDEventClient( String hostname, int port) throws StatsDClientException {
        this(hostname, port, null, NO_OP_HANDLER);
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
     * @param hostname
     *     the host name of the targeted StatsD server
     * @param port
     *     the port of the targeted StatsD server
     * @param constantTags
     *     tags to be added to all content sent (each of them should be in the format key:value)
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public NonBlockingStatsDEventClient( String hostname, int port, String[] constantTags) throws StatsDClientException {
        this( hostname, port, constantTags, NO_OP_HANDLER);
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
     * @param constantTags
     *     tags to be added to all content sent (each of them should be in the format key:value)
     * @param errorHandler
     *     handler to use when an exception occurs during usage
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public NonBlockingStatsDEventClient(String hostname, int port, String[] constantTags, StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        super(hostname, port, constantTags, errorHandler);
        executor.execute(new Runnable() {
			
			@Override
			public void run() {
				while (!executor.isShutdown()) {
					try {
						EventMessage eMsg = blockingQueue.poll(1, TimeUnit.SECONDS);
						if (eMsg != null) {
							blockingSend(
								prepareMessage(
										eMsg.getTitle(), eMsg.getMessage(), 
										eMsg.getDateHappened(), eMsg.getAggregationKey(), 
										eMsg.getPriority(), eMsg.getSourceTypeName(), 
										eMsg.getAlterType(), eMsg.getTags()));
						}
					} catch (Exception e) {
						handler.handle(e);
					}
				}
			}
		});
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    @Override
    public void stop() {
    	 try {
             executor.shutdown();
             executor.awaitTermination(30, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             handler.handle(e);
         }
         finally {
             super.stop();
         }
    }

    /**
     * 
     * Reports an event to the datadog agent (non-blocking)
     * 
     * @param title
     * @param message
     * @param dateHappened
     * @param aggregationKey
     * @param priority
     * @param sourceTypeName
     * @param alterType
     * @param tags -> Each item in this array should in the format key:value
     */
    @Override
    public void event(final String title, final String message, final long dateHappened, final String aggregationKey, 
    		final Priority priority, final String sourceTypeName, final AlertType alterType, final String... tags) {
    	if (title != null && message != null) {
    		final EventMessage eMsg = new EventMessage(title, message);
    		eMsg.setDateHappened(dateHappened);
    		eMsg.setAggregationKey(aggregationKey);
    		eMsg.setAlterType(alterType);
    		eMsg.setPriority(priority);
    		eMsg.setSourceTypeName(sourceTypeName);
    		eMsg.setTags(tags);
    		blockingQueue.offer(eMsg);
    	}
    }
    
}
