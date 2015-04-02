package monasca.test.statsd;

import java.net.SocketException;

import monasca.statsd.BlockingStatsDClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class BlockingStatsDClientTest {

    private static final int STATSD_SERVER_PORT = 17254;
    private final BlockingStatsDClient client = new BlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT);
    private DummyStatsDServer server;
    private Map<String, String> dimensions = new HashMap<String, String>();
    private Map<String, String> gaugeDimensions = new HashMap<String, String>();

    @Before
    public void start() throws SocketException {
        dimensions.put("name", "foo");
        dimensions.put("region", "us-west");
        gaugeDimensions.put("environment", "test");
        gaugeDimensions.put("zone", "1");
        server = new DummyStatsDServer(STATSD_SERVER_PORT);
    }

    @After
    public void stop() throws Exception {
        client.stop();
        server.close();
    }

    @Test public void
    sends_counter_value_to_statsd() throws Exception {


        client.count("mycount", 24, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mycount:24|c"));
    }

    @Test public void
    sends_counter_value_to_statsd_with_dimensions() throws Exception {

        client.count("mycount", 24, dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mycount:24|c|#{'name':'foo','region':'us-west'}"));
    }

    @Test public void
    sends_counter_increment_to_statsd() throws Exception {


        client.incrementCounter("myinc", null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.myinc:1|c"));
    }

    @Test public void
    sends_counter_increment_to_statsd_with_dimensions() throws Exception {


        client.incrementCounter("myinc", dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.myinc:1|c|#{'name':'foo','region':'us-west'}"));
    }

    @Test public void
    sends_counter_decrement_to_statsd() throws Exception {


        client.decrementCounter("mydec", null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mydec:-1|c"));
    }

    @Test public void
    sends_counter_decrement_to_statsd_with_dimensions() throws Exception {


        client.decrementCounter("mydec", dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mydec:-1|c|#{'name':'foo','region':'us-west'}"));
    }

    @Test public void
    sends_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 423, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mygauge:423|g"));
    }

    @Test public void
    sends_large_double_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 123456789012345.67890, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mygauge:123456789012345.670000|g"));
    }

    @Test public void
    sends_exact_double_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 123.45678901234567890, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mygauge:123.456789|g"));
    }

    @Test public void
    sends_double_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 0.423, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mygauge:0.423000|g"));
    }

    @Test public void
    sends_gauge_to_statsd_with_dimensions() throws Exception {


        client.recordGaugeValue("mygauge", 423, dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mygauge:423|g|#{'name':'foo','region':'us-west'}"));
    }

    @Test public void
    sends_double_gauge_to_statsd_with_dimensions() throws Exception {


        client.recordGaugeValue("mygauge", 0.423, dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mygauge:0.423000|g|#{'name':'foo','region':'us-west'}"));
    }

    @Test public void
    sends_histogram_to_statsd() throws Exception {


        client.recordHistogramValue("myhistogram", 423, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.myhistogram:423|h"));
    }

    @Test public void
    sends_double_histogram_to_statsd() throws Exception {


        client.recordHistogramValue("myhistogram", 0.423, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.myhistogram:0.423000|h"));
    }

    @Test public void
    sends_histogram_to_statsd_with_dimensions() throws Exception {


        client.recordHistogramValue("myhistogram", 423, dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.myhistogram:423|h|#{'name':'foo','region':'us-west'}"));
    }

    @Test public void
    sends_double_histogram_to_statsd_with_dimensions() throws Exception {


        client.recordHistogramValue("myhistogram", 0.423, dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.myhistogram:0.423000|h|#{'name':'foo','region':'us-west'}"));
    }

    @Test public void
    sends_timer_to_statsd() throws Exception {


        client.recordExecutionTime("mytime", 123, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mytime:123|ms"));
    }

    /**
     * A regression test for <a href="https://github.com/indeedeng/java-dogstatsd-client/issues/3">this i18n number formatting bug</a>
     * @throws Exception
     */
    @Test public void
    sends_timer_to_statsd_from_locale_with_unamerican_number_formatting() throws Exception {

        Locale originalDefaultLocale = Locale.getDefault();

        // change the default Locale to one that uses something other than a '.' as the decimal separator (Germany uses a comma)
        Locale.setDefault(Locale.GERMANY);

        try {


            client.recordExecutionTime("mytime", 123, dimensions);
            server.waitForMessage();

            assertThat(server.messagesReceived(), hasItem("my.prefix.mytime:123|ms|#{'name':'foo','region':'us-west'}"));
        } finally {
            // reset the default Locale in case changing it has side-effects
            Locale.setDefault(originalDefaultLocale);
        }
    }


    @Test public void
    sends_timer_to_statsd_with_dimensions() throws Exception {


        client.recordExecutionTime("mytime", 123, dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.mytime:123|ms|#{'name':'foo','region':'us-west'}"));
    }


    @Test public void
    sends_gauge_mixed_dimensions() throws Exception {
        final BlockingStatsDClient empty_prefix_client = new BlockingStatsDClient("my.prefix",
                                                                                  "localhost",
                                                                                  STATSD_SERVER_PORT,
                                                                                  gaugeDimensions);
        empty_prefix_client.gauge("value", 423, dimensions);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.value:423|g|#{'name':'foo','region':'us-west','environment':'test','zone':'1'}"));
    }

    @Test public void
    sends_gauge_default_dimensions_only() throws Exception {

        final BlockingStatsDClient empty_prefix_client = new BlockingStatsDClient("my.prefix",
                                                                                  "localhost",
                                                                                  STATSD_SERVER_PORT,
                                                                                  gaugeDimensions);
        empty_prefix_client.gauge("value", 423, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("my.prefix.value:423|g|#{'environment':'test','zone':'1'}"));
    }

    @Test public void
    sends_gauge_empty_prefix() throws Exception {

        final BlockingStatsDClient empty_prefix_client = new BlockingStatsDClient("", "localhost", STATSD_SERVER_PORT);
        empty_prefix_client.gauge("top.level.value", 423, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("top.level.value:423|g"));
    }

    @Test public void
    sends_gauge_null_prefix() throws Exception {

        final BlockingStatsDClient null_prefix_client = new BlockingStatsDClient(null, "localhost", STATSD_SERVER_PORT);
        null_prefix_client.gauge("top.level.value", 423, null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), hasItem("top.level.value:423|g"));
    }
}
