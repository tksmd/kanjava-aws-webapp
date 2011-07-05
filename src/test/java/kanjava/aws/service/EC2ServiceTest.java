package kanjava.aws.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import kanjava.aws.AWSModule;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.Instance;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class EC2ServiceTest {

	EC2Service service;

	@Before
	public void before() throws Exception {
		Injector injector = Guice.createInjector(new AWSModule());
		service = injector.getInstance(EC2Service.class);
	}

	@Test
	public void testGetRunnningInstances1() throws Exception {
		List<Instance> actual = service.getRunnningInstances();
		assertEquals(0, actual.size());
	}

}
