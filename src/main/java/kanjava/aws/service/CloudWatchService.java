package kanjava.aws.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CloudWatchService extends AbstractAWSService {

	@Inject
	private AmazonCloudWatch cloudWatch;

	public void putMetricData(String metricName, String dimensionName,
			Map<String, Number> values) {
		PutMetricDataRequest request = new PutMetricDataRequest()
				.withNamespace("kanjava");
		List<MetricDatum> metricData = newArrayList();
		for (Entry<String, Number> entry : values.entrySet()) {
			MetricDatum datum = new MetricDatum()
					.withMetricName(metricName)
					.withValue(entry.getValue().doubleValue())
					.withDimensions(
							new Dimension().withName(dimensionName).withValue(
									entry.getKey()));
			metricData.add(datum);
		}
		request.setMetricData(metricData);
		cloudWatch.putMetricData(request);
	}
}
