package kanjava.aws.service;

import java.util.List;

import kanjava.aws.Utils;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ELBService extends AbstractAWSService {

	@Inject
	private AmazonElasticLoadBalancing elb;

	/**
	 * ELB を作成する
	 * 
	 * @param name
	 * @return
	 */
	public LoadBalancerDescription createLoadBalancer(String name) {
		CreateLoadBalancerRequest request = new CreateLoadBalancerRequest(name)
				.withListeners(new Listener("http", 80, 80),
						new Listener("http", 8080, 8080))
				.withAvailabilityZones(availabilityZone);
		elb.createLoadBalancer(request);

		// ヘルスチェックを構成
		HealthCheck healthCheck = new HealthCheck("HTTP:80/", 30, 5, 2, 10);
		ConfigureHealthCheckRequest healthCheckRequest = new ConfigureHealthCheckRequest(
				name, healthCheck);
		elb.configureHealthCheck(healthCheckRequest);

		return getLoadBalancer(name);
	}

	/**
	 * ELB の情報を取得する
	 * 
	 * @param name
	 * @return
	 */
	public LoadBalancerDescription getLoadBalancer(String name) {
		DescribeLoadBalancersRequest request = new DescribeLoadBalancersRequest()
				.withLoadBalancerNames(name);
		DescribeLoadBalancersResult result = elb.describeLoadBalancers(request);
		if (Utils.isEmpty(result.getLoadBalancerDescriptions())) {
			return null;
		}
		return result.getLoadBalancerDescriptions().get(0);
	}

	/**
	 * ELB にインスタンスを紐づける
	 * 
	 * @param name
	 * @param instanceId
	 * @return
	 */
	public List<Instance> registerInstanceWithLoadBalancer(String name,
			String instanceId) {

		RegisterInstancesWithLoadBalancerRequest request = new RegisterInstancesWithLoadBalancerRequest()
				.withInstances(new Instance(instanceId)).withLoadBalancerName(
						name);
		RegisterInstancesWithLoadBalancerResult result = elb
				.registerInstancesWithLoadBalancer(request);
		return result.getInstances();
	}

	/**
	 * ELB からインスタンスを削除する
	 * 
	 * @param name
	 * @param instanceId
	 * @return
	 */
	public List<Instance> deregisterInstanceWithLoadBalancer(String name,
			String instanceId) {
		DeregisterInstancesFromLoadBalancerRequest request = new DeregisterInstancesFromLoadBalancerRequest()
				.withInstances(new Instance(instanceId)).withLoadBalancerName(
						name);
		DeregisterInstancesFromLoadBalancerResult result = elb
				.deregisterInstancesFromLoadBalancer(request);
		return result.getInstances();
	}

	/**
	 * ELB を削除する
	 * 
	 * @param name
	 */
	public void deleteLoadBalancer(String name) {
		DeleteLoadBalancerRequest request = new DeleteLoadBalancerRequest(name);
		elb.deleteLoadBalancer(request);
	}

}
