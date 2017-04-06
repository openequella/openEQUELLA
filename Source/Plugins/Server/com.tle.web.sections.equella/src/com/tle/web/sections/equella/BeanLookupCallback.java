package com.tle.web.sections.equella;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;

public class BeanLookupCallback implements ModalSessionCallback
{
	private static final long serialVersionUID = 1L;
	protected final String beanName;
	protected final Object pluginObj;

	public BeanLookupCallback(Class<? extends ModalSessionCallback> clazz)
	{
		this.beanName = "bean:" + clazz.getName(); //$NON-NLS-1$
		this.pluginObj = clazz;
	}

	@Override
	public void executeModalFinished(SectionInfo info, ModalSession session)
	{
		ModalSessionCallback lookedUp = ResourcesService.getResourceHelper(pluginObj).getBean(beanName);
		lookedUp.executeModalFinished(info, session);
	}
}
