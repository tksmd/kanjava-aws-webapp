package kanjava.aws.servlet;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kanjava.aws.Utils;
import kanjava.aws.service.CloudWatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class TweetListener implements ServletContextListener {

	static final Logger LOGGER = LoggerFactory.getLogger(TweetListener.class);

	static final long PERIOD = 10 * 60 * 1000;

	private Timer timer;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (Utils.isTweetMonitor()) {
			LOGGER.info("Start kanjava TweetMonitorTask");
			ServletContext context = sce.getServletContext();
			timer = new Timer("kanjava TweetMonitorTask");
			timer.schedule(new TweetMonitorTask(context), 0, PERIOD);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (timer != null) {
			LOGGER.info("Stop kanjava TweetMonitorTask");
			timer.cancel();
		}
	}

	static class TweetMonitorTask extends TimerTask {

		Injector injector;

		ServletContext context;

		TweetMonitor monitor;

		TweetMonitorTask(final ServletContext context) {
			this.context = context;
			this.monitor = new TweetMonitor(PERIOD);
			setupInjector();
		}

		@Override
		public void run() {
			setupInjector();
			if (injector == null) {
				LOGGER.warn("Injector not found, thus skip update metrics");
				return;
			}
			CloudWatchService service = injector
					.getInstance(CloudWatchService.class);
			Map<String, Number> tweetCounts = monitor.getTweetCounts();
			service.putMetricData("TweetCounts", "Speakers Metrics",
					tweetCounts);
		}

		void setupInjector() {
			if (injector != null) {
				return;
			}
			injector = (Injector) context
					.getAttribute(Injector.class.getName());
		}

	}

}
