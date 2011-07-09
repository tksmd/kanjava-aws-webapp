package kanjava.aws.service;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import kanjava.aws.AWSModule;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CloudWatchServiceTest {

	CloudWatchService service;

	@Before
	public void before() throws Exception {
		Injector injector = Guice.createInjector(new AWSModule());
		service = injector.getInstance(CloudWatchService.class);
	}

	@Test
	public void putMetrics1() throws Exception {
		Map<String, Number> values = newHashMap();
		values.put("tksmd", 10);
		values.put("dragon3", 20);
		service.putMetricData("test", "user", values);
	}

}
