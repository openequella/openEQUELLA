package com.tle.core.url;

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.lt;
import static org.hibernate.criterion.Restrictions.or;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.hibernate.Criteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.SimpleExpression;

import com.tle.beans.ReferencedURL;

/**
 * <p>
 * Because the URL checking policy needs to apply to objects and to DB queries,
 * I'm grouping these together into this single class where it can be documented
 * a single place. Hopefully anyone updating the policy in the future will have
 * enough wits about them to see they need to change them both.
 * </p>
 * <p>
 * So with that out of the way, the policy is:
 * <dl>
 * <dt>Re-check if it's been more than one month since the last check</dt>
 * <dd>URLs come and go, so we need to keep checking. This ensures that URLs are
 * rechecked every month, regardless of how many times they've been checked
 * before and whether those checks were successful or not.</dd>
 * <dt>Re-check unsuccessful URLs if less than 10 tries and it hasn't been
 * checked in over a day</dt>
 * <dd>If a URL has been successful then let's not waste our effort testing it
 * all the time as it's probably OK. The first rule will ensure that we
 * eventually do retry them.</dd>
 * </dl>
 * </p>
 * <p>
 * If the code below doesn't match the policy in the above comment, git blame to
 * find the offender and give them a slapping.
 * </p>
 * 
 * @author Nick Read
 */
public final class URLCheckerPolicy
{
	public static final int TRIES_UNTIL_WARNING = 5;
	public static final int TRIES_UNTIL_DISABLED = 10;

	public static boolean isUrlDisabled(ReferencedURL rurl)
	{
		return !rurl.isSuccess() && rurl.getTries() >= TRIES_UNTIL_DISABLED;
	}

	/**
	 * Review the checking policy on the class.
	 */
	public static boolean requiresChecking(ReferencedURL rurl)
	{
		// DO NOT CHANGE THIS METHOD without also changing the other methods in
		// this policy class, and also the documented policy in the class
		// comments.

		final long lastCheck = rurl.getLastChecked().getTime();

		if( lastCheck < oneMonthAgo() )
		{
			return true;
		}

		if( !rurl.isSuccess() && rurl.getTries() < TRIES_UNTIL_DISABLED )
		{
			if( lastCheck < oneDayAgo() )
			{
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("nls")
	public static Criteria addRequiresCheckingCriteria(Criteria c)
	{
		// DO NOT CHANGE THIS METHOD without also changing the other methods in
		// this policy class, and also the documented policy in the class
		// comments.

		SimpleExpression overOneMonthAgo = lt("lastChecked", new Date(oneMonthAgo()));

		SimpleExpression overOneDayAgo = lt("lastChecked", new Date(oneDayAgo()));
		SimpleExpression lessThanFiveTries = lt("tries", TRIES_UNTIL_DISABLED);
		SimpleExpression notSuccessful = eq("success", false);
		LogicalExpression badButStillTrying = and(notSuccessful, and(lessThanFiveTries, overOneDayAgo));

		return c.add(or(overOneMonthAgo, badButStillTrying));
	}

	private static long oneMonthAgo()
	{
		return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
	}

	private static long oneDayAgo()
	{
		return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
	}

	private URLCheckerPolicy()
	{
		super();
	}
}
