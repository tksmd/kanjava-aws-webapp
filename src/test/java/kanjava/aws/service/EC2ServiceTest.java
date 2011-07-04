package kanjava.aws.service;

import kanjava.aws.AWSModule;

import org.junit.Before;
import org.junit.Test;

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

	}

}
