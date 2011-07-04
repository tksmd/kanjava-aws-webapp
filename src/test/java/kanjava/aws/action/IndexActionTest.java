package kanjava.aws.action;

import static org.seasar.cubby.unit.CubbyAssert.assertPathEquals;
import static org.seasar.cubby.unit.CubbyRunner.processAction;
import kanjava.aws.ApplicationModule;

import org.junit.Before;
import org.junit.Test;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

public class IndexActionTest {

	MockServletContext servletContext;

	MockHttpServletRequest request;

	MockHttpServletResponse response;

	@Before
	public void before() throws Exception {
		this.servletContext = new MockServletContext();
		Injector injector = Guice.createInjector(new ApplicationModule());
		servletContext.setAttribute(Injector.class.getName(), injector);

		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();
	}

	@Test
	public void index1() throws Exception {
		request.setMethod("GET");
		request.setServletPath("/");
		ActionResult actualResult = processAction(servletContext, request,
				response, new GuiceFilter());
		assertPathEquals(Forward.class, "index.jsp", actualResult);
	}
	
	/**
	 * TODO: service を mock 化した感じに出来ると
	 * @throws Exception
	 */
	@Test
	public void status1() throws Exception {
		request.setMethod("GET");
		request.setServletPath("/status");
		ActionResult actualResult = processAction(servletContext, request,
				response, new GuiceFilter());
		assertPathEquals(Forward.class, "status.jsp", actualResult);
	}	
	
	
	

}
