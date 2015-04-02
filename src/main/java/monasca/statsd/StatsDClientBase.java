package monasca.statsd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * Base class for the statsd client 
 */
public class StatsDClientBase {

    protected static final int PACKET_SIZE_BYTES = 1500;
    protected final String prefix;
    protected final DatagramChannel clientChannel;
    protected final InetSocketAddress address;
    protected final StatsDClientErrorHandler handler;
    protected final Map<String, String> defaultDimensions = new HashMap<String, String>();

    /**
     * Create a new base instance
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
    public StatsDClientBase(String prefix, String hostname, int port, Map<String, String> defaultDimensions,
            StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        if(prefix != null && prefix.length() > 0) {
            this.prefix = String.format("%s.", prefix);
        } else {
            this.prefix = "";
        }
        this.handler = errorHandler;

        if(defaultDimensions != null && !defaultDimensions.isEmpty()) {
            this.defaultDimensions.putAll(defaultDimensions);
        }

        try {
            this.clientChannel = DatagramChannel.open();
            this.address = new InetSocketAddress(hostname, port);
        } catch (Exception e) {
            throw new StatsDClientException("Failed to start StatsD client", e);
        }        
    }

    /**
     * Generate a suffix conveying the given dimension list to the client
     */
    protected String dimensionString(Map<String, String> dimensions) {

        if (dimensions == null || dimensions.isEmpty()) {
            return "";
        }

        dimensions.putAll(this.defaultDimensions);
        StringBuilder sb = new StringBuilder("|#{'");
        Iterator<Map.Entry<String, String>> it = dimensions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>)it.next();
            sb.append(entry.getKey());
            sb.append("':'");
            sb.append(entry.getValue());
            if(it.hasNext()) {
                sb.append("',");
            } else {
                sb.append("'");
            }
                
        }
        sb.append("}");

        return sb.toString();
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    protected void stop() {
        try {
            if (clientChannel != null) {
                clientChannel.close();
            }
        }
        catch (Exception e) {
            handler.handle(e);
        }
        finally {
        }
    }

    protected void blockingSend(ByteBuffer sendBuffer) throws IOException {
        int sizeOfBuffer = sendBuffer.position();
        sendBuffer.flip();
        int sentBytes = clientChannel.send(sendBuffer, address);
        sendBuffer.limit(sendBuffer.capacity());
        sendBuffer.rewind();

        if (sizeOfBuffer != sentBytes) {
            handler.handle(
                    new IOException(
                        String.format(
                            "Could not send entirely stat %s to host %s:%d. Only sent %d bytes out of %d bytes",
                            sendBuffer.toString(),
                            address.getHostName(),
                            address.getPort(),
                            sentBytes,
                            sizeOfBuffer)));
        }
    }
}
