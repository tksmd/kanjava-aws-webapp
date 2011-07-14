package kanjava.aws.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import kanjava.aws.Utils;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class EC2Service extends AbstractAWSService {

	private static final String RUNNNING_STATE_CODE = "16";

	@Inject
	private AmazonEC2 ec2;

	@Inject
	@Named("EC2 KeyName")
	private String keyName;

	/**
	 * 稼働しているインスタンスのリストを取得する
	 * 
	 * @see <a
	 *      href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/index.html?ApiReference-query-DescribeInstances.html">DescribeInstances</a>
	 * 
	 * @return
	 */
	public List<Instance> getRunnningInstances() {
		Filter runningFilter = new Filter("instance-state-code",
				newArrayList(RUNNNING_STATE_CODE));
		DescribeInstancesRequest request = new DescribeInstancesRequest()
				.withFilters(newArrayList(runningFilter));
		DescribeInstancesResult result = ec2.describeInstances(request);
		List<Instance> ret = new ArrayList<Instance>();
		List<Reservation> reservations = result.getReservations();
		for (Reservation r : reservations) {
			ret.addAll(r.getInstances());
		}
		return ret;
	}

	/**
	 * 指定されたインスタンスの詳細情報を取得する
	 * 
	 * @param instanceId
	 * @return
	 */
	public Instance getInstance(String instanceId) {
		DescribeInstancesRequest request = new DescribeInstancesRequest()
				.withInstanceIds(instanceId);
		DescribeInstancesResult result = ec2.describeInstances(request);
		List<Reservation> reservations = result.getReservations();
		if (Utils.isEmpty(reservations)) {
			return null;
		}
		List<Instance> instances = reservations.get(0).getInstances();
		if (Utils.isEmpty(instances)) {
			return null;
		}
		return instances.get(0);
	}

	/**
	 * インスタンスを生成する
	 * 
	 * @param imageId
	 * @return
	 */
	public Instance runInstance(String imageId, String name) {
		RunInstancesRequest request = new RunInstancesRequest(imageId, 1, 1)
				.withInstanceType("t1.micro").withKeyName(keyName)
				.withPlacement(new Placement(availabilityZone))
				.withSecurityGroups("kanjava");
		RunInstancesResult result = ec2.runInstances(request);
		Instance ret = result.getReservation().getInstances().get(0);

		// タグを打つ
		createNameTag(ret.getInstanceId(), name);
		return ret;
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
				availabilityZone).withSize(1);
		CreateVolumeResult result = ec2.createVolume(request);
		return result.getVolume();
	}

	/**
	 * EBS の情報を取得する
	 * 
	 * @param volumeId
	 * @return
	 */
	public Volume getVolume(String volumeId) {
		DescribeVolumesRequest request = new DescribeVolumesRequest()
				.withVolumeIds(volumeId);
		DescribeVolumesResult result = ec2.describeVolumes(request);
		List<Volume> volumes = result.getVolumes();
		if (Utils.isEmpty(volumes)) {
			return null;
		}
		return volumes.get(0);
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
		DetachVolumeRequest request = new DetachVolumeRequest(volumeId)
				.withForce(true);
		DetachVolumeResult result = ec2.detachVolume(request);
		return result.getAttachment();
	}

	/**
	 * EBS を削除する
	 * 
	 * @param volumeId
	 */
	public void deleteVolume(String volumeId) {
		Volume volume = getVolume(volumeId);
		if ("in-use".equals(volume.getState())) {
			detachVolume(volumeId);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
		DeleteVolumeRequest request = new DeleteVolumeRequest(volumeId);
		ec2.deleteVolume(request);
	}

	/**
	 * Name タグを設定する
	 * 
	 * @param resourceId
	 * @param name
	 */
	protected void createNameTag(String resourceId, String name) {
		CreateTagsRequest request = new CreateTagsRequest().withResources(
				resourceId).withTags(new Tag("Name", name));
		ec2.createTags(request);
	}
}
