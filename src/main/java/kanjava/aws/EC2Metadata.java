package kanjava.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * @author someda
 * @see <a
 *      href="http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/index.html?AESDG-chapter-instancedata.html">Using
 *      Instance Metadata</a>
 */
public class EC2Metadata {

	private static final String BASE_FORMAT = "http://169.254.169.254/%s/meta-data/";

	private String baseUrl;

	private String version;

	private HttpClient client;

	public EC2Metadata() {
		this("latest");
	}

	public EC2Metadata(String version) {
		this.version = version;
		this.baseUrl = String.format(BASE_FORMAT, this.version);
		initClient();
	}

	void initClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams
				.setUserAgent(params, "EC2Metadata Java Client 0.0.1");
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		HttpConnectionParams.setTcpNoDelay(params, true);
		this.client = new DefaultHttpClient(params);
	}

	public List<String> getMetadataKeys() {
		return list("");
	}

	public String getInstanceId() {
		return get("instance-id");
	}

	public String getLocalHostName() {
		return get("local-hostname");
	}

	public String getLocalIPv4() {
		return get("local-ipv4");
	}

	public String getPublicHostName() {
		return get("public-hostname");
	}

	public String getPublicIPv4() {
		return get("public-ipv4");
	}

	public boolean isAvailable() {
		try {
			doLoad("");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String get(String name) {
		try {
			List<String> value = doLoad(name);
			return Utils.join(value, "\n");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> list(String name) {
		try {
			return doLoad(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> doLoad(String name) throws ClientProtocolException,
			IOException {
		if (name == null) {
			throw new IllegalArgumentException();
		}

		if (name.startsWith("/")) {
			name = name.substring(1);
		}
		HttpGet httpGet = new HttpGet(baseUrl + "/" + name);
		HttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream content = entity.getContent();
			return IOUtils.readLines(content);
		}
		return Collections.emptyList();
	}

}
