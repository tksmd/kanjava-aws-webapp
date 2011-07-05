package kanjava.aws.action;

import kanjava.aws.service.EC2Service;
import kanjava.aws.service.ELBService;

import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Direct;
import org.seasar.cubby.action.Json;
import org.seasar.cubby.action.Path;

import com.amazonaws.services.ec2.model.Instance;
import com.google.inject.Inject;

public class ApiAction {

	@Inject
	private EC2Service ec2Service;

	@Inject
	private ELBService elbService;

	@Path("api/ec2/run/")
	public ActionResult runInstance() {
		Instance instance = ec2Service.runInstance("");
		return new Json(instance);
	}

}
