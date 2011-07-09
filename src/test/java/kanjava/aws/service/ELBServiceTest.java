package kanjava.aws.service;

import java.util.List;

import kanjava.aws.AWSModule;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ELBServiceTest {

	ELBService service;

	@Before
	public void before() throws Exception {
		Injector injector = Guice.createInjector(new AWSModule());
		service = injector.getInstance(ELBService.class);
	}

	@Test
	public void createLoadBalancer1() throws Exception {
		String actual = service.createLoadBalancer("test-load-balancer");
		System.out.println(actual);
	}

	@Test
	public void deleteLoadBalancer1() throws Exception {
		service.deleteLoadBalancer("test-load-balancer");
	}

	@Test
	public void registerInstanceWithLoadBalancer1() throws Exception {
		List<Instance> actual = service.registerInstanceWithLoadBalancer(
				"test-load-balancer", "i-105d1b11");
		System.out.println(actual);
	}

	@Test
	public void deregisterInstanceWithLoadBalancer1() throws Exception {
		List<Instance> actual = service.deregisterInstanceWithLoadBalancer(
				"test-load-balancer", "i-105d1b11");
		System.out.println(actual);
	}

}
