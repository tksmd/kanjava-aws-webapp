package kanjava.aws;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void isTweetNeed1() throws Exception {
		assertFalse(Utils.isTweetMonitor());
	}

}
