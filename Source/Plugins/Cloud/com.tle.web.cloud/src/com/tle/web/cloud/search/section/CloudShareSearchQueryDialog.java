package com.tle.web.cloud.search.section;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractShareSearchQueryDialog;
import com.tle.web.search.actions.AbstractShareSearchQuerySection;

@NonNullByDefault
@Bind
public class CloudShareSearchQueryDialog extends AbstractShareSearchQueryDialog
{
	@Inject
	private CloudShareSearchQuerySection contentSection;

	@Override
	protected AbstractShareSearchQuerySection getContentSection()
	{
		return contentSection;
	}
}
