package kanjava.aws.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import kanjava.aws.AWSModule;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
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
	public void getRunnningInstances1() throws Exception {
		List<Instance> actual = service.getRunnningInstances();
		assertEquals(0, actual.size());
	}

	@Test
	public void runInstance1() throws Exception {
		Instance actual = service.runInstance("ami-bef045bf","tomcat");
		System.out.println(actual);
	}

	@Test
	public void terminateInstance1() throws Exception {
		InstanceStateChange actual = service.terminateInstance("i-105d1b11");
		System.out.println(actual);
	}

	@Test
	public void createVolume1() throws Exception {
		Volume actual = service.createVolume();
		System.out.println(actual);
	}

	@Test
	public void attachVolume1() throws Exception {
		VolumeAttachment actual = service.attachVolume("i-105d1b11",
				"vol-d83196b2", "/dev/sdf");
		System.out.println(actual);
	}

	@Test
	public void dettachVolume1() throws Exception {
		VolumeAttachment actual = service.detachVolume("vol-d83196b2");
		System.out.println(actual);
	}

	@Test
	public void deleteVolume1() throws Exception {
		service.deleteVolume("vol-d83196b2");
	}

}
