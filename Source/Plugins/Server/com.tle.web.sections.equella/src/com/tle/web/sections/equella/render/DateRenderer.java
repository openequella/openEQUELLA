package com.tle.web.sections.equella.render;

import java.io.IOException;
import java.util.Date;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.system.DateFormatSettings;
import com.tle.common.Check;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;

/**
 * @author Aaron
 */

public class DateRenderer implements SectionRenderable
{
	private final TagRenderer timeAgo;

	@AssistedInject
	protected DateRenderer(@Assisted Date date, ConfigurationService configurationService,
		UserPreferenceService userPreferenceService)
	{
		this(date, false, configurationService, userPreferenceService);
	}

	@AssistedInject
	protected DateRenderer(@Assisted Date date, @Assisted boolean suppressSuffix, ConfigurationService configService,
		UserPreferenceService userPrefs)
	{
		String displayDateFormat = suppressSuffix ? JQueryTimeAgo.DATE_FORMAT_APPROX : getDisplayDateFormat(
			configService, userPrefs);
		timeAgo = JQueryTimeAgo.timeAgoTag(date, suppressSuffix, displayDateFormat);
	}

	private String getDisplayDateFormat(ConfigurationService configService, UserPreferenceService userPrefs)
	{
		final DateFormatSettings sysSettings = configService.getProperties(new DateFormatSettings());
		String systemDateFormat = sysSettings.getDateFormat();

		String displayDateFormat = null;
		String userSelectedDateFormat = userPrefs.getDateFormat();

		if( Check.isEmpty(userSelectedDateFormat) )
		{
			if( Check.isEmpty(systemDateFormat) )
			{
				displayDateFormat = JQueryTimeAgo.DATE_FORMAT_APPROX;
			}
			else
			{
				displayDateFormat = systemDateFormat;
			}
		}
		else
		{
			displayDateFormat = userSelectedDateFormat;
		}

		return displayDateFormat;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		timeAgo.preRender(info);
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		timeAgo.realRender(writer);
	}
}
