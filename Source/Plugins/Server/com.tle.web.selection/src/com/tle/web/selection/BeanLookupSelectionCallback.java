package com.tle.web.selection;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.BeanLookupCallback;

/**
 * @author aholland
 */
public class BeanLookupSelectionCallback extends BeanLookupCallback implements SelectionsMadeCallback
{
	private static final long serialVersionUID = 1L;

	public BeanLookupSelectionCallback(Class<? extends SelectionsMadeCallback> clazz)
	{
		super(clazz);
	}

	@Override
	public boolean executeSelectionsMade(SectionInfo info, SelectionSession session)
	{
		SelectionsMadeCallback lookedUp = ResourcesService.getResourceHelper(pluginObj).getBean(beanName);
		lookedUp.executeSelectionsMade(info, session);
		return false;
	}
}
