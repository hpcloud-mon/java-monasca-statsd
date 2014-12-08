package com.ranartech.statsd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.NumberFormat;
import java.util.Locale;

import com.timgroup.statsd.StatsDClientErrorHandler;
import com.timgroup.statsd.StatsDClientException;

/** 
 * @author Arnab Karmakar
 *
 */
public class BlockingStatsDEventClient  {

    private static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {
        @Override public void handle(Exception e) { /* No-op */ }
    };

    /**
     * Because NumberFormat is not thread-safe we cannot share instances across threads. Use a ThreadLocal to
     * create one pre thread as this seems to offer a significant performance improvement over creating one per-thread:
     * http://stackoverflow.com/a/1285297/2648
     * https://github.com/indeedeng/java-dogstatsd-client/issues/4
     */
    protected static final ThreadLocal<NumberFormat> NUMBER_FORMATTERS = new ThreadLocal<NumberFormat>() {
        @Override
        protected NumberFormat initialValue() {

            // Always create the formatter for the US locale in order to avoid this bug:
            // https://github.com/indeedeng/java-dogstatsd-client/issues/3
            NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
            numberFormatter.setGroupingUsed(false);
            numberFormatter.setMaximumFractionDigits(6);
            return numberFormatter;
        }
    };

    protected final DatagramSocket clientSocket;
    protected final StatsDClientErrorHandler handler;
    protected final String[] constantTags;

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
    public BlockingStatsDEventClient( String hostname, int port) throws StatsDClientException {
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
     *     tags to be added to all content sent
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDEventClient( String hostname, int port, String[] constantTags) throws StatsDClientException {
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
     *     tags to be added to all content sent
     * @param errorHandler
     *     handler to use when an exception occurs during usage
     * @throws StatsDClientException
     *     if the client could not be started
     */
    public BlockingStatsDEventClient(String hostname, int port, String[] constantTags, StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        this.handler = errorHandler;
        if(constantTags != null && constantTags.length == 0) {
            constantTags = null;
        }
        this.constantTags = constantTags;

        try {
            this.clientSocket = new DatagramSocket();
            this.clientSocket.connect(new InetSocketAddress(hostname, port));
        } catch (Exception e) {
            throw new StatsDClientException("Failed to start StatsD client", e);
        }
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    public void stop() {
        if (clientSocket != null) {
        	try {
        		clientSocket.close();
        	} catch(Exception ignore) {}
        }
    }

    /**
     * Generate a suffix conveying the given tag list to the client
     */
    String tagString(String[] tags) {
        boolean have_call_tags = (tags != null && tags.length > 0);
        boolean have_constant_tags = (constantTags != null && constantTags.length > 0);
        if(!have_call_tags && !have_constant_tags) {
            return "";
        }
        StringBuilder sb = new StringBuilder("|#");
        if(have_constant_tags) {
            for(int n=constantTags.length - 1; n>=0; n--) {
                sb.append(constantTags[n]);
                if(n > 0 || have_call_tags) {
                    sb.append(",");
                }
            }
        }
        if (have_call_tags) {
            for(int n=tags.length - 1; n>=0; n--) {
                sb.append(tags[n]);
                if(n > 0) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 
     * Reports an event to the datadog agent
     * 
     * @param title
     * @param message
     * @param dateHappened
     * @param hostname
     * @param aggregationKey
     * @param priority
     * @param sourceTypeName
     * @param alterType
     * @param tags
     */
    public void errorEvent(final String title, final String message, final long dateHappened, final String hostname, final String aggregationKey, 
			final Priority priority, final String sourceTypeName, final AlterType alterType, final String... tags) {
    	if (title != null && message != null) {
	    	blockingSend(prepareMessage(title, message, dateHappened, hostname, aggregationKey, priority, sourceTypeName, alterType, tagString(tags)));
    	}
    }
    
    protected String prepareMessage(final String title, final String message, final long dateHappened, final String hostname, final String aggregationKey, 
			final Priority priority, final String sourceTypeName, final AlterType alterType, final String... tags) {
    	return String.format("_e{%d,%d}:%s|%s|d:%d|h:%s|k:%s|p:%s|s:%s|t:%s%s", 
    			title.length(), message.length(), title, message, dateHappened, hostname, aggregationKey, priority.name(), sourceTypeName, alterType.name(), tagString(tags));
    }

    private void blockingSend(String message) {
        try {
            final byte[] sendData = message.getBytes();
            final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
            clientSocket.send(sendPacket);
        } catch (Exception e) {
            handler.handle(e);
        }
    }
    
}