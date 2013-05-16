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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * 
 * The <code>BurstFilter</code> is a logging filter that regulates logging
 * traffic. Use this filter when you want to control the maximum burst of log
 * statements that can be sent to an appender. The filter is configured in the
 * logback configuration file. For example, the following configuration limits
 * the number of INFO level (and lower) log statements that can be sent to the
 * console to a burst of 100 and allows a maximum of 10 log statements to be
 * sent to the appender every 6 seconds after that burst.<br>
 * <br>
 * 
 * <code> 
 * 
 * &lt;appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"&gt;<br>
 *  &nbsp;&lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;<br>
 * 		&nbsp;&nbsp;&lt;Pattern&gt;%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n&lt;/Pattern&gt;<br>
 * 	&nbsp;&lt;/layout&gt;<br>
 * 	&nbsp;&lt;Filter class="BurstFilter"&gt;<br>
 * 		&nbsp;&nbsp;&lt;param name="level" value="INFO"/&gt;<br>
 * 		&nbsp;&nbsp;&lt;param name="burstRecoveryAmount" value="10"/&gt;<br>
 * 		&nbsp;&nbsp;&lt;param name="burstRecoveryInterval" value="6"/&gt;<br>
 * 		&nbsp;&nbsp;&lt;param name="maxBurst" value="100"/&gt;<br>
 * 	&nbsp;&lt;/Filter&gt;<br>
 * &lt;/appender&gt;<br>
 * </code><br>
 * 
 * @author Chad LaVigne
 * 
 */
public class BurstFilter extends Filter<LoggingEvent> {
	/**
	 * Level of messages to be filtered. Anything at or below this level will be
	 * filtered out if <code>maxBurst</code> has been exceeded. The default is
	 * WARN meaning any messages that are higher than warn will be logged
	 * regardless of the size of a burst.
	 */
	private Level level = Level.WARN;

	/**
	 * Number of log statments to allow following a traffic burst of more than
	 * <code>maxBurst</code>. This many statements is added to the total number
	 * allowed every <code>burstRecoveryInterval</code> seconds.
	 */
	private long burstRecoveryAmount;

	/**
	 * Interval, in seconds, at which to add to the number of log statements
	 * that will be allowed following a burst. This value specifies how often
	 * <code>burstRecoverAmount</code> statements will be added to the total
	 * number allowed for every <code>burstRecoveryInterval</code> that passes
	 * following a burst, up to but not exceeding <code>maxBurst</code>.
	 */
	private long burstRecoveryInterval;

	/**
	 * This value dictates the maximum traffic burst that can be logged to any
	 * appender that uses the <code>BurstFilter</code>, i.e. there can never be
	 * more than <code>maxBurst</code> log statements sent to an appender in
	 * <code>burstRecoveryInterval</code> seconds.
	 */
	private long maxBurst;

	/**
	 * Token bucket implementation to throttle the number of messages that can
	 * be logged to an appender using this filter.
	 */
	private TokenBucket tokenBucket;

	/**
	 * Decide if we're going to log <code>event</code> based on whether the
	 * maximum burst of log statements has been exceeded.
	 * 
	 * @see ch.qos.logback.core.filter.Filter#decide(java.lang.Object)
	 */
	@Override
	public FilterReply decide(LoggingEvent event) {
		// initialize tokenBucket here because the burstRecoveryAmount,
		// burstRecoveryInterval & maxBurst attributes get set
		// via logback.xml configuration when it instantiates the filter

		if (tokenBucket == null) {
			tokenBucket = new TokenBucket(burstRecoveryAmount, burstRecoveryInterval, maxBurst);
		}

		return event.getLevel().toInt() > level.toInt() || tokenBucket.getToken() ? FilterReply.NEUTRAL : FilterReply.DENY;
	}

	/**
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	/**
	 * @return the burstRecoveryAmount
	 */
	public long getBurstRecoveryAmount() {
		return burstRecoveryAmount;
	}

	/**
	 * @param burstRecoveryAmount
	 *            the burstRecoveryAmount to set
	 */
	public void setBurstRecoveryAmount(long burstRecoveryAmount) {
		this.burstRecoveryAmount = burstRecoveryAmount;
	}

	/**
	 * @return the burstRecoveryInterval
	 */
	public long getBurstRecoveryInterval() {
		return burstRecoveryInterval;
	}

	/**
	 * @param burstRecoveryInterval
	 *            the burstRecoveryInterval to set
	 */
	public void setBurstRecoveryInterval(long burstRecoveryInterval) {
		this.burstRecoveryInterval = burstRecoveryInterval;
	}

	/**
	 * @return the maxBurst
	 */
	public long getMaxBurst() {
		return maxBurst;
	}

	/**
	 * @param maxBurst
	 *            the maxBurst to set
	 */
	public void setMaxBurst(long maxBurst) {
		this.maxBurst = maxBurst;
	}

	/**
	 * 
	 * Simple Token Bucket implementation to control traffic bursts. The
	 * <code>TokenBucket</code> adds a token to the bucket at the rate of
	 * <code>fillAmount</code> tokens every <code>fillInterval</code> seconds up
	 * to <code>maxTokens</code>.
	 * 
	 */
	private class TokenBucket {
		/**
		 * Number of tokens to add to the bucket, <code>fillAmount</code> tokens
		 * are added to the bucket every <code>fillInterval</code> seconds.
		 */
		private long fillAmount;

		/**
		 * Interval at which to add tokens to the bucket,
		 * <code>fillAmount</code> tokens are added to the bucket every
		 * <code>fillInterval</code> seconds.
		 */
		private long fillInterval;

		/**
		 * The maximum number of tokens allowed in the bucket. This becomes the
		 * peak traffic burst allowed by the token bucket in
		 * <code>fillInterval</code> seconds.
		 */
		private long maxTokens;

		/**
		 * The number of tokens currently in the bucket.
		 */
		private long currentNumberOfTokens;

		/**
		 * Time of last token removal.
		 */
		private long lastTokenRemovedTime;

		/**
		 * Create a new <code>TokenBucket</code> that will add
		 * <code>fillAmount</code> tokens every <code>fillInterval</code>
		 * seconds up to a maximum of <code>maxTokens</code>
		 * 
		 * @param fillAmount
		 * @param fillInterval
		 * @param maxTokens
		 */
		public TokenBucket(long fillAmount, long fillInterval, long maxTokens) {
			this.fillAmount = fillAmount;
			this.fillInterval = fillInterval;
			this.maxTokens = maxTokens;
			this.currentNumberOfTokens = maxTokens;
			lastTokenRemovedTime = System.currentTimeMillis();
		}

		/**
		 * Method to get a token from the bucket. If the bucket is not empty a
		 * token is removed.
		 * 
		 * @return
		 */
		public synchronized boolean getToken() {
			replaceTokens();
			boolean isEmpty = currentNumberOfTokens <= 0;

			if (!isEmpty) {
				currentNumberOfTokens--;
				lastTokenRemovedTime = System.currentTimeMillis();
			}

			return !isEmpty;
		}

		/**
		 * Method to replace tokens that have been removed based on the amount
		 * of time that has passed since tokens were last added. For every
		 * <code>fillInterval</code> seconds that have passed,
		 * <code>fillAmount</code> tokens will be added up to
		 * <code>maxTokens</code>.
		 */
		private void replaceTokens() {
			long currentTime = System.currentTimeMillis();
			long secondsSinceLastFill = (long) ((currentTime / 1000) - (lastTokenRemovedTime / 1000));

			if (secondsSinceLastFill >= fillInterval) {
				long numberOfTokensToAdd = (secondsSinceLastFill / fillInterval) * fillAmount;
				currentNumberOfTokens = currentNumberOfTokens + numberOfTokensToAdd > maxTokens ? maxTokens : currentNumberOfTokens + numberOfTokensToAdd;
			}
		}
	}
}