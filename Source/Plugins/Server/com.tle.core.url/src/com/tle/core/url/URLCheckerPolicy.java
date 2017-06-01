/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.url;

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.lt;
import static org.hibernate.criterion.Restrictions.or;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.SimpleExpression;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.beans.ReferencedURL;
import com.tle.core.guice.Bind;

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
@Bind
@Singleton
public class URLCheckerPolicy
{
	@Inject(optional = true)
	@Named("urlChecker.triesUntilWarning")
	private int triesUntilWarning = 5;

	@Inject(optional = true)
	@Named("urlChecker.triesUntilDisabled")
	private int triesUntilDisabled = 10;

	public boolean isUrlDisabled(ReferencedURL rurl)
	{
		return !rurl.isSuccess() && rurl.getTries() >= triesUntilDisabled;
	}

	/**
	 * Review the checking policy on the class.
	 */
	public boolean requiresChecking(ReferencedURL rurl)
	{
		// DO NOT CHANGE THIS METHOD without also changing the other methods in
		// this policy class, and also the documented policy in the class
		// comments.

		final long lastCheck = rurl.getLastChecked().getTime();

		if( lastCheck < oneMonthAgo() )
		{
			return true;
		}

		if( !rurl.isSuccess() && rurl.getTries() < triesUntilDisabled )
		{
			if( lastCheck < oneDayAgo() )
			{
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("nls")
	public Criteria addRequiresCheckingCriteria(Criteria c)
	{
		// DO NOT CHANGE THIS METHOD without also changing the other methods in
		// this policy class, and also the documented policy in the class
		// comments.

		SimpleExpression overOneMonthAgo = lt("lastChecked", new Date(oneMonthAgo()));

		SimpleExpression overOneDayAgo = lt("lastChecked", new Date(oneDayAgo()));
		SimpleExpression lessThanFiveTries = lt("tries", triesUntilDisabled);
		SimpleExpression notSuccessful = eq("success", false);
		LogicalExpression badButStillTrying = and(notSuccessful, and(lessThanFiveTries, overOneDayAgo));

		return c.add(or(overOneMonthAgo, badButStillTrying));
	}

	private long oneMonthAgo()
	{
		return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
	}

	private long oneDayAgo()
	{
		return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
	}

	public int getTriesUntilWarning()
	{
		return triesUntilWarning;
	}

	public int getTriesUntilDisabled()
	{
		return triesUntilDisabled;
	}
}
