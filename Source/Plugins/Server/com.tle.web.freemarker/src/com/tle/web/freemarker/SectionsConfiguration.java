package com.tle.web.freemarker;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.methods.UserFormatMethod;

@Bind
@Singleton
public class SectionsConfiguration extends BasicConfiguration
{
	@Inject
	private CustomTemplateLoader customLoader;
	@Inject
	private SectionsBeansWrapper beanWrapper;
	@Inject
	private UserService userService;

	@PostConstruct
	protected void setup()
	{
		setObjectWrapper(beanWrapper);
		setTemplateLoader(customLoader);
		setSharedVariable("_userformat", new UserFormatMethod(userService)); //$NON-NLS-1$
	}
}
