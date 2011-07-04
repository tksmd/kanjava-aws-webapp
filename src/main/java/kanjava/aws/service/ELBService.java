package kanjava.aws.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
import com.google.inject.Inject;

public class ELBService extends AbstractAWSService {

	@Inject
	private AmazonElasticLoadBalancing elb;

	/**
	 * ELB を作成する
	 * 
	 * @param name
	 * @return
	 */
	public String createLoadBalancer(String name) {
		CreateLoadBalancerRequest request = new CreateLoadBalancerRequest(name);
		request.setAvailabilityZones(newArrayList(availabilityZone));
		Listener httpListener = new Listener("http", 80, 80);
		Listener appListener = new Listener("http", 8080, 8080);
		request.setListeners(newArrayList(httpListener, appListener));
		CreateLoadBalancerResult result = elb.createLoadBalancer(request);
		return result.getDNSName();
	}

	/**
	 * ELB にインスタンスを紐づける
	 * 
	 * @param elbName
	 * @param instanceId
	 * @return
	 */
	public List<Instance> registerInstanceWithLoadBalancer(String elbName,
			String instanceId) {

		RegisterInstancesWithLoadBalancerRequest request = new RegisterInstancesWithLoadBalancerRequest();
		request.setLoadBalancerName(elbName);
		request.setInstances(newArrayList(new Instance(instanceId)));
		RegisterInstancesWithLoadBalancerResult result = elb
				.registerInstancesWithLoadBalancer(request);

		return result.getInstances();
	}

	/**
	 * ELB からインスタンスを削除する
	 * 
	 * @param elbName
	 * @param instanceId
	 * @return
	 */
	public List<Instance> deregisterInstanceWithLoadBalancer(String elbName,
			String instanceId) {
		DeregisterInstancesFromLoadBalancerRequest request = new DeregisterInstancesFromLoadBalancerRequest();
		request.setLoadBalancerName(elbName);
		request.setInstances(newArrayList(new Instance(instanceId)));
		DeregisterInstancesFromLoadBalancerResult result = elb
				.deregisterInstancesFromLoadBalancer(request);
		return result.getInstances();
	}

	/**
	 * ELB を削除する
	 * 
	 * @param elbName
	 */
	public void deleteLoadBalancer(String elbName) {
		DeleteLoadBalancerRequest request = new DeleteLoadBalancerRequest(
				elbName);
		elb.deleteLoadBalancer(request);
	}

}
