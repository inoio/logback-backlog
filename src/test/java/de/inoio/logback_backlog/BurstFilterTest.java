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

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * Unit test for <code>BurstFilter</code>.
 * 
 */

public class BurstFilterTest {

  private Logger logger = LoggerFactory.getLogger(BurstFilterTest.class);

  @Test
  public void test() {

    BurstFilter burstFilter = getBurstFilter();

    // empty the bucket and make sure no more than 100 errors get logged
    for (int i = 0; i < 110; i++) {
      logger.info("Logging 110 messages, should only see 100 logs # " + (i + 1));
    }
    
    Assert.assertEquals(100, burstFilter.getNeutralCount());
    Assert.assertEquals(10, burstFilter.getDenyCount());

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
    
    Assert.assertEquals(120, burstFilter.getNeutralCount());
    Assert.assertEquals(100, burstFilter.getDenyCount());

    // now log 110 debugs, they shouldn't get through because the filter's
    // level is set at info
    for (int i = 0; i < 110; i++) {
      logger.debug("TEST FAILED! Logging 110 debug messages, shouldn't see any of them because they are debugs #" + (i + 1));
    }
    
    Assert.assertEquals(120, burstFilter.getNeutralCount());
    Assert.assertEquals(210, burstFilter.getDenyCount());


    // wait and make sure we can log messages again despite the fact we just
    // logged a bunch of warns and errors
    try {
      Thread.sleep(18000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < 110; i++) {
      logger.warn("Waited 18 seconds, should see 30 logs #" + (i + 1));
    }
    
    Assert.assertEquals(150, burstFilter.getNeutralCount());
    Assert.assertEquals(290, burstFilter.getDenyCount());

  }

  private BurstFilter getBurstFilter() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    for (ch.qos.logback.classic.Logger logger : loggerContext.getLoggerList())
      for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();)
        return (BurstFilter) index.next().getCopyOfAttachedFiltersList().get(0);
    return null;
  }

}
