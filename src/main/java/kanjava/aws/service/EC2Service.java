package kanjava.aws.service;

import static com.google.common.collect.Lists.newArrayList;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.google.inject.Inject;

public class EC2Service extends AbstractAWSService {

	@Inject
	private AmazonEC2 ec2;

	/**
	 * インスタンスを生成する
	 * 
	 * @param imageId
	 * @return
	 */
	public Instance runInstance(String imageId) {
		RunInstancesRequest request = new RunInstancesRequest(imageId, 1, 1);
		request.setInstanceType("t1.micro");
		request.setKeyName("tksmd"); // TODO
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

}
