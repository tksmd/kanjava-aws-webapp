package kanjava.aws.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EC2Service extends AbstractAWSService {

	@Inject
	private AmazonEC2 ec2;

	@Inject
	@Named("EC2 KeyName")
	private String keyName;

	/**
	 * 稼働しているインスタンスのリストを取得する
	 * 
	 * @return
	 */
	public List<Instance> getRunnningInstances() {

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request.setFilters(newArrayList(new Filter("instance-state-code",
				newArrayList("16"))));

		DescribeInstancesResult result = ec2.describeInstances(request);

		List<Instance> ret = new ArrayList<Instance>();
		List<Reservation> reservations = result.getReservations();
		for (Reservation r : reservations) {
			ret.addAll(r.getInstances());
			// ret.addAll(filter(r.getInstances(), new InstanceStatePredicate(
			// InstanceStateName.Running)));
		}
		return ret;
	}

	/**
	 * インスタンスを生成する
	 * 
	 * @param imageId
	 * @return
	 */
	public Instance runInstance(String imageId) {
		RunInstancesRequest request = new RunInstancesRequest(imageId, 1, 1);
		request.setInstanceType("t1.micro");
		request.setKeyName(keyName);
		request.setPlacement(new Placement(availabilityZone));
		RunInstancesResult result = ec2.runInstances(request);
		return result.getReservation().getInstances().get(0);
	}

	/**
	 * インスタンスを停止する
	 * 
	 * @param instanceId
	 * @return
	 */
	public InstanceStateChange terminateInstance(String instanceId) {
		TerminateInstancesRequest request = new TerminateInstancesRequest(
				newArrayList(instanceId));
		TerminateInstancesResult result = ec2.terminateInstances(request);
		return result.getTerminatingInstances().get(0);
	}

	/**
	 * EBS を生成する
	 * 
	 * @return
	 */
	public Volume createVolume() {
		CreateVolumeRequest request = new CreateVolumeRequest(1,
				availabilityZone);
		request.setSize(1);
		CreateVolumeResult result = ec2.createVolume(request);
		return result.getVolume();
	}

	/**
	 * EBS をサーバにアタッチする
	 * 
	 * @param instanceId
	 * @param volumeId
	 * @param device
	 * @return
	 */
	public VolumeAttachment attachVolume(String instanceId, String volumeId,
			String device) {
		AttachVolumeRequest attachVolumeRequest = new AttachVolumeRequest(
				volumeId, instanceId, device);
		AttachVolumeResult result = ec2.attachVolume(attachVolumeRequest);
		return result.getAttachment();
	}

	/**
	 * EBS をサーバからデタッチする
	 * 
	 * @param volumeId
	 * @return
	 */
	public VolumeAttachment detachVolume(String volumeId) {
		DetachVolumeRequest request = new DetachVolumeRequest(volumeId);
		request.setForce(true);
		DetachVolumeResult result = ec2.detachVolume(request);
		return result.getAttachment();
	}

	/**
	 * EBS を削除する
	 * 
	 * @param volumeId
	 */
	public void deleteVolume(String volumeId) {
		DeleteVolumeRequest request = new DeleteVolumeRequest(volumeId);
		ec2.deleteVolume(request);
	}

	/**
	 * インスタンスの状態によってフィルタリングするための {@link Predicate}
	 */
	static class InstanceStatePredicate implements Predicate<Instance> {

		InstanceStateName stateName;

		public InstanceStatePredicate(InstanceStateName stateName) {
			this.stateName = stateName;
		}

		@Override
		public boolean apply(Instance input) {
			InstanceState state = input.getState();
			return this.stateName.toString().equals(state.getName());
		}
	}

}
