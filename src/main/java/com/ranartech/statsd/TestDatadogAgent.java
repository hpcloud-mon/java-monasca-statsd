package com.ranartech.statsd;

import org.junit.Test;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

public class TestDatadogAgent {

	private static final StatsDClient statsd = new NonBlockingStatsDClient(
			"com.binary.charts", /* prefix to any stats; may be null or empty string */
			"chartreport.binary.com", /* common case: localhost */
			8125, /* port */
			new String[] { "tag:value" } /*
										 * DataDog extension: Constant tags,
										 * always applied
										 */
	);

	@Test
	public void test() {
    
		statsd.increment("maven_test_counter");
	    statsd.recordGaugeValue("maven_test_gauge_int", 100);
	    statsd.recordGaugeValue("maven_test_gauge_float", 0.01); /* DataDog extension: support for floating-point gauges */
	    statsd.recordHistogramValue("maven_test_histogram_int", 15);     /* DataDog extension: histograms */
	    statsd.recordHistogramValue("maven_test_histogram_float", 15.5);   /* ...also floating-point */
	
	    /* Compatibility note: Unlike upstream statsd, DataDog expects execution times to be a
	     * floating-point value in seconds, not a millisecond value. This library
	     * does the conversion from ms to fractional seconds.
	     */
	    statsd.recordExecutionTime("maven_test_executionTime", 25, "cluster:foo"); /* DataDog extension: cluster tag */
	    
	    try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	}
	
	public static void main(String[] args) {
		new TestDatadogAgent().test();
	}

	
}