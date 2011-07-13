package kanjava.aws.action;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.seasar.cubby.unit.CubbyAssert.assertPathEquals;
import static org.seasar.cubby.unit.CubbyRunner.processAction;
import kanjava.aws.ApplicationModule;
import kanjava.aws.service.EC2Service;

import org.junit.Before;
import org.junit.Test;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Json;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

public class ApiActionTest {

	MockServletContext servletContext;

	MockHttpServletRequest request;

	MockHttpServletResponse response;

	MockAWSModule module;

	@Before
	public void before() throws Exception {
		this.servletContext = new MockServletContext();
		this.module = new MockAWSModule();
		Injector injector = Guice.createInjector(new ApplicationModule(),
				this.module);
		servletContext.setAttribute(Injector.class.getName(), injector);

		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();
	}

	/**
	 * パラメータが足りなくてバリデーションにかかる 1
	 * 
	 * @throws Exception
	 */
	@Test
	public void ebs1() throws Exception {
		request.setMethod("GET");
		request.addHeader("X-Requested-With", "XMLHttpRequest");
		request.setServletPath("/api/ebs/attach/vol-xxxxxx");
		ActionResult actualResult = processAction(servletContext, request,
				response, new GuiceFilter());
		assertPathEquals(Json.class, "", actualResult);
	}

	/**
	 * パラメータが足りなくてバリデーションにかかる 2
	 * 
	 * @throws Exception
	 */
	@Test
	public void ebs2() throws Exception {
		request.setMethod("GET");
		request.addHeader("X-Requested-With", "XMLHttpRequest");
		request.addParameter("instanceId", "i-xxxxxx");
		request.setServletPath("/api/ebs/attach/vol-xxxxxx");
		ActionResult actualResult = processAction(servletContext, request,
				response, new GuiceFilter());
		assertPathEquals(Json.class, "", actualResult);
	}

	/**
	 * パラメータが足りなくてバリデーションにかかる 3
	 * 
	 * @throws Exception
	 */
	@Test
	public void ebs3() throws Exception {
		request.setMethod("GET");
		request.addHeader("X-Requested-With", "XMLHttpRequest");
		request.addParameter("device", "/dev/sdf");
		request.setServletPath("/api/ebs/attach/vol-xxxxxx");
		ActionResult actualResult = processAction(servletContext, request,
				response, new GuiceFilter());
		assertPathEquals(Json.class, "", actualResult);
	}

	/**
	 * ちゃんと呼び出される 4
	 * 
	 * @throws Exception
	 */
	@Test
	public void ebs4() throws Exception {
		request.setMethod("GET");
		request.addHeader("X-Requested-With", "XMLHttpRequest");
		request.addParameter("device", "/dev/sdf");
		request.addParameter("instanceId", "i-xxxxxx");

		VolumeAttachment attachment = new VolumeAttachment()
				.withDevice("/dev/sdf");
		expect(module.ec2.attachVolume("i-xxxxxx", "vol-xxxxxx", "/dev/sdf"))
				.andReturn(attachment);
		replay(module.ec2);

		request.setServletPath("/api/ebs/attach/vol-xxxxxx");
		ActionResult actualResult = processAction(servletContext, request,
				response, new GuiceFilter());
		assertPathEquals(Json.class, "", actualResult);

		Json json = Json.class.cast(actualResult);
		assertTrue(attachment.equals(json.getBean()));

		verify(module.ec2);
	}

	static class MockAWSModule extends AbstractModule {

		EC2Service ec2;

		@Override
		protected void configure() {
			ec2 = createMock(EC2Service.class);
			bind(EC2Service.class).toInstance(ec2);
		}
	}

}
