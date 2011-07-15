package kanjava.aws;

import static kanjava.aws.Utils.getResource;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class AWSModule extends AbstractModule {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AWSModule.class);

	/**
	 * <a href=
	 * "http://aws.amazon.com/articles/3912?_encoding=UTF8&jiveRedirect=1"
	 * >各APIのエンドポイント一覧</a>
	 */
	private static final String EC2_ENDPOINT = "ec2.ap-northeast-1.amazonaws.com";

	/**
	 * <a href=
	 * "http://aws.amazon.com/articles/3912?_encoding=UTF8&jiveRedirect=1"
	 * >各APIのエンドポイント一覧</a>
	 */
	private static final String ELB_ENDPOINT = "elasticloadbalancing.ap-northeast-1.amazonaws.com";

	/**
	 * <a href=
	 * "http://aws.amazon.com/articles/3912?_encoding=UTF8&jiveRedirect=1"
	 * >各APIのエンドポイント一覧</a>
	 */
	private static final String CLOUDWATCH_ENDPOINT = "monitoring.ap-northeast-1.amazonaws.com";

	private AWSCredentials credentials;

	/**
	 * AWS のアカウント環境に依存する設定を行う
	 */
	@Override
	protected void configure() {

		bind(String.class).annotatedWith(Names.named("EC2 KeyName"))
				.toInstance("tksmd");
		bind(String.class).annotatedWith(Names.named("Tomcat ImageId"))
				.toInstance("ami-b6ee5bb7");
		bind(String.class).annotatedWith(Names.named("GlassFish ImageId"))
				.toInstance("ami-beee5bbf");

		try {
			credentials = new PropertiesCredentials(
					getResource("AwsCredentials.properties"));
		} catch (IOException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("AwsCredentials.properties is needed");
			}
			throw new RuntimeException(e);
		}
		bind(AWSCredentials.class).toInstance(credentials);
	}

	@Provides
	AmazonEC2 provideAmazonEC2() {
		AmazonEC2Client ret = new AmazonEC2Client(credentials);
		ret.setEndpoint(EC2_ENDPOINT);
		return ret;
	}

	@Provides
	AmazonElasticLoadBalancing provideAmazonElasticLoadBalancing() {
		AmazonElasticLoadBalancingClient ret = new AmazonElasticLoadBalancingClient(
				credentials);
		ret.setEndpoint(ELB_ENDPOINT);
		return ret;
	}

	@Provides
	AmazonCloudWatch provideAmazonCloudWatch() {
		AmazonCloudWatchClient ret = new AmazonCloudWatchClient(credentials);
		ret.setEndpoint(CLOUDWATCH_ENDPOINT);
		return ret;
	}

}
