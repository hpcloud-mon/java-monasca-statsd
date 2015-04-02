package monasca.test.statsd;

import java.net.SocketException;

import monasca.statsd.AlertType;
import monasca.statsd.NonBlockingStatsDEventClient;
import monasca.statsd.Priority;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class NonBlockingStatsDEventClientTest {

    private static final int STATSD_SERVER_PORT = 17254;
    private final NonBlockingStatsDEventClient client = new NonBlockingStatsDEventClient("localhost", STATSD_SERVER_PORT);
    private DummyStatsDServer server;

    @Before
    public void start() throws SocketException {
        server = new DummyStatsDServer(STATSD_SERVER_PORT);
    }

    @After
    public void stop() throws Exception {
        client.stop();
        server.close();
    }

    @Test public void
    sends_event1() throws Exception {


        client.event("title", "message");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{5,7}:title|message|h:localhost"));
    }

    @Test public void
    sends_event2() throws Exception {


        client.event("title", "message", 1);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{5,7}:title|message|d:1|h:localhost"));
    }

    @Test public void
    sends_event3() throws Exception {


        client.event("title", "message", AlertType.warning);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{5,7}:title|message|h:localhost|t:warning"));
    }

    @Test public void
    sends_event4() throws Exception {


        client.event("title", "message", Priority.normal);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{5,7}:title|message|h:localhost|p:normal"));
    }

    @Test public void
    sends_event5() throws Exception {


        client.event("title", "message", new String[] {"tag1:tag1", "tag2:tag2"});
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{5,7}:title|message|h:localhost|#tag2:tag2,tag1:tag1"));
    }

    @Test public void
    sends_event6() throws Exception {


        client.event("title", "message", 1, "testAggregationKey", Priority.normal, "sourceTypeName", AlertType.error, new String[] {"tag1:tag1", "tag2:tag2"});
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{5,7}:title|message|d:1|h:localhost|k:testAggregationKey|p:normal|s:sourceTypeName|t:error|#tag2:tag2,tag1:tag1"));
    }

}
