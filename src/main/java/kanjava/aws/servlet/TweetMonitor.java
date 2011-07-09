package kanjava.aws.servlet;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

class TweetMonitor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TweetMonitor.class);

	List<String> speakers = newArrayList("kis", "shin1ogawa", "skrb",
			"yoshioterada", "tksmd");

	Twitter twitter;

	long period;

	TweetMonitor(long period) {
		this.period = period;
		this.twitter = new TwitterFactory().getInstance();
	}

	Map<String, Number> getTweetCounts() {
		HashMap<String, Number> ret = newHashMap();
		for (String speaker : speakers) {
			try {
				ret.put(speaker, getTweetCount(speaker));
			} catch (TwitterException e) {
				LOGGER.error("failed to get " + speaker
						+ "'s tweet counts because of : " + e.getMessage());
				ret.put(speaker, -1);
			}
		}
		return ret;
	}

	/**
	 * user の period 以前の時点から現在までにつぶやかれた tweet 数を取得する
	 * 
	 * @param user
	 * @return
	 * @throws TwitterException
	 */
	Integer getTweetCount(String user) throws TwitterException {
		int ret = 0;
		int page = 1;
		boolean cont = true;
		Date start = new Date(new Date().getTime() - period);
		while (cont && page <= 3) {
			Paging paging = new Paging(page, 10);
			ResponseList<Status> timeline = twitter.getUserTimeline(user,
					paging);
			Comparator<Status> newer = Collections
					.reverseOrder(new StatusComparator());
			Collections.sort(timeline, newer);
			for (Status s : timeline) {
				if (s.getCreatedAt().after(start)) {
					ret++;
				} else {
					cont = false;
				}
			}
			if (!cont) {
				break;
			}
			page++;
		}
		return ret;
	}

	static class StatusComparator implements Comparator<Status> {
		@Override
		public int compare(Status o1, Status o2) {
			return o1.getCreatedAt().compareTo(o2.getCreatedAt());
		}
	}

}
