package kanjava.aws.servlet;

import org.junit.Before;
import org.junit.Test;

public class TweetMonitorTest {

	TweetMonitor monitor;

	@Before
	public void before() throws Exception {
		monitor = new TweetMonitor(10 * 60 * 1000);
	}

	@Test
	public void getTweetCount1() throws Exception {
		Integer count = monitor.getTweetCount("tksmd");
		System.out.println(count);
	}
}
