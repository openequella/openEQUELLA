package com.tle.web.viewitem.summary.attachment.service;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ViewAttachmentsFreemarkerFactory extends ExtendedFreemarkerFactory
{
	@SuppressWarnings("nls")
	public ViewAttachmentsFreemarkerFactory()
	{
		setName("ViewAttachmentsFreemarkerFactory");
	}
}
