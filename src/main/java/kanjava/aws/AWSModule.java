package kanjava.aws;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class AWSModule extends AbstractModule {

	private static final Logger logger = LoggerFactory
			.getLogger(AWSModule.class);

	private static final String ENDPOINT = "ec2.ap-northeast-1.amazonaws.com";

	private AWSCredentials credentials;

	@Override
	protected void configure() {

		bind(String.class).annotatedWith(Names.named("EC2 KeyName"))
				.toInstance("tksmd");
		try {
			credentials = new PropertiesCredentials(
					getResource("AwsCredentials.properties"));
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("AwsCredentials.properties is needed");
			}
			throw new RuntimeException(e);
		}
		bind(AWSCredentials.class).toInstance(credentials);
	}

	@Provides
	AmazonEC2 provideAmazonEC2() {
		AmazonEC2Client ret = new AmazonEC2Client(credentials);
		ret.setEndpoint(ENDPOINT);
		return ret;
	}

	@Provides
	AmazonElasticLoadBalancing provideAmazonElasticLoadBalancing() {
		AmazonElasticLoadBalancingClient ret = new AmazonElasticLoadBalancingClient(
				credentials);
		ret.setEndpoint(ENDPOINT);
		return ret;
	}

	private final InputStream getResource(String url) {
		return Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(url);
	}

}
