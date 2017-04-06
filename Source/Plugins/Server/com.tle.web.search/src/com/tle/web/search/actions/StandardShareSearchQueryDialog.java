package com.tle.web.search.actions;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;

/**
 * @author Andrew Gibb
 */
@NonNullByDefault
@Bind
public class StandardShareSearchQueryDialog extends AbstractShareSearchQueryDialog
{
	@Inject
	private StandardShareSearchQuerySection contentSection;

	@Override
	protected AbstractShareSearchQuerySection getContentSection()
	{
		return contentSection;
	}
}
