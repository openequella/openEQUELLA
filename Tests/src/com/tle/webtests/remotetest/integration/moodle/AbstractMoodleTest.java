package com.tle.webtests.remotetest.integration.moodle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("moodle")
public abstract class AbstractMoodleTest extends AbstractCleanupTest
{
	private static Pattern versionPattern = Pattern.compile(".*/moodle(\\d\\d)/");
	private String moodleUrl;
	private int moodleVersion;
	private int order = 1000;

	public void setMoodleUrl(String moodleUrl)
	{
		this.moodleUrl = moodleUrl;
		Matcher matcher = versionPattern.matcher(moodleUrl);
		matcher.matches();
		this.moodleVersion = Integer.parseInt(matcher.group(1));
	}

	@Override
	protected void customisePageContext()
	{
		if( moodleUrl != null )
		{
			context.setIntegUrl(moodleUrl);
		}
		else
		{
			setMoodleUrl(context.getIntegUrl());
		}
		context.setNamePrefix(context.getNamePrefix() + '_' + moodleVersion);
		context.setAttribute("moodle_version", moodleVersion);
		namePrefix = context.getNamePrefix();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName().toString() + "_moodle" + moodleVersion;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public int getMoodleVersion()
	{
		return moodleVersion;
	}

}
