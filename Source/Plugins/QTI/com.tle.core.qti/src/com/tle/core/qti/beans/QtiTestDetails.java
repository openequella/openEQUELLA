package com.tle.core.qti.beans;

import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;

/**
 * @author Aaron
 */
public class QtiTestDetails
{
	private String title;
	private String toolName;
	private String toolVersion;
	private int partCount;
	private int sectionCount;
	private int questionCount;
	private long minTime;
	private long maxTime;
	private boolean allowLateSubmission;
	private boolean allowSkipping;
	private NavigationMode navigationMode;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getToolName()
	{
		return toolName;
	}

	public void setToolName(String toolName)
	{
		this.toolName = toolName;
	}

	public String getToolVersion()
	{
		return toolVersion;
	}

	public void setToolVersion(String toolVersion)
	{
		this.toolVersion = toolVersion;
	}

	public int getPartCount()
	{
		return partCount;
	}

	public void setPartCount(int partCount)
	{
		this.partCount = partCount;
	}

	public int getSectionCount()
	{
		return sectionCount;
	}

	public void setSectionCount(int sectionCount)
	{
		this.sectionCount = sectionCount;
	}

	public int getQuestionCount()
	{
		return questionCount;
	}

	public void setQuestionCount(int questionCount)
	{
		this.questionCount = questionCount;
	}

	public long getMinTime()
	{
		return minTime;
	}

	public void setMinTime(long minTime)
	{
		this.minTime = minTime;
	}

	public long getMaxTime()
	{
		return maxTime;
	}

	public void setMaxTime(long maxTime)
	{
		this.maxTime = maxTime;
	}

	public boolean isAllowLateSubmission()
	{
		return allowLateSubmission;
	}

	public void setAllowLateSubmission(boolean allowLateSubmission)
	{
		this.allowLateSubmission = allowLateSubmission;
	}

	public boolean isAllowSkipping()
	{
		return allowSkipping;
	}

	public void setAllowSkipping(boolean allowSkipping)
	{
		this.allowSkipping = allowSkipping;
	}

	public NavigationMode getNavigationMode()
	{
		return navigationMode;
	}

	public void setNavigationMode(NavigationMode navigationMode)
	{
		this.navigationMode = navigationMode;
	}
}
