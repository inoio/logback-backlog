/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package de.inoio.logback_backlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for <code>BurstFilter</code>.
 * 
 */
public class BurstFilterTest {
	private static Logger logger = LoggerFactory.getLogger(BurstFilterTest.class);

	/**
	 * Execute test using config file provided as argument
	 * 
	 * @param argv
	 */
	public static void main(String argv[]) {
		test();
	}

	/**
	 * Print test usage.
	 * 
	 * @param msg
	 */
	static void usage(String msg) {
		System.err.println(msg);
		System.err.println("Usage: java " + BurstFilterTest.class.getName() + " configFile");
		System.exit(1);
	}

	/**
	 * Test BurstFilter by surpassing maximum number of log messages allowed by
	 * filter and making sure only the maximum number are indeed logged, then
	 * wait for while and make sure the filter allows the appropriate number of
	 * messages to be logged.
	 */
	static void test() {
		// empty the bucket and make sure no more than 100 errors get logged
		for (int i = 0; i < 110; i++) {
			logger.info("Logging 110 messages, should only see 100 logs # " + (i + 1));
		}

		// the bucket should be empty, now wait for 12 seconds and see if we can
		// log more messages
		try {
			Thread.sleep(12000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 110; i++) {
			logger.info("Waited 12 seconds and trying to log again, should only see 20 logs #" + (i + 1));
		}

		// now log 110 debugs, they shouldn't get through because the filter's
		// level is set at info
		for (int i = 0; i < 110; i++) {
			logger.debug("TEST FAILED! Logging 110 debug messages, shouldn't see any of them because they are debugs #" + (i + 1));
		}

		// now log 110 infos, they shouldn't get through because the filter's
		// level is set at info
		for (int i = 0; i < 110; i++) {
			logger.info("TEST FAILED! Logging 110 info messages, shouldn't see any of them because they are infos #" + (i + 1));
		}

		// now log 110 warns, they shouldn't get through because the filter's
		// level is set at info
		for (int i = 0; i < 110; i++) {
			logger.warn("Logging 110 warn messages, should see all of them because they are warns #" + (i + 1));
		}

		// now log 110 errors, they should all get through because the filter
		// level is set at info
		for (int i = 0; i < 110; i++) {
			logger.error("Logging 110 error messages, should see all of them because they are errors #" + (i + 1));
		}

		// wait and make sure we can log messages again despite the fact we just
		// logged a bunch of warns and errors
		try {
			Thread.sleep(18000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 110; i++) {
			logger.debug("Waited 18 seconds, should see 30 logs #" + (i + 1));
		}
	}
}
