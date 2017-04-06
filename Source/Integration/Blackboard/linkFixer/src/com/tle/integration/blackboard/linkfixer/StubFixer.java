package com.tle.integration.blackboard.linkfixer;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class StubFixer extends AbstractFixer
{
	private final List<String> content = new ArrayList<String>();

	public StubFixer()
	{
		super();
	}

	public void addContent(String newContent)
	{
		content.add(newContent);
	}

	@Override
	public void submit(HttpServletRequest request) throws Exception
	{
		for( String c : content )
		{
			logMessage(0, "OLD " + c);
			FixTextFeedback feedback = new FixTextFeedback();
			logMessage(1, "NEW " + fixText(c, feedback));
			if( feedback.fixedHardCodedIds )
			{
				logMessage(1, "Hard coded ID fixed");
			}
			if( feedback.fixedNonTokenedUrls )
			{
				logMessage(1, "Non token URL fixed");
			}
		}
		System.out.print(log.toString());
	}

	@Override
	protected int getBlackboardVersion()
	{
		return 8;
	}

	@Override
	protected synchronized String getEquellaUrl()
	{
		return equellaUrl;
	}

	public synchronized void setEquellaUrl(String equellaUrl)
	{
		this.equellaUrl = equellaUrl;
	}

	@Override
	protected String getRelativePath()
	{
		return "webapps/dych/tle/etc";
	}
}
