package kanjava.aws.action;

import static com.google.common.collect.Maps.*;

import java.util.Map;

import kanjava.aws.service.EC2Service;
import kanjava.aws.service.ELBService;

import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Json;
import org.seasar.cubby.action.Path;
import org.seasar.cubby.action.RequestParameter;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Volume;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

@ActionClass
@RequestScoped
public class ApiAction {

	@Inject
	private EC2Service ec2Service;

	@Inject
	private ELBService elbService;

	@Inject
	@Named("Tomcat ImageId")
	private String tomcatImageId;

	@Inject
	@Named("GlassFish ImageId")
	private String glassFishImageId;

	@RequestParameter
	private String imageType;

	@RequestParameter
	private String instanceId;

	@RequestParameter
	private String volumeId;

	@RequestParameter
	private String elbName;

	public ActionResult index() {
		return new Forward("/api.jsp");
	}

	@Path("ec2/run/{imageType,tomcat|glassfish}")
	public ActionResult runInstance() {
		Instance instance = null;
		if ("tomcat".equals(imageType)) {
			instance = ec2Service.runInstance(tomcatImageId, imageType);
		} else if ("glassfish".equals(imageType)) {
			instance = ec2Service.runInstance(glassFishImageId, imageType);
		}
		return new Json(instance);
	}

	@Path("ec2/instance/{instanceId,i-[a-zA-Z0-9]+}")
	public ActionResult getInstance() {
		Instance instance = ec2Service.getInstance(instanceId);
		return new Json(instance);
	}

	@Path("ec2/terminate/{instanceId,i-[a-zA-Z0-9]+}")
	public ActionResult terminateInstance() {
		InstanceStateChange stateChange = ec2Service
				.terminateInstance(instanceId);
		return new Json(stateChange);
	}

	@Path("ebs/create")
	public ActionResult createVolume() {
		Volume volume = ec2Service.createVolume();
		return new Json(volume);
	}

	@Path("ebs/delete/{volumeId,v-[a-zA-Z0-9]+}")
	public ActionResult deleteVolume() {
		ec2Service.deleteVolume(volumeId);
		return new Json(new Object());
	}

	@Path("elb/create/{elbName,[a-zA-Z0-9\\-]+}")
	public ActionResult createLoadBalancer() {
		String dnsName = elbService.createLoadBalancer(elbName);
		Map<String, String> ret = newHashMap();
		ret.put("name", elbName);
		ret.put("dnsName", dnsName);
		return new Json(ret);
	}

	@Path("elb/delete/{elbName,[a-zA-Z0-9\\-]+}")
	public ActionResult deleteLoadBalancer() {
		elbService.deleteLoadBalancer(elbName);
		return new Json(new Object());
	}

}
